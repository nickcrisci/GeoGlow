import os
import json
import paho.mqtt.client as mqtt
import database as db

SERVICE_TOPIC = "GeoGlow/Friend-Service"

def __get_sub_topics(filename: str) -> list:
    """
    Reads subscription topics and QoS levels from a file.

    This function parses a configuration file containing subscription topics and
    their corresponding Quality of Service (QoS) levels.

    Args:
        filename (str): Path to the file containing topic and QoS information.
                        The file format is expected to have one topic and QoS pair per line,
                        with each pair separated by a comma.

    Returns:
        list: A list of tuples containing (topic, qos) pairs.
              Each tuple represents a subscription with the topic name and its desired QoS level.
    """
    with open(filename, "r", encoding="utf8") as f:
        lines = f.readlines()[1:]
        lines = [line.strip() for line in lines]
        topics = [f"{SERVICE_TOPIC}/{line.split(",")[0]}" for line in lines]
        qos = [int(line.split(",")[1]) for line in lines]
        return list(zip(topics, qos))
    
def __on_message(client: mqtt.Client, userdata: any, msg: mqtt.MQTTMessage) -> None:
    """
    Callback function invoked when a message is received on a subscribed topic.

    This function is triggered whenever a message arrives on a topic that the client is subscribed to.
    It extracts the topic and payload from the message and delegates processing based on the received topic.

    Args:
        client (mqtt.Client): The MQTT client object that received the message.
        userdata (Any): Additional user data provided to the client on connection.
        msg (mqtt.MQTTMessage): The received MQTT message object. It contains information about the topic, payload, QoS level, and retain flag.
    """
    topic = msg.topic
    if topic == f"{SERVICE_TOPIC}/ping":
        __on_ping(msg)
    elif topic == f"{SERVICE_TOPIC}/Api":
        __on_api(client, msg)
    elif "Color/" in topic:
        __on_color(client, msg)

def __on_connect(client: mqtt.Client, userdata: any, flags: dict, reason_code: mqtt.CONNACK, properties: dict) -> None:
    """
    Callback function invoked when the connection attempt to the MQTT broker completes.

    This function handles the connection result.
    It checks the reason code to determine if the connection was successful.
    If successful, it retrieves subscription topics and QoS levels
    from the configuration file and subscribes to them.

    Args:
        client (mqtt.Client): The MQTT client object that attempted the connection.
        userdata (Any): Additional user data provided to the client on connection.
        flags (dict): Connection flags sent by the broker.
        reason_code (mqtt.Connack): The connection result code from the broker.
        properties (dict): Connection properties sent by the broker.
    """
    if reason_code.is_failure:
        print(f"Failed to connect: {reason_code}. loop_forever() will retry connection")
    else:
        sub_topics = __get_sub_topics("mqtt_sub_topics.txt")
        print("Trying to connect to topics: ", sub_topics)
        client.subscribe(sub_topics)

def __on_subscribe(client: mqtt.Client, userdata: any, mid: int, reason_code_list: list, properties: dict) -> None:
    """
    Callback function invoked when the client subscribes to topics.

    This function iterates through the list of reason codes received
    for each subscribed topic and prints messages based on success or failure.
    The `reason_code_list` contains a reason code object
    for each topic that the client attempted to subscribe to.

    Args:
        client (mqtt.Client): The MQTT client object that performed the subscription.
        userdata (Any): Additional user data provided to the client on connection.
        mid (int): The message identifier for the subscribe message sent to the broker.
        reason_code_list (list): A list of mqtt.Connack objects,
                                 one for each topic in the subscription request.
                                 Each object indicates the success or failure of the subscription for the corresponding topic.
        properties (dict): Subscription properties sent by the broker (optional).
    """
    for reason_code in reason_code_list:
        if reason_code.is_failure:
            print(f"Broker rejected you subscription: {reason_code}")
        else:
            print(f"Broker granted the following QoS: {reason_code.value}")

def __on_ping(msg: mqtt.MQTTMessage) -> None:
    """
    Processes a ping message received on the "GeoGlow/Friend-Service/ping" topic.

    This function handles incoming ping messages.
    It parses the JSON payload to extract the friend ID and device ID,
    then updates the timestamp of the corresponding device in the database.

    Args:
        msg (mqtt.MQTTMessage): The received MQTT message object. It contains the topic and payload.
                                The payload is expected to be a JSON string with keys "friendId" and "deviceId".
    """
    payload = json.loads(msg.payload.decode())

    db.received_controller_ping(payload)

# TODO: The request friendIds currently gets all friends, implement seperate commands for a single friend and all friends
def __on_api(client: mqtt.Client, msg: mqtt.MQTTMessage) -> None:
    """
    Processes an API request message received on the "GeoGlow/Friend-Service/api" topic.

    This function handles incoming API request messages.
    It parses the JSON payload to extract the friend ID and command.
    If the command is "requestFriendIDs", it retrieves the friend's data from the database
    and publishes the response back to the client.

    Args:
        client (mqtt.Client): The MQTT client object that received the message.
        msg (mqtt.MQTTMessage): The received MQTT message object. It contains the topic and payload.
                                The payload is expected to be a JSON string with keys "friendId" and "command".
    """
    payload = json.loads(msg.payload.decode())
    friendId = payload["friendId"]
    if payload["command"] == "requestFriendIDs":
        deviceIds = db.get_all_friends_data()
        client.publish(f"{SERVICE_TOPIC}/Api/{friendId}", json.dumps(deviceIds))

def __process_color_payload(payload: dict) -> dict:
    # TODO: Add further computation of payload here
            # (e.g. encryption of payload)
            # Afterwards return the processed payload
    return payload

def __on_color(client: mqtt.Client, msg: mqtt.MQTTMessage) -> None:
    sub_topics = msg.topic.split("/")
    deviceId = sub_topics[-1]
    friendId = sub_topics[-2]

    if db.find_friend(friendId) is None:
        print(f"Friend with friendId: {friendId} not found.")
        return
    
    payload = json.loads(msg.payload.decode())
    db.add_to_daily(friendId, deviceId, payload["color_palette"])
    processed_payload = __process_color_payload(payload)

    client.publish(f"{SERVICE_TOPIC}/{friendId}/{deviceId}", json.dumps(processed_payload))

def create_and_connect_client() -> mqtt.Client:
    """
    Creates and connects an MQTT client to the broker.

    This function initializes an MQTT client, sets up the callback functions for
    connection, message reception, and subscription handling, and connects the client to the broker.

    Returns:
        mqtt.Client: The initialized and connected MQTT client object.
    """
    mqtt_broker = os.environ["MQTT_BROKER_HOST"]
    mqtt_port = int(os.environ["MQTT_BROKER_PORT"])

    print(f"Connecting to MQTT broker: {mqtt_broker} on port: {mqtt_port}")

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.on_connect = __on_connect
    client.on_message = __on_message
    client.on_subscribe = __on_subscribe
    client.connect(mqtt_broker, mqtt_port)
    return client    
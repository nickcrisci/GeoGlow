import json
import paho.mqtt.client as mqtt
import pymongo

db_client = pymongo.MongoClient("mongodb://mongo:27017/")
db = db_client["friend_database"]
collection = db["friend_collection"]

with open("mqtt_config.txt", "r") as f:
    lines = f.readlines()
    mqtt_broker = lines[0].split("=")[1].strip()
    mqtt_port = int(lines[1].split("=")[1].strip())

def get_sub_topics(filename):
    """
    Reads subscription topics and QoS levels from a file.

    This function parses a configuration file containing subscription topics and
    their corresponding Quality of Service (QoS) levels.

    Parameters: 
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
        topics = [line.split(",")[0] for line in lines]
        qos = [int(line.split(",")[1]) for line in lines]
        return list(zip(topics, qos))

def on_message(client, userdata, msg):
    """
    Callback function invoked when a message is received on a subscribed topic.
    This function is triggered whenever a message arrives on a topic that the client is subscribed to.
    It extracts the topic and payload from the message and delegates processing based on the received topic.

    Parameters:
        client (mqtt.Client): The MQTT client object that received the message.
        userdata (Any): Additional user data provided to the client on connection.
        msg (mqtt.MQTTMessage): The received MQTT message object. It contains information about the topic, payload, QoS level, and retain flag.
    """
    topic = msg.topic
    payload = msg.payload.decode()
    if topic == "GeoGlow/Friend-Service/register":
        on_register(payload)

def on_register(payload_json):
    """
    Processes a registration message received on the "GeoGlow/Friend-Service/register" topic.

    This function handles incoming registration messages.
    It parses the JSON payload to extract the controller ID
    and checks if a document with that ID already exists in the database collection.
    If not, it inserts a new document.

    Parameters:
        payload_json (str): The JSON-encoded payload of the registration message.
        It is expected to contain a key "controller_id" with the unique identifier of the controller.
    """
    payload = json.loads(payload_json)
    controller_id = payload["controller_id"]
    data = {"controller_id": controller_id}
    existing_document = collection.find_one(filter=data)
    if not existing_document:
        result = collection.insert_one(data)
        print(f"Document inserted successfully! ID: {result.inserted_id}")
    else:
        print(f"Document with controller_id: {data['controller_id']} already exists!")

def on_connect(client, userdata, flags, reason_code, properties):
    """
    Callback function invoked when the connection attempt to the MQTT broker completes.

    This function handles the connection result.
    It checks the reason code to determine if the connection was successful.
    If successful, it retrieves subscription topics and QoS levels
    from the configuration file and subscribes to them.

    Parameters:
        client (mqtt.Client): The MQTT client object that attempted the connection.
        userdata (Any): Additional user data provided to the client on connection.
        flags (dict): Connection flags sent by the broker.
        reason_code (mqtt.Connack): The connection result code from the broker.
        properties (dict): Connection properties sent by the broker.
    """
    if reason_code.is_failure:
        print(f"Failed to connect: {reason_code}. loop_forever() will retry connection")
    else:
        sub_topics = get_sub_topics("mqtt_sub_topics.txt")
        print(sub_topics)
        client.subscribe(sub_topics)

def on_subscribe(client, userdata, mid, reason_code_list, properties):
    """
    Callback function invoked when the client subscribes to topics.

    This function iterates through the list of reason codes received
    for each subscribed topic and prints messages based on success or failure.
    The `reason_code_list` contains a reason code object
    for each topic that the client attempted to subscribe to.

    Parameters:
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

def main():
    print(f"Connecting to MQTT broker: {mqtt_broker} on port: {mqtt_port}")
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_subscribe = on_subscribe

    client.connect(mqtt_broker)
    client.loop_forever()

if __name__ == "__main__":
    main()
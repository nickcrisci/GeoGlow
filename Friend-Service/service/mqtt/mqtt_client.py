import os
import json
import paho.mqtt.client as mqtt
import database as db
from . import handler
from .mqtt_config import MQTT_BROKER_HOST, MQTT_BROKER_PORT

SERVICE_TOPIC = "GeoGlow/Friend-Service"

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)

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
        print("Connected successfully")
        sub_topics = __get_sub_topics("mqtt/mqtt_sub_topics.txt")
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

def connect_client():# -> mqtt.Client:
    """
    Creates and connects an MQTT client to the broker.

    This function initializes an MQTT client, sets up the callback functions for
    connection, message reception, and subscription handling, and connects the client to the broker.

    Returns:
        mqtt.Client: The initialized and connected MQTT client object.
    """
    print(f"Connecting to MQTT broker: {MQTT_BROKER_HOST} on port: {MQTT_BROKER_PORT}")

    client.on_connect = __on_connect
    client.on_subscribe = __on_subscribe
    client.message_callback_add(f"{SERVICE_TOPIC}/ping", handler.on_ping_callback)
    client.message_callback_add(f"{SERVICE_TOPIC}/Api", handler.on_api_callback)
    client.message_callback_add(f"{SERVICE_TOPIC}/Color/#", handler.on_color_callback)
    client.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT)
    return client

import json
import database as db
from paho.mqtt.client import MQTTMessage, Client
from .color_handler import *

SERVICE_TOPIC = "GeoGlow/Friend-Service"

def on_ping_callback(client: Client, userdata, msg: MQTTMessage) -> None:
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

def on_api_callback(client: Client, userdata, msg: MQTTMessage) -> None:
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
    command = payload["command"]
    friendId = payload["friendId"]
    if command == "requestDeviceIds":
        data = db.get_all_friends_data()
        client.publish(f"{SERVICE_TOPIC}/Api/{friendId}", json.dumps(data))
    if command == "postFriendId":
        db.register_friend(friendId, payload["name"])

def on_color_callback(client: Client, userdata, msg: MQTTMessage) -> None:
    sub_topics = msg.topic.split("/")
    deviceId = sub_topics[-1]
    friendId = sub_topics[-2]

    friend = db.find_friend(friendId)
    if friend is None:
        print(f"Friend with friendId: {friendId} not found.")
        return
    
    payload = json.loads(msg.payload.decode())
    color_tile_mapping = map_color_tiles(friendId, deviceId, payload["color_palette"])
    db.add_to_daily(friendId, deviceId, payload["color_palette"])
    processed_payload = process_color_payload(color_tile_mapping)
    
    client.publish(f"GeoGlow/{friendId}/{deviceId}/color", json.dumps(processed_payload))

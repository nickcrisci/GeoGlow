import json
from logger import logger
import database as db
from paho.mqtt.client import MQTTMessage, Client
from .color_handler import *

SERVICE_TOPIC = "GeoGlow/Friend-Service"
PAYLOAD_ARGUMENT_MISSING = "Payload was missing a required argument: %s"

def on_ping_callback(client: Client, _, msg: MQTTMessage) -> None:
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

    if "friendId" not in payload:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "friendId")
        return
    if "deviceId" not in payload:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "deviceId")
        return
    if "panelIds" not in payload:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "panelIds")
        return

    db.received_controller_ping(payload)

def on_api_callback(client: Client, _, msg: MQTTMessage) -> None:
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
    command = payload.get("command")
    if not command:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "command")
        return

    friend_id = payload.get("friendId")
    if not friend_id:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "friendId")
        return

    if command == "requestDeviceIds":
        data = db.get_all_friends_data()
        client.publish(f"{SERVICE_TOPIC}/Api/{friend_id}", json.dumps(data))
    if command == "postFriendId":
        friend_name = payload.get("name")
        if not friend_name:
            logger.error(PAYLOAD_ARGUMENT_MISSING, "name")
            return
        db.register_friend(friend_id, friend_name)
    else:
        logger.info("Unknown command")

def on_color_callback(client: Client, _, msg: MQTTMessage) -> None:
    try:
        sub_topics = msg.topic.split("Color/")[1].split("/")
        friend_id = sub_topics[0]
        device_id = sub_topics[1]
    except IndexError:
        logger.error("Topic was incorrectly formatted. Make sure it is: %s/Color/<friend_id>/<device_id>", {SERVICE_TOPIC})
        return

    friend = db.find_friend(friend_id)
    if friend is None:
        logger.error("Friend with friendId: %s not found.", {friend_id})
        return

    payload = json.loads(msg.payload.decode())
    color_palette = payload.get("color_palette")
    if not color_palette:
        logger.error(PAYLOAD_ARGUMENT_MISSING, "color_palette")
        return
    color_tile_mapping = map_color_tiles(friend_id, device_id, color_palette)
    db.add_to_daily(friend_id, device_id, color_palette)
    processed_payload = process_color_payload(color_tile_mapping)

    client.publish(f"GeoGlow/{friend_id}/{device_id}/color", json.dumps(processed_payload))

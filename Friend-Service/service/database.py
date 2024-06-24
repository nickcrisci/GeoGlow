from datetime import datetime, UTC
import pymongo

client = pymongo.MongoClient("mongodb://mongo:27017/")
client.drop_database("GeoGlow_db")

db = client["GeoGlow_db"]

# Setup friend and device collections
friend_col = db["friends"]
device_col = db["devices"]

# Setup index to expire documents after 5 minutes
device_col.create_index({"_timestamp": 1}, expireAfterSeconds = 5 * 60)
'''
dummy data
device_dummy_data = [
    { "friendId": "nick", "deviceId" : "123" },
    { "friendId": "nick", "deviceId" : "456" },
    { "friendId": "finn", "deviceId" : "456" }
]

friend_dummy_data = [
    { "name": "Nick", "friendId": "nick" },
    { "name": "Finn", "friendId": "finn" },
    { "name": "Katy", "friendId": "katy" }
]

for i in range(0, 3):
    device_col.insert_one(device_dummy_data[i])
    friend_col.insert_one(friend_dummy_data[i])
'''
def register_device(friendId, deviceId):
    """
    Registers a new friend by inserting a document into the collection.

    Args:
        controller_id (str): The unique identifier of the friend.

    Returns:
        Any: The insertion result object from pymongo.
    """
    data = {
        "friendId": friendId,
        "deviceId": deviceId,
        "_timestamp": datetime.now(UTC)
    }
    result = device_col.insert_one(data)
    print(f"Document inserted successfully! ID: {result.inserted_id}")

def received_controller_ping(payload):
    timestamp = datetime.now(UTC)
    update = {"$set": {"_timestamp": timestamp}}
    friendId, deviceId = payload["deviceId"], payload["friendId"]
    update_result = device_col.update_one({"deviceId": deviceId}, update)

    # First ping is a register
    if update_result.modified_count == 1:
        print(f"Updated timestamp of controller with ID '{deviceId}' successfully")
    else:
        register_device(friendId, deviceId)

def register_friend(friendId, name = "!Anon"):

    data = {
        "name": name,
        "friendId": friendId
    }

    # Upsert operation
    upsert_result = friend_col.update_one(
        {"friendId": friendId},
        {"$set": data},
        upsert=True
    )

    # Check the upsert result
    if upsert_result.matched_count == 0:
        print("New friend inserted:", data)
    else:
        print(f"Friend with ID {friendId} already exists, document updated.")

def get_friends_devices(friendId):
    devices = device_col.find({"friendId": friendId})
    friendDevices = [device["deviceId"] for device in devices]

    if len(friendDevices) == 0:
        return []
    return friendDevices

def get_friend_data(friendId):
    cursor = friend_col.find({"friendId": friendId})
    friend = None
    # If the friend with id friendId is not registered yet StopIteration will be thrown
    try:
        friend = cursor.next()
    except StopIteration:
        register_friend(friendId)
        friend = friend_col.find({"friendId": friendId}).next()
    finally:
        devices = get_friends_devices(friendId)
        friend["devices"] = devices
    return friend
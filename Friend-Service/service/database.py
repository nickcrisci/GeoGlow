from datetime import datetime, UTC
import pymongo

client = pymongo.MongoClient("mongodb://mongo:27017/")
client.drop_database("GeoGlow_db")

db = client["GeoGlow_db"]

# Setup friend and device collections
friend_col = db["friends"]
device_col = db["devices"]
daily_col = db["daily"]
# Setup index to expire documents after 5 minutes
device_col.create_index({"_timestamp": 1}, expireAfterSeconds = 5 * 60)

device_dummy_data = [
    { "friendId": "Anakin", "deviceId" : "123", "panelIds": [1,2,3]},
    { "friendId": "Ahsoka", "deviceId" : "456", "panelIds": [1,2, 3]},
    { "friendId": "Padme", "deviceId" : "456", "panelIds": [1,2,3]}
]

friend_dummy_data = [
    { "name": "Anakin", "friendId": "TestFriendId" },
    { "name": "Ahsoka", "friendId": "2609126b" },
    { "name": "Padme", "friendId": "katy" }
]

def register_device(payload: dict) -> None:
    """
    Registers a new device for a friend by inserting a document into the device collection.

    Args:
        friendId (str): The unique identifier of the friend.
        deviceId (str): The unique identifier of the device.

    Returns:
        None
    """
    data = {
        "friendId": payload["friendId"],
        "deviceId": payload["deviceId"],
        "panelIds": payload["panelIds"],
        "_timestamp": datetime.now(UTC)
    }

    result = device_col.insert_one(data)
    print(f"Document inserted successfully! ID: {result.inserted_id}")

def received_controller_ping(payload: dict) -> None:
    """
    Handles a ping received from a device by updating the timestamp or registering the device.

    Args:
        payload (dict): The payload containing the device and friend information.
            Expected keys: "deviceId", "friendId".

    Returns:
        None
    """
    timestamp = datetime.now(UTC)
    update = {"$set": {"_timestamp": timestamp}}
    deviceId = payload["deviceId"]
    update_result = device_col.update_one({"deviceId": deviceId}, update)

    # First ping is a register
    if update_result.modified_count == 1:
        print(f"Updated timestamp of controller with ID '{deviceId}' successfully")
    else:
        register_device(payload)

def register_friend(friendId: str, name: str = "!Anon") -> None:
    """
    Registers a new friend by inserting or updating a document in the friend collection.

    Args:
        friendId (str): The unique identifier of the friend.
        name (str, optional): The name of the friend. Defaults to "!Anon".

    Returns:
        None
    """
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

def _get_friends_devices(friendId: str) -> list:
    """
    Retrieves a list of device IDs associated with a given friend ID.

    Args:
        friendId (str): The unique identifier of the friend.

    Returns:
        list: A list of device IDs associated with the friend.
    """
    devices = device_col.find({"friendId": friendId})
    friendDevices = [device["deviceId"] for device in devices]

    if len(friendDevices) == 0:
        return []
    return friendDevices

def find_friend(friendId: str):
    cursor = friend_col.find({"friendId": friendId})
    try:
        return cursor.next()
    except StopIteration:
        return None
    
def find_device(friendId: str, deviceId: str):
    return device_col.find_one({"friendId": friendId, "deviceId": deviceId})

def get_friend_data(friendId: str) -> dict:
    """
    Retrieves the friend's data along with the associated devices.

    Args:
        friendId (str): The unique identifier of the friend.

    Returns:
        dict: A dictionary containing the friend's data and associated devices.
    """
    friend = friend_col.find_one({"friendId": friendId}, {'_id': False})
    if friend == None:
        register_friend(friendId)
        friend = friend_col.find_one({"friendId": friendId}, {'_id': False})
    
    devices = _get_friends_devices(friendId)
    friend["devices"] = devices
    return friend

def get_all_friends_data() -> list:
    friends = []
    cursor = friend_col.find({})
    for friend in cursor:
        friends.append(get_friend_data(friend["friendId"]))
    return friends

def add_to_daily(friendId: str, deviceId: str, colors) -> None:
    friend_daily = daily_col.find_one({"friendId": friendId, "deviceId": deviceId})
    if friend_daily:
        daily_col.update_one({"_id": friend_daily["_id"]}, {"$push": {"color_data": colors}})
        print("Updated friend with friendID: ", friendId)
    else:
        daily_col.insert_one({"friendId": friendId, "deviceId": deviceId, "color_data": [colors]})
        print("Inserted friend with friendID: ", friendId)

def get_friends_daily(friendId, deviceId):
    friends_daily = daily_col.find_one({"friendId": friendId, "deviceId": deviceId})
    if friends_daily:
        return friends_daily["color_data"]
    else: 
        return None
    
for i in range(0, 3):
    register_device(device_dummy_data[i])
    friend_col.insert_one(friend_dummy_data[i])
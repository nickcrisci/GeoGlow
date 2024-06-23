import time
import pymongo

client = pymongo.MongoClient("mongodb://mongo:27017/")
db = client["friend_database"]
collection = db["friend_collection"]

def register_friend(friendId, deviceId):
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
        "_timestamp": time.time() 
    }
    result = collection.insert_one(data)
    print(f"Document inserted successfully! ID: {result.inserted_id}")

def received_controller_ping(payload):
    timestamp = time.time()
    update = {"$set": {"timestamp": timestamp}}
    friendId, deviceId = payload["deviceId"], payload["friendId"]
    update_result = collection.update_one({"deviceId": deviceId}, update, upsert = True)  
    
    # First ping is a register
    if update_result.modified_count == 1:
        print(f"Updated timestamp of controller with ID '{deviceId}' successfully")
    else:
        register_friend(friendId, deviceId)

# TODO: Read the device ids from database
#   and return object
def get_device_ids():
    pass
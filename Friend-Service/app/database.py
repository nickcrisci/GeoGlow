import time
import pymongo

client = pymongo.MongoClient("mongodb://mongo:27017/")
db = client["friend_database"]
collection = db["friend_collection"]

def register_friend(controller_id):
    """
    Registers a new friend by inserting a document into the collection.

    Args:
        controller_id (str): The unique identifier of the friend.

    Returns:
        Any: The insertion result object from pymongo.
    """
    existing_document = collection.find_one(filter={"controller_id": controller_id})
    if not existing_document:
        result = collection.insert_one({"controller_id": controller_id, "timestamp": time.time()})
        print(f"Document inserted successfully! ID: {result.inserted_id}")
    else:
        print(f"Document with controller_id: {controller_id} already exists!")

def received_controller_ping(controller_id):
    timestamp = time.time()
    update = {"$set": {"timestamp": timestamp}}
    update_result = collection.update_one({"controller_id": controller_id}, update)    
    # If for some reason ping is received before register
    if update_result.modified_count == 1:
        print(f"Updated timestamp of controller with ID '{controller_id}' successfully")
    else:
        register_friend(controller_id)

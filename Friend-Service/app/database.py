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
        result = collection.insert_one({"controller_id": controller_id})
        print(f"Document inserted successfully! ID: {result.inserted_id}")
    else:
        print(f"Document with controller_id: {controller_id} already exists!")

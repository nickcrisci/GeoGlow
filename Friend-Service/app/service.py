from mqtt_client import create_and_connect_client

def main():
    client = create_and_connect_client()
    client.loop_forever()

if __name__ == "__main__":
    main()
    
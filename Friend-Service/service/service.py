from mqtt.mqtt_client import connect_client

def main():
    client = connect_client()
    client.loop_forever()

if __name__ == "__main__":
    main()

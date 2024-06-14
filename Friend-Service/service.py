import os
import paho.mqtt.client as mqtt

with open("mqtt_config.txt", "r") as f:
    lines = f.readlines()
    mqtt_broker = lines[0].split("=")[1].strip()
    mqtt_port = int(lines[1].split("=")[1].strip())

def on_message(client, userdata, msg):
    topic = msg.topic
    payload = msg.payload.decode()
    print(payload)

def connect_to_mqtt(broker, port):
    print(f"Connecting to MQTT broker: {broker} on port: {port}")
    client = mqtt.Client(callback_api_version = mqtt.CallbackAPIVersion.VERSION2)
    client.on_message = on_message
    client.connect(broker)
    client.subscribe("GeoGlow/#")
    client.loop_start()
    return client

def on_connect(client, userdata, flags, reason_code, properties):
    if flags.session_present:
        pass
    if reason_code == 0:
        print("Successfully established a connection")
    if reason_code > 0:
        print("Connection failed")

def main():
    client = connect_to_mqtt(mqtt_broker, mqtt_port)

    while True:
        pass

if __name__ == "__main__":
    main()
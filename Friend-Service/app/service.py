import paho.mqtt.client as mqtt

with open("mqtt_config.txt", "r") as f:
    lines = f.readlines()
    mqtt_broker = lines[0].split("=")[1].strip()
    mqtt_port = int(lines[1].split("=")[1].strip())

def get_sub_topics(filename):
    with open(filename, "r") as f:
        lines = f.readlines()[1:]
        lines = [line.strip() for line in lines]
        topics = [line.split(",")[0] for line in lines]
        qos = [int(line.split(",")[1]) for line in lines]
        return list(zip(topics, qos))

def on_message(client, userdata, msg):
    topic = msg.topic
    print(topic)

def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code.is_failure:
        print(f"Failed to connect: {reason_code}. loop_forever() will retry connection")
    else:
        sub_topics = get_sub_topics("mqtt_sub_topics.txt")
        print(sub_topics)
        client.subscribe(sub_topics)

def on_subscribe(client, userdata, mid, reason_code_list, properties):
    for reason_code in reason_code_list:
        if reason_code.is_failure:
            print(f"Broker rejected you subscription: {reason_code}")
        else:
            print(f"Broker granted the following QoS: {reason_code.value}")

def main():
    print(f"Connecting to MQTT broker: {mqtt_broker} on port: {mqtt_port}")
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_subscribe = on_subscribe

    client.connect(mqtt_broker)
    client.loop_forever()

if __name__ == "__main__":
    main()
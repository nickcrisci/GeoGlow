#include "MQTTClient.h"

std::vector<TopicAdapter*> MQTTClient::topicAdapters;

MQTTClient::MQTTClient(const char* mqttBroker, const int mqttPort, WiFiClient& wifiClient)
    : mqttBroker(mqttBroker), mqttPort(mqttPort), client(wifiClient) {
    client.setServer(mqttBroker, mqttPort);
    client.setCallback(MQTTClient::callback);
}

void MQTTClient::loop() {
    if (!client.connected()) {
        reconnect();
    }
    client.loop();
}

void MQTTClient::reconnect() {
    while (!client.connected()) {
        Serial.print("Attempting MQTT connection...");
        if (client.connect("ESP8266Client")) {
            Serial.println("connected");
            for (auto adapter : topicAdapters) {
                client.subscribe(adapter->getTopic());
            }
        } else {
            Serial.print("failed, rc=");
            Serial.print(client.state());
            Serial.println(" try again in 5 seconds");
            delay(5000);
        }
    }
}

void MQTTClient::addTopicAdapter(TopicAdapter* adapter) {
    topicAdapters.push_back(adapter);
    if (client.connected()) {
        client.subscribe(adapter->getTopic());
    }
}

void MQTTClient::callback(char* topic, byte* payload, unsigned int length) {
    for (auto adapter : topicAdapters) {
        if (strcmp(topic, adapter->getTopic()) == 0) {
            adapter->callback(topic, payload, length);
            return;
        }
    }
    Serial.print("Unhandled message [");
    Serial.print(topic);
    Serial.print("] ");
    for (unsigned int i = 0; i < length; i++) {
        Serial.print((char)payload[i]);
    }
    Serial.println();
}

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
        if (matches(adapter->getTopic(), topic)) {
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

bool MQTTClient::matches(const char* subscribedTopic, char* receivedTopic) {
    const char* wildCardPos = strchr(subscribedTopic, '#');
    if (wildCardPos != NULL) {
        // Check if the '#' is at the end of the subscribed topic
        if (wildCardPos[1] == '\0') {
            // Remove the '#' and the trailing slash (if present) from subscribedTopic
            size_t subscribedTopicLength = wildCardPos - subscribedTopic;
            if (subscribedTopicLength > 0 && subscribedTopic[subscribedTopicLength - 1] == '/') {
                subscribedTopicLength--;
            }
            // Compare the first subscribedTopicLength characters
            return strncmp(subscribedTopic, receivedTopic, subscribedTopicLength) == 0;
        }
        // If '#' is not at the end, it's not a valid subscription
        return false;
    }

    const char* plusPos = strchr(subscribedTopic, '+');
    if (plusPos != NULL) {
        // Check if the '+' is part of a complete level
        const char* slashPos = strchr(receivedTopic, '/');
        if (slashPos == NULL) {
            // If there's no next level, the match is valid
            return true;
        }
        // Compare until the slash
        return strncmp(subscribedTopic, receivedTopic, plusPos - subscribedTopic) == 0 &&
               strcmp(plusPos + 1, slashPos + 1) == 0;
    }

    // Regular topic matching
    return strcmp(subscribedTopic, receivedTopic) == 0;
}

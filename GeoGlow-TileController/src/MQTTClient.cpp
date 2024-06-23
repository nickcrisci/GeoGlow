#include "MQTTClient.h"

std::vector<TopicAdapter *> MQTTClient::topicAdapters;

MQTTClient::MQTTClient(const char *mqttBroker, const int mqttPort, WiFiClient &wifiClient)
    : mqttBroker(mqttBroker), mqttPort(mqttPort), client(wifiClient) {
    client.setServer(mqttBroker, mqttPort);
    client.setCallback(callback);
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
            for (const auto adapter: topicAdapters) {
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

void MQTTClient::publish(const char *topic, const JsonDocument &jsonPayload) {
    if (client.connected()) {
        char buffer[512];
        const size_t n = serializeJson(jsonPayload, buffer);
        client.publish(topic, buffer, n);
    } else {
        Serial.println("MQTT client not connected. Unable to publish message.");
    }
}

void MQTTClient::addTopicAdapter(TopicAdapter *adapter) {
    topicAdapters.push_back(adapter);
    if (client.connected()) {
        client.subscribe(adapter->getTopic());
    }
}

void MQTTClient::callback(char *topic, const byte *payload, unsigned int length) {
    char payloadBuffer[length + 1];
    memcpy(payloadBuffer, payload, length);
    payloadBuffer[length] = '\0';

    JsonDocument jsonDocument;

    if (deserializeJson(jsonDocument, payloadBuffer)) {
        Serial.print("Unhandled message [");
        Serial.print(topic);
        Serial.print("] ");
        Serial.println(payloadBuffer);
        return;
    }

    for (const auto adapter: topicAdapters) {
        if (matches(adapter->getTopic(), topic)) {
            adapter->callback(topic, jsonDocument.as<JsonObject>(), length);
            return;
        }
    }

    Serial.print("Unhandled message [");
    Serial.print(topic);
    Serial.print("] ");
    Serial.println(payloadBuffer);
}

bool MQTTClient::matches(const char *subscribedTopic, const char *receivedTopic) {
    if (const char *wildCardPos = strchr(subscribedTopic, '#'); wildCardPos != nullptr) {
        if (wildCardPos[1] == '\0') {
            size_t subscribedTopicLength = wildCardPos - subscribedTopic;
            if (subscribedTopicLength > 0 && subscribedTopic[subscribedTopicLength - 1] == '/') {
                subscribedTopicLength--;
            }
            return strncmp(subscribedTopic, receivedTopic, subscribedTopicLength) == 0;
        }
        return false;
    }

    const char *plusPos = strchr(subscribedTopic, '+');
    if (plusPos != nullptr) {
        const char *slashPos = strchr(receivedTopic, '/');
        if (slashPos == nullptr) {
            return true;
        }
        return strncmp(subscribedTopic, receivedTopic, plusPos - subscribedTopic) == 0 &&
               strcmp(plusPos + 1, slashPos + 1) == 0;
    }

    return strcmp(subscribedTopic, receivedTopic) == 0;
}

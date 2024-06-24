#ifndef MQTTCLIENT_H
#define MQTTCLIENT_H

#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <vector>
#include <ArduinoJson.h>
#include "TopicAdapter.h"

class MQTTClient {
public:
    explicit MQTTClient(WiFiClient &wifiClient);

    void setup(const char *mqttBroker, int mqttPort);

    void loop();

    void publish(const char *topic, const JsonDocument &jsonPayload);

    void addTopicAdapter(TopicAdapter *adapter);

private:
    void reconnect();

    static void callback(char *topic, const byte *payload, unsigned int length);

    static bool matches(const char *subscribedTopic, const char *receivedTopic);

    PubSubClient client;
    static std::vector<TopicAdapter *> topicAdapters;
};

#endif

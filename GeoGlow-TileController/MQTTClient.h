#ifndef MQTTCLIENT_H
#define MQTTCLIENT_H

#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <vector>
#include "TopicAdapter.h"

class MQTTClient {
public:
    MQTTClient(const char* mqttBroker, const int mqttPort, WiFiClient& wifiClient);
    void loop();
    void addTopicAdapter(TopicAdapter* adapter);

private:
    void reconnect();
    static void callback(char* topic, byte* payload, unsigned int length);

    const char* mqttBroker;
    const int mqttPort;
    PubSubClient client;
    static std::vector<TopicAdapter*> topicAdapters;
};

#endif
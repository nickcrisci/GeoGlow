#ifndef TESTADAPTER_H
#define TESTADAPTER_H

#include "TopicAdapter.h"

class TestAdapter : public TopicAdapter {
public:
    TestAdapter() {}

    const char* getTopic() const override {
        return "test/#";
    }

    void callback(char* topic, const JsonObject& payload, unsigned int length) override {
        int value = payload["value"];
        Serial.print("Received value from topic '");
        Serial.print(topic);
        Serial.print("': ");
        Serial.println(value);
    }

private:
    const char* topic;
};

#endif
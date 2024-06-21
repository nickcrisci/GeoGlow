#ifndef TESTADAPTER_H
#define TESTADAPTER_H

#include "TopicAdapter.h"

class TestAdapter : public TopicAdapter {
public:
    TestAdapter() {}

    const char* getTopic() const override {
        return "test/#";
    }

    void callback(char* topic, byte* payload, unsigned int length) override {
        Serial.print("Message arrived in TestAdapter [");
        Serial.print(topic);
        Serial.print("] ");
        for (unsigned int i = 0; i < length; i++) {
            Serial.print((char)payload[i]);
        }
        Serial.println();
    }

private:
    const char* topic;
};

#endif
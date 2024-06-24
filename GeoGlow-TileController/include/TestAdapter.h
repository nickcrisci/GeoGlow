#ifndef TESTADAPTER_H
#define TESTADAPTER_H

#include "TopicAdapter.h"

class TestAdapter final : public TopicAdapter
{
public:
    TestAdapter(): topic("test/#")
    {
    }

    [[nodiscard]] const char* getTopic() const override
    {
        return topic;
    }

    void callback(char* topic, const JsonObject& payload, unsigned int length) override
    {
        const int value = payload["value"];
        Serial.print("Received value from topic '");
        Serial.print(topic);
        Serial.print("': ");
        Serial.println(value);
    }

private:
    const char* topic;
};

#endif

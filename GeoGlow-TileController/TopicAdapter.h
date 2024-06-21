#ifndef TOPICADAPTER_H
#define TOPICADAPTER_H

#include <Arduino.h>

class TopicAdapter {
public:
    virtual const char* getTopic() const = 0;
    virtual void callback(char* topic, byte* payload, unsigned int length) = 0;
};

#endif

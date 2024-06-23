#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include "MQTTClient.h"
#include "NanoleafApiWrapper.h"
#include "config.h"

#include "TestAdapter.h"

WiFiClient wifiClient;
MQTTClient mqttClient(MQTT_BROKER, MQTT_PORT, wifiClient);
NanoleafApiWrapper nanoleaf;

TestAdapter testAdapter;

unsigned long lastPublishTime = 30000;

void setup() {
    Serial.begin(115200);
    delay(10);
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(WLAN_SSID);
    WiFi.begin(WLAN_SSID, WLAN_PASS);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    randomSeed(micros());

    Serial.println("");
    Serial.println("Now WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    mqttClient.addTopicAdapter(&testAdapter);

    nanoleaf.setPower(true);
    delay(5000);
    nanoleaf.setPower(false);
}

void loop() {
    mqttClient.loop();

    if (millis() - lastPublishTime >= 30000) {
        JsonDocument jsonPayload;
        jsonPayload["friendId"] = FRIEND_ID;
        jsonPayload["deviceId"] = DEVICE_ID;
        mqttClient.publish("GeoGlow/test", jsonPayload);

        lastPublishTime = millis();
    }
}

#include <ESP8266WiFi.h>
#include "MQTTClient.h"
#include "config.h"

#include "TestAdapter.h"

WiFiClient wifiClient;
MQTTClient mqttClient(MQTT_BROKER, MQTT_PORT, wifiClient);

TestAdapter testAdapter;

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
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    mqttClient.addTopicAdapter(&testAdapter);
}

void loop() {
    mqttClient.loop();
}
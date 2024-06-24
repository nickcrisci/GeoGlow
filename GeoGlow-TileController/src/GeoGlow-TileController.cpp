#include <Arduino.h>
#include <FS.h>
#include <ESP8266WiFi.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include <UUID.h>
#include "MQTTClient.h"
#include "NanoleafApiWrapper.h"

#include "TestAdapter.h"

WiFiManager wifiManager;
WiFiClient wifiClient;
MQTTClient mqttClient(wifiClient);
NanoleafApiWrapper nanoleaf(wifiClient);

TestAdapter testAdapter;

unsigned long lastPublishTime = 30000;

char mqttBroker[40];
char mqttPort[6] = "1883";
char nanoleafBaseUrl[55] = "";
char friendId[36] = "";
char deviceId[36] = "";

bool shouldSaveConfig = false;

void saveConfigCallback() {
    Serial.println("Should save config");
    shouldSaveConfig = true;
}

void setup() {
    Serial.begin(115200);
    delay(10);

    UUID uuid;
    strcpy(deviceId, uuid.toCharArray());

    if (SPIFFS.begin()) {
        Serial.println("mounted file system");
        if (SPIFFS.exists("/config.json")) {
            Serial.println("reading config file");
            File configFile = SPIFFS.open("/config.json", "r");
            if (configFile) {
                Serial.println("opened config file");
                size_t size = configFile.size();
                std::unique_ptr<char[]> buf(new char[size]);
                configFile.readBytes(buf.get(), size);

                JsonDocument jsonConfig;
                deserializeJson(jsonConfig, buf.get());
                Serial.println("parsed json");
                strcpy(deviceId, jsonConfig["deviceId"]);
                strcpy(mqttBroker, jsonConfig["mqttBroker"]);
                strcpy(mqttPort, jsonConfig["mqttPort"]);
                strcpy(nanoleafBaseUrl, jsonConfig["nanoleafBaseUrl"]);
                strcpy(friendId, jsonConfig["friendId"]);
            } else {
                Serial.println("failed to load json config");
            }
            configFile.close();
        }
    } else {
        Serial.println("failed to mount FS");
    }

    WiFiManagerParameter customMqttBroker("mqttBroker", "mqtt broker", mqttBroker, 40);
    WiFiManagerParameter customMqttPort("mqttPort", "mqtt port", mqttPort, 6);
    WiFiManagerParameter customNanoleafBaseUrl(
        "nanoleafBaseUrl",
        "http://<Nanoleaf-Base-Url[:<Port>]>",
        nanoleafBaseUrl,
        55
    );
    WiFiManagerParameter customFriendId("friendId", "<Friend-ID>", friendId, 36);

    wifiManager.setSaveConfigCallback(saveConfigCallback);
    wifiManager.addParameter(&customMqttBroker);
    wifiManager.addParameter(&customMqttPort);
    wifiManager.addParameter(&customNanoleafBaseUrl);
    wifiManager.addParameter(&customFriendId);

    if (!wifiManager.autoConnect("GeoGlow")) {
        Serial.println("failed to connect and hit timeout");
        delay(3000);
        EspClass::restart();
        delay(5000);
    }

    Serial.println("connected...yeey :)");
    strcpy(mqttBroker, customMqttBroker.getValue());
    strcpy(mqttPort, customMqttPort.getValue());
    strcpy(nanoleafBaseUrl, customNanoleafBaseUrl.getValue());
    strcpy(friendId, customFriendId.getValue());

    friendId[sizeof(friendId) - 1] = '\0';
    deviceId[sizeof(deviceId) - 1] = '\0';

    Serial.println("The values in the file are: ");
    Serial.println("\tmqttBroker : " + String(mqttBroker));
    Serial.println("\tmqttPort : " + String(mqttPort));
    Serial.println("\tnanoleafBaseUrl : " + String(nanoleafBaseUrl));
    Serial.println("\tfriendId : " + String(friendId));
    Serial.println("\tdeviceId : " + String(deviceId));

    if (shouldSaveConfig) {
        Serial.println("saving config");
        JsonDocument jsonConfig;
        jsonConfig["mqttBroker"] = mqttBroker;
        jsonConfig["mqttPort"] = mqttPort;
        jsonConfig["nanoleafBaseUrl"] = nanoleafBaseUrl;
        jsonConfig["friendId"] = friendId;
        jsonConfig["deviceId"] = deviceId;

        File configFile = SPIFFS.open("/config.json", "w");
        if (!configFile) {
            Serial.println("failed to open config file for writing");
        }

        serializeJson(jsonConfig, Serial);
        serializeJson(jsonConfig, configFile);
        configFile.close();
    }

    Serial.println("local ip");
    Serial.println(WiFi.localIP());
    mqttClient.setup(mqttBroker, String(mqttPort).toInt());
    mqttClient.addTopicAdapter(&testAdapter);

    nanoleaf.setup(nanoleafBaseUrl, "OmizkYRK3cMtYovrlb8rTvAdDtK2uZpu");
}

void loop() {
    mqttClient.loop();

    if (millis() - lastPublishTime >= 30000) {
        JsonDocument jsonPayload;
        jsonPayload["friendId"] = friendId;
        jsonPayload["deviceId"] = deviceId;
        mqttClient.publish("GeoGlow/ping", jsonPayload);

        lastPublishTime = millis();
    }
}

#include <Arduino.h>
#include <FS.h>
#include <ESP8266WiFi.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include "MQTTClient.h"
#include "NanoleafApiWrapper.h"

#include "TestAdapter.h"

WiFiManager wifiManager;
WiFiClient wifiClient;
MQTTClient mqttClient;
NanoleafApiWrapper nanoleaf;

TestAdapter testAdapter;

unsigned long lastPublishTime = 30000;

char mqttBroker[40];
char mqttPort[6] = "1883";
char nanoleafBaseUrl[55] = "http://<Nanoleaf-Base-Url[:<Port>]>";
char friendId[36] = "<Friend-ID>";

bool shouldSaveConfig = false;

void saveConfigCallback()
{
    Serial.println("Should save config");
    shouldSaveConfig = true;
}

void setup()
{
    Serial.begin(115200);
    delay(10);
    Serial.println();
    Serial.print("Connecting to ");
    /*
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
    */


    if (SPIFFS.begin())
    {
        Serial.println("mounted file system");
        if (SPIFFS.exists("/config.json"))
        {
            Serial.println("reading config file");
            File configFile = SPIFFS.open("/config.json", "r");
            if (configFile)
            {
                Serial.println("opened config file");
                size_t size = configFile.size();
                std::unique_ptr<char[]> buf(new char[size]);
                configFile.readBytes(buf.get(), size);

                JsonDocument jsonConfig;
                deserializeJson(jsonConfig, buf.get());
                Serial.println("\nparsed json");
                strcpy(mqttBroker, jsonConfig["mqttBroker"]);
                strcpy(mqttPort, jsonConfig["mqttPort"]);
                strcpy(nanoleafBaseUrl, jsonConfig["nanoleafBaseUrl"]);
                strcpy(friendId, jsonConfig["friendId"]);
                Serial.println("\nfilled in values");
            }
            else
            {
                Serial.println("failed to load json config");
            }
            configFile.close();
        }
    }
    else
    {
        Serial.println("failed to mount FS");
    }

    WiFiManagerParameter customMqttBroker("MQTT-Broker", "mqtt broker", mqttBroker, 40);
    WiFiManagerParameter customMqttPort("MQTT-Port", "mqtt port", mqttPort, 6);
    WiFiManagerParameter customNanoleafBaseUrl("Nanoleaf-Base-Url", "nanoleaf base url", nanoleafBaseUrl, 55);
    WiFiManagerParameter customFriendId("FriendId", "Friend-Id", friendId, 36);

    wifiManager.setSaveConfigCallback(saveConfigCallback);
    wifiManager.addParameter(&customMqttBroker);
    wifiManager.addParameter(&customMqttPort);
    wifiManager.addParameter(&customNanoleafBaseUrl);
    wifiManager.addParameter(&customFriendId);

    //reset settings - for testing
    //wifiManager.resetSettings();

    if (!wifiManager.autoConnect("AutoConnectAP", "password"))
    {
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
    Serial.println("The values in the file are: ");
    Serial.println("\tmqttBroker : " + String(mqttBroker));
    Serial.println("\tmqttPort : " + String(mqttPort));
    Serial.println("\tnanoleafBaseUrl : " + String(nanoleafBaseUrl));
    Serial.println("\tfriendId : " + String(friendId));

    if (shouldSaveConfig)
    {
        Serial.println("saving config");
        JsonDocument jsonConfig;
        jsonConfig["mqttBroker"] = mqttBroker;
        jsonConfig["mqttPort"] = mqttPort;
        jsonConfig["nanoleafBaseUrl"] = nanoleafBaseUrl;
        jsonConfig["friendId"] = friendId;

        File configFile = SPIFFS.open("/config.json", "w");
        if (!configFile)
        {
            Serial.println("failed to open config file for writing");
        }

        serializeJson(jsonConfig, Serial);
        serializeJson(jsonConfig, configFile);
        configFile.close();
    }

    Serial.println("local ip");
    Serial.println(WiFi.localIP());
    Serial.print("Setting up mqtt....");
    mqttClient.setup(mqttBroker, reinterpret_cast<int>(mqttPort));
    Serial.println("done;");
    mqttClient.addTopicAdapter(&testAdapter);

    nanoleaf.setup(nanoleafBaseUrl, "OmizkYRK3cMtYovrlb8rTvAdDtK2uZpu");
    nanoleaf.setPower(true);
    delay(1000);
    nanoleaf.setPower(false);
}

void loop()
{
    mqttClient.loop();

    if (millis() - lastPublishTime >= 30000)
    {
        JsonDocument jsonPayload;
        jsonPayload["friendId"] = friendId;
        jsonPayload["deviceId"] = "1be3cb4d-0d8d-4328-8d9d-a37a887f3a4c";
        mqttClient.publish("GeoGlow/test", jsonPayload);

        lastPublishTime = millis();
    }
}

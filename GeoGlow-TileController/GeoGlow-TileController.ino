#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "NanoleafApiWrapper.h"
#include "config.h"

WiFiClient wifiClient;
NanoleafApiWrapper nanoleaf;

PubSubClient client(wifiClient);

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  WiFi.mode(WIFI_STA);
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
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "TileController-";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      Serial.println("connected");

      // List of Topics to Subscribe
      client.subscribe("test");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  setup_wifi();
  client.setServer(MQTT_BROKER, MQTT_PORT);
  client.setCallback(callback);


}

void loop() {
  if (!client.connected()) {
    reconnect();
  }else{
    JsonDocument jsonPayload;

    jsonPayload["controller_id"] = DEVICE_ID;

    String stringPayload;

    serializeJson(jsonPayload, stringPayload);
    Serial.print("Data:");
    Serial.println(stringPayload);
    Serial.println("Sending data...");
    client.publish("GeoGlow/Friend-Service/register", stringPayload.c_str() );
    delay(5000);
  }
  client.loop();
}

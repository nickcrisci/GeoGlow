#include <ESP8266WiFi.h>
#include "NanoleafApiWrapper.h"
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"
#include "config.h"

WiFiClient client;
NanoleafApiWrapper nanoleaf;

Adafruit_MQTT_Client mqtt(&client, MQTT_BROKER, MQTT_PORT);

Adafruit_MQTT_Subscribe test = Adafruit_MQTT_Subscribe(&mqtt, "test");


void testCallback(char* x, uint16_t len) {
  Serial.print("Hey we're in a test callback, the test value is: ");
  //nanoleaf.postToTest("{}");
  Serial.println(x);
}

void setup() {
  Serial.begin(115200);
  delay(10);

  Serial.println(F("GeoGlow-TileController"));

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  test.setCallback(testCallback);

  mqtt.subscribe(&test);
}

void loop() {
  MQTT_connect();
  mqtt.processPackets(10000);
  if (!mqtt.ping()) {
    mqtt.disconnect();
  }
}

void MQTT_connect() {
  int8_t ret;

  if (mqtt.connected()) {
    return;
  }

  Serial.print("Connecting to MQTT...");

  uint8_t retries = 3;
  while ((ret = mqtt.connect()) != 0) {  // connect will return 0 for connected
    Serial.println(mqtt.connectErrorString(ret));
    Serial.println("Retrying MQTT connection in 10 seconds...");
    mqtt.disconnect();
    delay(10000);  // wait 10 seconds
    retries--;
    if (retries == 0) {
      // basically die and wait for WDT to reset me
      while (1)
        ;
    }
  }
  Serial.println("MQTT Connected!");
}

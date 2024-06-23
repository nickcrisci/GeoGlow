#ifndef NanoleafApiWrapper_h
#define NanoleafApiWrapper_h

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

class NanoleafApiWrapper {
  public:
    NanoleafApiWrapper(const String& nanoleafBaseUrl, WiFiClient& wifiClient);
    bool postToTest(const JsonDocument& jsonPayload);
    bool postToTest2(const JsonDocument& jsonPayload);

  private:
    bool postData(const String& endpoint, const JsonDocument& jsonPayload);
    String nanoleafBaseUrl;
    WiFiClient client;
};

#endif

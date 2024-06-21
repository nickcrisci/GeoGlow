#ifndef NanoleafApiWrapper_h
#define NanoleafApiWrapper_h

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include "config.h"

class NanoleafApiWrapper {
  private:
    String serverName;
    const String defaultServerName = NANOLEAF_BASE_URL; // Standard-URL

  public:
    NanoleafApiWrapper(const String& serverName = "");
    bool postToTest(const JsonDocument& jsonPayload);
    bool postToTest2(const JsonDocument& jsonPayload);

  private:
    bool postData(const String& endpoint, const JsonDocument& jsonPayload);
};

#endif

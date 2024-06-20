#ifndef NanoleafApiWrapper_h
#define NanoleafApiWrapper_h

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "config.h"

class NanoleafApiWrapper {
  private:
    String serverName;
    const String defaultServerName = NANOLEAF_BASE_URL; // Standard-URL

  public:
    NanoleafApiWrapper(const String& serverName = "");
    bool postToTest(const String& jsonPayload);
    bool postToTest2(const String& jsonPayload);

  private:
    bool postData(const String& endpoint, const String& jsonPayload);
};

#endif

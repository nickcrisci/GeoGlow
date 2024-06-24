#ifndef NanoleafApiWrapper_h
#define NanoleafApiWrapper_h

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

class NanoleafApiWrapper {
public:
    explicit NanoleafApiWrapper(const WiFiClient &wifiClient);

    void setup(const char *nanoleafBaseUrl, const char *nanoleafAuthToken);

    bool setPower(const bool &state);

private:
    bool getData(const String &endpoint, JsonDocument &jsonResponse);

    bool postData(const String &endpoint, const JsonDocument &jsonPayload);

    bool putData(const String &endpoint, const JsonDocument &jsonPayload);

    String nanoleafBaseUrl;
    String nanoleafAuthToken;
    WiFiClient client;
};

#endif

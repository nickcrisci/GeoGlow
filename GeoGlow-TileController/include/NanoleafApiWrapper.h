#ifndef NanoleafApiWrapper_h
#define NanoleafApiWrapper_h

#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

class NanoleafApiWrapper {
public:
    explicit NanoleafApiWrapper(const WiFiClient &wifiClient);

    void setup(const char *nanoleafBaseUrl, const char *nanoleafAuthToken);

    bool isConnected();

    String generateToken();

    bool setPower(const bool &state);

private:
    bool sendRequest(
        const String &method,
        const String &endpoint,
        const JsonDocument *requestBody,
        JsonDocument *responseBody,
        bool useAuthToken
    );

    String nanoleafBaseUrl;
    String nanoleafAuthToken;
    WiFiClient client;
};

#endif

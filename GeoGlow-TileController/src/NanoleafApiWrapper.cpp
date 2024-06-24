#include "NanoleafApiWrapper.h"

NanoleafApiWrapper::NanoleafApiWrapper(const WiFiClient &wifiClient)
    : client(wifiClient) {
}

void NanoleafApiWrapper::setup(const char *nanoleafBaseUrl, const char *nanoleafAuthToken) {
    this->nanoleafBaseUrl = nanoleafBaseUrl;
    this->nanoleafAuthToken = nanoleafAuthToken;
}


bool NanoleafApiWrapper::getData(const String &endpoint, JsonDocument &jsonResponse) {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        const String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

        http.begin(client, url);
        http.addHeader("Content-Type", "application/json");

        if (const int httpResponseCode = http.GET(); httpResponseCode > 0) {
            String response = http.getString();
            Serial.println(httpResponseCode);
            Serial.println(response);

            deserializeJson(jsonResponse, response);

            http.end();
            return true;
        } else {
            Serial.print("Error on sending GET: ");
            Serial.println(httpResponseCode);
            http.end();
            return false;
        }
    }
    Serial.println("WiFi Disconnected");
    return false;
}

bool NanoleafApiWrapper::postData(const String &endpoint, const JsonDocument &jsonPayload) {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        const String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

        http.begin(client, url);
        http.addHeader("Content-Type", "application/json");

        String stringPayload;
        serializeJson(jsonPayload, stringPayload);

        const int httpResponseCode = http.POST(stringPayload);

        if (httpResponseCode > 0) {
            const String response = http.getString();
            Serial.println(httpResponseCode);
            Serial.println(response);
            http.end();
            return true;
        }
        Serial.print("Error on sending POST: ");
        Serial.println(httpResponseCode);
        http.end();
        return false;
    }
    Serial.println("WiFi Disconnected");
    return false;
}

bool NanoleafApiWrapper::putData(const String &endpoint, const JsonDocument &jsonPayload) {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        const String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

        http.begin(client, url);
        http.addHeader("Content-Type", "application/json");

        String stringPayload;
        serializeJson(jsonPayload, stringPayload);

        if (const int httpResponseCode = http.PUT(stringPayload); httpResponseCode > 0) {
            const String response = http.getString();
            Serial.println(httpResponseCode);
            Serial.println(response);
            http.end();
            return true;
        } else {
            Serial.print("Error on sending PUT: ");
            Serial.println(httpResponseCode);
            http.end();
            return false;
        }
    }
    Serial.println("WiFi Disconnected");
    return false;
}

bool NanoleafApiWrapper::setPower(const bool &state) {
    JsonDocument jsonPayload;
    jsonPayload["on"].to<JsonObject>();
    jsonPayload["on"]["value"] = state;
    return putData("/state", jsonPayload);
}

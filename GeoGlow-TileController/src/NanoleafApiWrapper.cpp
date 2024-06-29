#include "NanoleafApiWrapper.h"

NanoleafApiWrapper::NanoleafApiWrapper(const WiFiClient &wifiClient)
    : client(wifiClient) {
}


void NanoleafApiWrapper::setup(const char *nanoleafBaseUrl, const char *nanoleafAuthToken) {
    this->nanoleafBaseUrl = nanoleafBaseUrl;
    this->nanoleafAuthToken = nanoleafAuthToken;
}


bool NanoleafApiWrapper::sendRequest(const String &method, const String &endpoint, const JsonDocument *requestBody,
                                     JsonDocument *responseBody, bool useAuthToken) {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        String url = nanoleafBaseUrl;

        if (useAuthToken) {
            url += "/api/v1/" + nanoleafAuthToken;
        } else {
            url += "/api/v1";
        }

        url += endpoint;

        http.begin(client, url);
        http.addHeader("Content-Type", "application/json");

        int httpResponseCode = -1;

        if (method.equalsIgnoreCase("GET")) {
            httpResponseCode = http.GET();
        } else if (method.equalsIgnoreCase("POST")) {
            if (requestBody != nullptr) {
                String stringPayload;
                serializeJson(*requestBody, stringPayload);
                httpResponseCode = http.POST(stringPayload);
            } else {
                httpResponseCode = http.POST("");
            }
        } else if (method.equalsIgnoreCase("PUT")) {
            if (requestBody != nullptr) {
                String stringPayload;
                serializeJson(*requestBody, stringPayload);
                httpResponseCode = http.PUT(stringPayload);
            } else {
                httpResponseCode = http.PUT("");
            }
        }

        if (httpResponseCode > 0) {
            String response = http.getString();

            if (responseBody != nullptr) {
                deserializeJson(*responseBody, response);
            }

            http.end();
            return true;
        }
        Serial.print("Error on sending ");
        Serial.print(method);
        Serial.print(": ");
        Serial.println(httpResponseCode);
        http.end();
        return false;
    }
    Serial.println("WiFi Disconnected");
    return false;
}


bool NanoleafApiWrapper::isConnected() {
    JsonDocument jsonResponse;
    if (sendRequest("GET", "/", nullptr, &jsonResponse, true)) {
        if (jsonResponse["serialNo"] != nullptr) {
            return true;
        }
    }
    return false;
}


String NanoleafApiWrapper::generateToken() {
    JsonDocument jsonResponse;
    if (sendRequest("POST", "/new", nullptr, &jsonResponse, false)) {
        if (const String strPayload = jsonResponse["auth_token"]; strPayload != nullptr && strPayload != "null") {
            return strPayload;
        }
    }
    return "";
}


bool NanoleafApiWrapper::setPower(const bool &state) {
    JsonDocument jsonPayload;
    jsonPayload["on"].to<JsonObject>();
    jsonPayload["on"]["value"] = state;
    return sendRequest("PUT", "/state", &jsonPayload, nullptr, true);
}

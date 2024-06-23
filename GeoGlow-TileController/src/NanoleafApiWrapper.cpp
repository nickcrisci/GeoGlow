#include "NanoleafApiWrapper.h"

NanoleafApiWrapper::NanoleafApiWrapper() {
    nanoleafBaseUrl = NANOLEAF_BASE_URL;
    nanoleafAuthToken = NANOLEAF_AUTH_TOKEN;

    WiFiClient wifiClient;
    client = wifiClient;
}

bool NanoleafApiWrapper::getData(const String& endpoint, JsonDocument& jsonResponse) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.GET();

    if (httpResponseCode > 0) {
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
  } else {
    Serial.println("WiFi Disconnected");
    return false;
  }
}

bool NanoleafApiWrapper::postData(const String& endpoint, const JsonDocument& jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");

    String stringPayload;
    serializeJson(jsonPayload, stringPayload);

    int httpResponseCode = http.POST(stringPayload);

    if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.println(httpResponseCode);
      Serial.println(response);
      http.end();
      return true;
    } else {
      Serial.print("Error on sending POST: ");
      Serial.println(httpResponseCode);
      http.end();
      return false;
    }
  } else {
    Serial.println("WiFi Disconnected");
    return false;
  }
}

bool NanoleafApiWrapper::putData(const String& endpoint, const JsonDocument& jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = nanoleafBaseUrl + "/api/v1/" + nanoleafAuthToken + endpoint;

    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");

    String stringPayload;
    serializeJson(jsonPayload, stringPayload);

    int httpResponseCode = http.PUT(stringPayload);

    if (httpResponseCode > 0) {
      String response = http.getString();
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
  } else {
    Serial.println("WiFi Disconnected");
    return false;
  }
}

bool NanoleafApiWrapper::setPower(const bool& state) {
  JsonDocument jsonPayload;

  jsonPayload["on"].to<JsonObject>();
  jsonPayload["on"]["value"] = state;


  //JsonObject on  = jsonPayload.createNestedObject("on");
  //on["value"] = state;
  return putData( "/state", jsonPayload);
}
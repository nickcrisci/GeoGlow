#include "NanoleafApiWrapper.h"

NanoleafApiWrapper::NanoleafApiWrapper(const String& nanoleafBaseUrl, WiFiClient& wifiClient) : nanoleafBaseUrl(nanoleafBaseUrl), client(wifiClient) {}

bool NanoleafApiWrapper::postToTest(const JsonDocument& jsonPayload) {
  return postData("/test", jsonPayload);
}

bool NanoleafApiWrapper::postToTest2(const JsonDocument& jsonPayload) {
  return postData("/test2", jsonPayload);
}

bool NanoleafApiWrapper::postData(const String& endpoint, const JsonDocument& jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = nanoleafBaseUrl + endpoint;

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

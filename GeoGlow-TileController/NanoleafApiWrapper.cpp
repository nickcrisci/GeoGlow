#include "NanoleafApiWrapper.h"

NanoleafApiWrapper::NanoleafApiWrapper(const String& serverName) {
  if (serverName == "") {
    this->serverName = defaultServerName; // Verwende die Standard-URL
  } else {
    this->serverName = serverName; // Verwende die angegebene URL
  }
  
  // Extrahiere Hostname und Port
  int index = this->serverName.indexOf(':', 7); // Suche nach Port, nach "http://"
  String hostname = this->serverName.substring(7, index);
  int port = this->serverName.substring(index + 1).toInt();
  
  httpClient = new HttpClient(wifiClient, hostname, port);
}

bool NanoleafApiWrapper::postToTest(const String& jsonPayload) {
  return postData("/test", jsonPayload);
}

bool NanoleafApiWrapper::postToTest2(const String& jsonPayload) {
  return postData("/test2", jsonPayload);
}

bool NanoleafApiWrapper::postData(const String& endpoint, const String& jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    httpClient->beginRequest();
    httpClient->post(endpoint);
    httpClient->sendHeader("Content-Type", "application/json");
    httpClient->sendHeader("Content-Length", jsonPayload.length());
    httpClient->beginBody();
    httpClient->print(jsonPayload);
    httpClient->endRequest();

    int statusCode = httpClient->responseStatusCode();
    String response = httpClient->responseBody();

    Serial.println(statusCode); // HTTP Response code
    Serial.println(response);   // HTTP Response payload

    return (statusCode > 0);
  } else {
    Serial.println("WiFi Disconnected");
    return false;
  }
}

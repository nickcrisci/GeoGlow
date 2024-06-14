# Friend-Service

Dieser Service ist dafür verantwortlich zu verwalten, welche Freunde und Geräte erreichbar sind. Sobald die Geräte eingeschaltet werden teilen diese das per MQTT mit. 
Der Service sieht die entsprechenden Nachrichten und hinterlegt die notwendigen Informationen.

Des Weiteren empfängt der Friend-Service einen regelmäßigen Ping von den Controllern. So wird sichergestellt, das auch nur die Verbindung zu Geräten, die auch tatsächlich online sind, hergestellt wird. 

In einer zukünftigen Version dieses Services ist es vorstellbar, dass hier auch entsprechende Berechtigungen verwaltet werden, um sicherzustellen, dass nur berechtigte Freunde sich mit den entsprechenden Geräten verbinden dürfen.

## Ausführen des Images
Damit der Service auch vernünftig ausgeführt werden kann muss eine `mqtt_config.txt` Datei hinzugefügt werden. Diese muss sich auf dem selben Level wie die `Dockerfile` befinden und folgenden Inhalt haben:
```txt
MQTT_BROKER_HOST=<your_mqtt_broker_host>
MQTT_BROKER_PORT=<your_mqtt_broker_port>
```
`<your_mqtt_broker_host>` und `<your_mqtt_broker_port>` müssen entsprechend ausgetauscht werden.  
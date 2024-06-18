# Friend Service
This service is responsible for managing which friends and devices are available. As soon as the devices are turned on, they share this information via MQTT. The service sees the corresponding messages and stores the necessary information.

In addition, the Friend Service receives a regular ping from the controllers. This ensures that only connections to devices that are actually online are established.

In a future version of this service, it is conceivable that appropriate permissions will also be managed here to ensure that only authorized friends are allowed to connect to the corresponding devices.

## Starting the service
For the service to be able to start the mqtt details have to be specified. To do this simply add a file named `mqtt_config.txt` to the app directory.
This file should have the following content:
```txt
MQTT_BROKER_HOST=<your_mqtt_broker_host>
MQTT_BROKER_PORT=<your_mqtt_broker_port>
```
`<your_mqtt_broker_host>` and `<your_mqtt_broker_port>` have to be specified accordingly.  

After adding this file simply open up a terminal and use `docker compose build` followed by `docker compose up` to start the service.
package com.example.geoglow

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient


class MqttClient(private val context: Context) {

    companion object {
        private const val TAG = "MQTTClient"
        private const val HOST = "hivemq.dock.moxd.io"
        private const val PORT = 1883
        private const val SERVICE = "GeoGlowService"
    }

    private var mqttClient: Mqtt3AsyncClient = MqttClient.builder()
        .automaticReconnectWithDefaultConfig()
        .useMqttVersion3()
        .identifier("androidClient")
        .serverHost(HOST)
        .serverPort(PORT)
        //.sslWithDefaultConfig()
        .buildAsync()

    fun connect() {
        mqttClient.connectWith()
            //.simpleAuth()
            //.username("")
            //.password("".toByteArray())
            //.applySimpleAuth()
            .send()
            .whenComplete { connAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt connection failed: ${throwable.message}")
                } else {
                    Log.i(TAG, "Mqtt connection established: ${connAck.returnCode}")
                }
            }
    }

    fun disconnect() {
        mqttClient.disconnect()
            .whenComplete { unConnAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt disconnect failed: ${throwable.message}")
                } else {
                    Log.i(TAG, "Mqtt disconnect successful: $unConnAck")
                }
            }
    }

    fun subscribe(uniqueId: String) {
        val subTopic = "GeoGlow/$SERVICE/Api/$uniqueId"

        mqttClient.subscribeWith()
            .topicFilter(subTopic)
            .callback { publish ->
                val payload = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                val friendList = jsonStringToList(payload)
                SharedPreferencesHelper.setFriendList(context, friendList)
                Log.i(TAG,"Mqtt subscription payload: $friendList")
            }
            .send()
            .whenComplete { subAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt subscription failed: ${throwable.message}")
                } else {
                    Log.i(TAG, "Mqtt subscription successful: ${subAck.returnCodes}")
                }
            }
    }

    fun unsubscribe(topic: String) {
        mqttClient.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete { unSubAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt cancelling subscription failed: ${throwable.message}")
                } else {
                    Log.i(TAG, "Mqtt subscription successfully canceled: $unSubAck")
                }
            }
    }

    fun publish(uniqueId: String, demand: Boolean) {
        val pubTopic: String
        val jsonPayload = JSONObject()

        if (demand) {
            pubTopic = "GeoGlow/$SERVICE/Api/$uniqueId"
            jsonPayload.put("command", "RequestFriendIDs")
        } else {
            pubTopic = "GeoGlow/$SERVICE/Api"
            jsonPayload.put("command", "PostFriendID")
            jsonPayload.put("friendId", uniqueId)
        }

        mqttClient.publishWith()
            .topic(pubTopic)
            .payload(jsonPayload.toString().toByteArray())
            .send()
            .whenComplete { publish, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt publish failed: ${throwable.message}")
                } else {
                    val decodedPayload = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                    Log.i(TAG, "Mqtt publish successful: $decodedPayload")
                }
            }
    }

    fun publish(uniqueId: String, friendId: String, payload: List<Array<Int>>) {
        val pubTopic = "GeoGlow/$friendId/$uniqueId"
        val jsonPayload = transformListToJson("color palette", payload)

        mqttClient.publishWith()
            .topic(pubTopic)
            .payload(jsonPayload.toString().toByteArray())
            .send()
            .whenComplete { publish, throwable ->
                if (throwable != null) {
                    Log.e(TAG,"Mqtt publish failed: ${throwable.message}")
                } else {
                    val decodedPayload = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                    Log.i(TAG, "Mqtt publish successful: $decodedPayload")
                }
            }
    }
}
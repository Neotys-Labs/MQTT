/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the names of its contributors may be
 *       used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.neotys.action.mqtt.util;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;

/**
 *
 * Wrap a Paho mqttclient and provides internal housekeeping of subscription queues
 *
 * Created by smader on 23/11/16.
 */
public class MqttClientWrapper {
    private MqttClient mqttClient;
    private String brokerAlias;
    private Map<String, Queue<MqttMessage>> subscriptionQueues;

    /**
     * Construct a wrapper on the specified paho client
     * @param mqttClient
     */
    public MqttClientWrapper(final MqttClient mqttClient, final String brokerAlias) {
        this.mqttClient = mqttClient;
        this.brokerAlias = brokerAlias;
    }

    /**
     * Get the paho client
     * @return the paho client
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Get the broker alias as defined by user
     * @return
     */
    public String getBrokerAlias() {
        return brokerAlias;
    }

    public String getExecutorAlias() {
        return brokerAlias + ".executor";
    }

    /**
     * Is the wrapped mqtt client connected?
     * @return true if connected and false otherwise
     */
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    /**
     * Get the server URI of the wrapped mqtt client
     * @return the server URI of the wrapped mqtt client
     */
    public String getServerURI() {
        return mqttClient.getServerURI();
    }

    /**
     * Get the queue of mqtt messages for the specified topic and null if none found
     * @param topic
     * @return
     */
    public Queue<MqttMessage> getQueueForTopic(final String topic) {
        return getSubscriptionQueues().get(topic);
    }

    /**
     * Register the specified queue of mqtt messages on the specified topic
     * @param topic
     * @param subscriptionQueue
     * @return the queue
     */
    public Queue<MqttMessage> registerQueueForTopic(final String topic, final Queue<MqttMessage> subscriptionQueue) {
        return getSubscriptionQueues().put(topic, subscriptionQueue);
    }

    /**
     * Remove queue for specified topic
     * @param topic
     * @return true if queue for topic found and false otherwise
     */
    public boolean removeQueueForTopic(final String topic) {
        return getSubscriptionQueues().remove(topic) != null;
    }

    /**
     * Get - lazy instantiate - subscriptionQueues
     * @return
     */
    private Map<String, Queue<MqttMessage>> getSubscriptionQueues() {
        if (subscriptionQueues == null) {
            subscriptionQueues = new Hashtable<String, Queue<MqttMessage>>();
        }
        return subscriptionQueues;
    }

    @Override
    public String toString() {
        return "MQTTConnection(brokerAlias(" + brokerAlias +
                "), serverURI(" + mqttClient.getServerURI() +
                "), clientId(" + mqttClient.getClientId() + "))";
    }
}

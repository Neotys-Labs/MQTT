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
package com.neotys.action.mqtt.subscribe;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Callback used by the subscribe advanced action: merely queues the receivedmqtt  message on the internal queue it
 * was constructed with.
 */
public class QueueReceivedMessages implements MqttCallback {
    private LinkedBlockingQueue<MqttMessage> queue;

    /**
     * Build the callback with queue on which incoming messages will be queued
     */
	public QueueReceivedMessages() {
        this.queue = new LinkedBlockingQueue<MqttMessage>();
	}

    /**
     * Get the queue of incoming mqqt messages
     * @return
     */
    public Queue<MqttMessage> getQueue() {
        return queue;
    }

    @Override
	public void connectionLost(Throwable cause) {
		// TODO at least log
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) { /* NOOP */ }

	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        // queue incoming message
		queue.offer(mqttMessage);
	}
}

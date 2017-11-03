package com.neotys.mqtt.mqttwebsocket;


import static com.neotys.extensions.codec.predicates.MorePredicates.isRequestEntity;
import static com.neotys.extensions.codec.predicates.MorePredicates.isResponseEntity;
import static com.neotys.extensions.codec.predicates.MorePredicates.isWebSocketEntity;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.instanceOf;

import com.neotys.extensions.codec.AbstractBinder;

public class mqttbinder extends AbstractBinder {

	@Override
	protected void configure() {
		whenEntity(and(isWebSocketEntity(),isResponseEntity())).decodeWith(MqttDecoder.class);
		whenEntity(and(isWebSocketEntity(),isRequestEntity())).decodeWith(MqttDecoder.class);
		whenObject(instanceOf(MqttNLMessage.class)).encodeWith(mqttEncoders.class);
	}

}

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

import com.google.common.net.InternetDomainName;
import com.neotys.action.argument.ArgumentValidator;
import com.google.common.net.HostSpecifier;

import java.text.ParseException;

/**
 * Validates parameters related to MQTT advanced actions
 *
 * Created by smader on 03/11/16.
 */

public enum MqttArgumentValidator implements ArgumentValidator {
    PROTOCOL_ARGUMENT_VALIDATOR("Unsupported MQTT protocol: supported protocols are 'tcp' or 'ssl'.") {
        @Override
        public boolean apply(final String input) {
            if (input == null) return false;

            final String toLower = input.toLowerCase();
            return toLower.equals("tcp") || toLower.equals("ssl");
        }
    },
    HOST_ARGUMENT_VALIDATOR("Invalid host.") {
        @Override
        public boolean apply(final String input) {
            if (input == null) return false;
            try {
                HostSpecifier.from(input);
                return true;
            } catch(ParseException pe) {
                try {
                    InternetDomainName.from(input);
                    return true;
                }
                catch(IllegalArgumentException iae) {
                    return false;
                }
            }
        }
    },
    QOS_ARGUMENT_VALIDATOR("Illegal MQTT QoS value: legal values are 0, 1 or 2.") {
        @Override
        public boolean apply(final String input) {
            if (input == null) return false;
            try {
                int qos = Integer.parseInt(input);
                return qos >= 0 && qos <= 2;
            } catch(NumberFormatException nfe) {
                return false;
            }
        }
    };

    /** The error message associated with the validator. */
    private final String errorMessage;

    MqttArgumentValidator(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** @return the errorMessage */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}





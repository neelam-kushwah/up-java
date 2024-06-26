/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;

public class RpcServerExample {

    private static UUri methodUri = UUri.newBuilder()
        .setAuthorityName("Hartley").setUeId(1).setUeVersionMajor(1)
        .setResourceId(1).build();

    @Test
    @DisplayName("Test APIs where processing the request is successfully")
    public void test_HappyPath() {
        // Create the test transport
        final TestUTransport transport = new TestUTransport();

        // Register a listener to handle incoming client requests for a given method
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };

        // Register the Request handling listener
        RpcServer server = UPClient.create(transport);
        assertEquals(server.registerRequestHandler(methodUri, handler), UCode.OK);

        // Now unregister the request listener (we are done serving the method)
        assertEquals(server.unregisterRequestHandler(methodUri, handler), UCode.OK);
    }

    @Test
    @DisplayName("Test APIs where processing the request is successfully")
    public void test_UnHappyPath() {
        // Create the test transport
        final TestUTransport transport = new TestUTransport();

        // Register a listener to handle incoming client requests for a given method
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UStatusException(UCode.FAILED_PRECONDITION, "Failed to process the request");
            }
        };

        // Register the Request handling listener
        RpcServer server = UPClient.create(transport);

        assertEquals(server.registerRequestHandler(methodUri, handler), UCode.OK);

        // Now unregister the request listener (we are done serving the method)
        assertEquals(server.unregisterRequestHandler(methodUri, handler), UCode.OK);
    }
}

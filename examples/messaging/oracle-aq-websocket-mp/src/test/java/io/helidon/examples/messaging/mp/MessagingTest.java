/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.messaging.mp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.helidon.messaging.connectors.aq.AqMessage;
import io.helidon.messaging.connectors.mock.MockConnector;
import io.helidon.messaging.connectors.mock.TestConnector;
import io.helidon.microprofile.testing.junit5.AddConfigBlock;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@HelidonTest
@AddConfigBlock(type = "yaml", value = """
        config_ordinal: 205
        
        mp.messaging:
          outgoing:
            to-queue-1:
              connector: helidon-mock
              destination: EXAMPLE_QUEUE_1
            to-queue-2:
              connector: helidon-mock
              destination: EXAMPLE_QUEUE_2
        
          incoming:
            from-queue-1:
              connector: helidon-mock
              destination: EXAMPLE_QUEUE_1
            from-queue-2:
              connector: helidon-mock
              destination: EXAMPLE_QUEUE_2
            from-byte-queue:
              connector: helidon-mock
              destination: example_queue_bytes
            from-map-queue:
              connector: helidon-mock
              destination: example_queue_map
        """)
class MessagingTest {

    @Inject
    @TestConnector
    MockConnector mockConnector;

    @Inject
    MsgProcessingBean msgProcessingBean;

    @Test
    @SuppressWarnings("unchecked")
    void testMessage() throws ExecutionException, InterruptedException, TimeoutException {
        // Test channel to-queue-1
        msgProcessingBean.process("Test message");
        mockConnector.outgoing("to-queue-1", String.class)
                .request(1)
                .assertPayloads("Test message");

        // Test channel from-queue-2
        CompletableFuture<String> future = new CompletableFuture<>();
        msgProcessingBean.subscribeMulti().log().forEach(future::complete);

        AqMessage<String> aqMessage = (AqMessage<String>) mock(AqMessage.class);
        Mockito.when(aqMessage.getPayload()).thenReturn("Test message 2");

        mockConnector.incoming("from-queue-2", String.class).emit(aqMessage);
        assertThat(future.get(15, TimeUnit.SECONDS), is("Test message 2"));
    }
}

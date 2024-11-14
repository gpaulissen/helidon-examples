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

package io.helidon.examples.integrations.oci.genai;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.config.Config;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.Region;

/**
 * JAX-RS resource - REST API for the Gen AI example.
 */
public class GenAiService implements HttpService {
    private static final Logger LOGGER = Logger.getLogger(GenAiService.class.getName());

    private final GenerativeAiInferenceClient generativeAiInferenceClient;
    private final Config config;
    private String COMPARTMENT_ID;
    private String MODEL_ID;
    private static final String USER_MESSAGE_PARAM = "userMessage" ;

    GenAiService(GenerativeAiInferenceClient generativeAiInferenceClient,
                  Config config) {
        this.generativeAiInferenceClient = generativeAiInferenceClient;
        this.config = config;
        generativeAiInferenceClient.setRegion(Region.valueOf(config.get("oci.genai.region").asString().get()));
        this.COMPARTMENT_ID = config.get("oci.genai.compartment_id").asString().get();
        this.MODEL_ID = config.get("oci.genai.model_id").asString().get();
    }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/chat/{" + USER_MESSAGE_PARAM + "}", this::chatModelAsk);
    }

    public void chatModelAsk(ServerRequest req, ServerResponse res) {
        String userMessage = req.path().pathParameters().get("userMessage");
        LOGGER.log(Level.INFO, "UserMessage is: "  + userMessage);
        ChatContent content = TextContent.builder()
                .text(userMessage)
                .build();
        List<ChatContent> contents = new ArrayList<>();
        contents.add(content);
        Message message = UserMessage.builder()
                .content(contents)
                .build();
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        GenericChatRequest chatRequest = GenericChatRequest.builder()
                .messages(messages)
                .isStream(false)
                .build();
        ChatDetails details = ChatDetails.builder()
                .servingMode(OnDemandServingMode.builder().modelId(MODEL_ID).build())
                .compartmentId(COMPARTMENT_ID)
                .chatRequest(chatRequest)
                .build();
        ChatRequest request = ChatRequest.builder()
                .chatDetails(details)
                .build();
        ChatResponse response = generativeAiInferenceClient.chat(request);
        ChatResult chatResult = response.getChatResult();
        LOGGER.log(Level.INFO, "Chat Result is: "  + chatResult.toString());
        res.send(chatResult.toString());
    }
}


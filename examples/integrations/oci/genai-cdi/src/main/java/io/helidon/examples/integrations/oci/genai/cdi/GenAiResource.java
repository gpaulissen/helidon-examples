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

package io.helidon.examples.integrations.oci.genai.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * JAX-RS resource - REST API for the Gen AI example.
 */
@Path("/genai")
public class GenAiResource {
    private static final Logger LOGGER = Logger.getLogger(GenAiResource.class.getName());

    private final GenerativeAiInferenceClient generativeAiInferenceClient;

    @Inject
    @ConfigProperty(name="oci.genai.compartment.id")
    private String COMPARTMENT_ID;

    @Inject
    @ConfigProperty(name="oci.genai.model.id")
    private String MODEL_ID;

    @Inject
    GenAiResource(GenerativeAiInferenceClient generativeAiInferenceClient,
                  @ConfigProperty(name = "oci.genai.region") String region) {
        this.generativeAiInferenceClient = generativeAiInferenceClient;
        generativeAiInferenceClient.setRegion(Region.valueOf(region));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chat")
    public String chatModelAsk(@QueryParam("userMessage") String userMessage) {
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
        return chatResult.toString();
    }
}


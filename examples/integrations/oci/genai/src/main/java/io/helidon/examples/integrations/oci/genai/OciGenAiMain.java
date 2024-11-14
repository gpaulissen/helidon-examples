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

import java.io.IOException;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
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
import com.oracle.bmc.model.BmcException;

/**
 * Main class of the example.
 * This example sets up a web server to serve REST API that integrates with OCI GenAI Service.
 */
public final class OciGenAiMain {
    /**
     * Cannot be instantiated.
     */
    private OciGenAiMain() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) throws IOException {
        // load logging configuration
        LogConfig.configureRuntime();

        // By default, this will pick up application.yaml from the classpath
        Config config = Config.create();

        // this requires OCI configuration in the usual place
        // ~/.oci/config
        //ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
        //AuthenticationDetailsProvider authProvider = new ConfigFileAuthenticationDetailsProvider(configFile);
        AuthenticationDetailsProvider authProvider =
                new SessionTokenAuthenticationDetailsProvider(ConfigFileReader.DEFAULT_FILE_PATH,"helidonocidev");
        GenerativeAiInferenceClient generativeAiInferenceClient = GenerativeAiInferenceClient.builder().build(authProvider);

        // Prepare routing for the server
        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(routing -> routing
                        .register("/genai", new GenAiService(generativeAiInferenceClient, config))
                        // OCI SDK error handling
                        .error(BmcException.class, (req, res, ex) ->
                                res.status(ex.getStatusCode())
                                        .send(ex.getMessage())))
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/");
    }
}

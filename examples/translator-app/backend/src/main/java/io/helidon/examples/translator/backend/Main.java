/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.translator.backend;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.logging.common.LogConfig;
import io.helidon.tracing.Tracer;
import io.helidon.tracing.TracerBuilder;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.tracing.TracingObserver;

/**
 * Translator application backend main class.
 */
public class Main {

    private Main() {
    }

    static void setup(WebServerConfig.Builder server) {
        Config config = Config.builder()
                .sources(ConfigSources.environmentVariables())
                .build();

        Tracer tracer = TracerBuilder.create(config.get("tracing"))
                .serviceName("helidon-webserver-translator-backend")
                .build();

        server.port(9080)
                .addFeature(ObserveFeature.builder()
                                    .addObserver(TracingObserver.create(tracer))
                                    .build())
                .routing(routing -> routing
                        .register(new TranslatorBackendService()));
    }

    /**
     * The main method of Translator backend.
     *
     * @param args command-line args, currently ignored.
     */
    public static void main(String[] args) {
        // configure logging in order to not have the standard JVM defaults
        LogConfig.configureRuntime();

        WebServerConfig.Builder builder = WebServer.builder();
        setup(builder);
        WebServer server = builder.build().start();

        System.out.println("WEB server is up! http://localhost:" + server.port());
    }
}

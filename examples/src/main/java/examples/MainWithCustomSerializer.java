/*
 * Copyright 2022 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;

import dev.c0ps.libhttpd.HttpServerGracefulShutdownThread;
import dev.c0ps.libhttpd.HttpServerImpl;
import examples.data.CoordinateModule;
import examples.services.CoordinateService;

public class MainWithCustomSerializer {

    private static final int HTTP_PORT = 12345;
    private static final String HTTP_BASE_URL = "/";

    public static void main(String[] args) {

        // create and register modules for custom serialization
        var om = new ObjectMapper().registerModule(new CoordinateModule());

        // bind the ObjectMapper to your configured instance
        var injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ObjectMapper.class).toInstance(om);
            }
        });

        // start server as before
        var server = new HttpServerImpl(injector, HTTP_PORT, HTTP_BASE_URL);
        server.register(CoordinateService.class);
        Runtime.getRuntime().addShutdownHook(new HttpServerGracefulShutdownThread(server));
        server.start();
    }
}
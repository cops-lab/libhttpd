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

import com.google.inject.Guice;
import com.google.inject.Injector;

import dev.c0ps.libhttpd.HttpServerGracefulShutdownThread;
import dev.c0ps.libhttpd.HttpServerImpl;
import dev.c0ps.libhttpd.Scope;
import examples.services.BasicService;
import examples.services.CounterService;

public class MainMinimal {

    private static final int HTTP_PORT = 12345;
    private static final String HTTP_BASE_URL = "/";

    // Guice will be used for all injections
    private static final Injector INJECTOR = Guice.createInjector();

    public static void main(String[] args) {

        // instantiate server
        var server = new HttpServerImpl(INJECTOR, HTTP_PORT, HTTP_BASE_URL);

        // register all JAX-RS resources of your application here
        server.register(BasicService.class);

        // you can play around with different scopes here, e.g., SINGLETON or PROTOTYPE
        server.register(Scope.SINGLETON, CounterService.class);

        // server blocks until it is killed (e.g., via SIGNIT / Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new HttpServerGracefulShutdownThread(server));
        server.start();
    }
}
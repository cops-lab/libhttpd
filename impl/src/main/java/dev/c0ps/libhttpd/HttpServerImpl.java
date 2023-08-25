/*
 * Copyright 2021 Delft University of Technology
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
package dev.c0ps.libhttpd;

import static dev.c0ps.libhttpd.Scope.SINGLETON;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class HttpServerImpl implements HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerImpl.class);

    private final Injector injector;
    private final String httpBaseUrl;
    private final Server server;

    private final Map<Class<?>, Scope> bindings = new HashMap<>();

    @Inject
    public HttpServerImpl(Injector injector, //
            @Named("HttpServerImpl.httpPort") int httpPort, //
            @Named("HttpServerImpl.httpBaseUrl") String httpBaseUrl) {
        this.injector = injector;
        this.httpBaseUrl = httpBaseUrl;
        server = new Server(httpPort);
    }

    @Override
    public void register(Class<?>... types) {
        register(SINGLETON, types);
    }

    @Override
    public void register(Scope scope, Class<?>... types) {
        for (var type : types) {
            bindings.put(type, scope);
        }
    }

    @Override
    public void start() {
        LOG.info("Starting HTTP Server ...");

        var config = new HttpServerConfig(injector, bindings);

        var ctx = new ServletContextHandler(NO_SESSIONS);
        ctx.setContextPath(httpBaseUrl);
        ctx.addServlet(new ServletHolder(new ServletContainer(config)), "/*");

        // TODO extend with resource handling?
        // See https://stackoverflow.com/questions/28418449/what-is-difference-between-servletcontexthandler-setresourcebase-and-resourcehan/28419106#28419106

        server.setHandler(ctx);

        try {
            server.start();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStarting() {
        return server.isStarting();
    }

    @Override
    public void stop() {
        LOG.info("Stopping HTTP Server ...");
        try {
            if (server != null && server.isRunning()) {
                server.stop();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
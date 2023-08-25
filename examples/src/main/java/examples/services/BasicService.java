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
package examples.services;

import java.util.List;

import examples.data.Person;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/basic")
public class BasicService {

    @GET
    @Path("/helloworld")
    public Response consumeQueryParameters(@QueryParam("name") String name) {
        return Response.ok("Hello " + name + "!").build();
    }

    @GET
    @Path("/fail")
    public Response returnCustomError(@QueryParam("name") String name) {
        return Response.status(Status.NOT_IMPLEMENTED).entity("you can add your own content").build();
    }

    @GET
    @Path("/hello/{name}")
    public Response consumePathParameters(@PathParam("name") String name) {
        return Response.ok("Hello " + name + "!").build();
    }

    @GET
    @Path("/header-in")
    public Response accessHeader(@HeaderParam("foo") String foo) {
        return Response.ok("Header 'foo' was: " + foo).build();
    }

    @GET
    @Path("/header-out")
    public Response writeHeader(@HeaderParam("foo") String foo) {
        return Response.ok("check response header!").header("foo", "bar").build();
    }

    @GET
    @Path("/returnjson")
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnJson() {
        return Response.ok(List.of(1, 2, 3)).build();
    }

    @POST
    @Path("/consumejson")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeJsonBody(Person p) {
        var msg = String.format("Hello %s %s!", p.first, p.last);
        return Response.ok(msg).build();
    }
}
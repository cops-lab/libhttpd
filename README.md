# libhttpd

This repository contains *libhttpd*, a simple facade for a [JAX-RS compatible][jax-rs] webserver that trivializes the creation of REST APIs.

Webservices and micro-services are used everywhere these days and a rudimentary REST API is a very common requirement of many applications.
It should be straight-forward to add one to an application, right?
The canonical way to build Java web applications is via a *servlet container*, for exampe, using popular implementations like [Eclipse Jetty][jetty] or [Apache Tomcat][tomcat].
However, these servers represent a *general solution* and must work in every imaginable scenario.
As a result, they are complicated to setup and integrate into existing applications.
Frameworks like [Spring][spring] hide all these details well and provide an easier interface to developers.
However, the approachability comes at the price of being locked into the ecosystem and additional constraints like the necessity to use specialized mechanisms for dependency injection.
This makes it even harder to extent an *existing* applications without complete re-write.
After realizing *how* unnecessary complicated it is to add a simple REST API to an existing application, the idea of this project was born.

The *libhttpd* library takes inspiration from [Spring][spring] and hides the implementation details of a servlet container while reusing established techniques and not imposing any architectural compromises on the client.
The facade of *libhttpd* boils down to two basic interfaces (*HttpServer* and *Scope*), which allow to register JAX-RS resources.
The current implementation is based on [Jetty][jetty], but no part of the facade is Jetty-specific, so future versions can be switched to other servers.
Instead of Jetty's built-in HK2 injector, *libhttpd* allows to use [Google Guice][guice] for the instantiation and injection of any requested objects, which is used much more widely.
With full controll over the injection process, it becomes easily possible to attach object mappers, which can automatically handle the parsing/serialization of data structures in requests/responses.
By default, [Jackson][jackson] is used for JSON serialization, which can be easily extended with serializers for custom data structures.

At the moment, *libhttpd* does not try to be a full-blown webserver, e.g., it is not possible to return static resources like images.
While the underlying [Jetty server][jetty] would make that certainly possible, the main use case of *libhttpd* is to trivialize the setup and creation of webservices.

In short, *libhttpd* has the following features:

- Trivial to learn and get started
- Can be integrated as a library into existing applications (only one direct dependency!)
- Reuses battle-proven OSS technology under the hood ([Eclipse Jetty][jetty])
- Full support for [JAX-RS resources and annotations][jax-rs]
- Full support for object mappers, by default, [Jackson][jackson] is used for JSON mapping
- Dependency injection via [Google Guice][guice]


[jax-rs]: https://en.wikipedia.org/wiki/Jakarta_RESTful_Web_Services
[jetty]: https://eclipse.dev/jetty/
[tomcat]: https://tomcat.apache.org/
[spring]: https://spring.io/
[guice]: https://github.com/google/guice
[jackson]: https://github.com/FasterXML/jackson

**Please note:** Packages of *libhttpd* are released in the [COPS Lab Packages Repository](https://github.com/cops-lab/packages) and can be added to your project as a regular Maven dependency. Refer to the GitHub documentation to understand [how to add that repository as a package registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) to a Maven `pom.xml` file.

## Examples

The [examples](./examples/) folder contains a project that allows to start the server.
The project contains several resources that illustrate how to perform several standard operations.
In order to get started, checkout the repository, build it with Maven, and change to the `examples/target` folder.

    $ git checkout https://github.com/cops-lab/libhttpd
    $ cd libhttpd
    $ mvn clean verify
    $ cd examples/target

You can now explore the functionality in two different scenarios.
We recommend to replicate the following commands while following the implementation in the Java sources.
It is also interesting to study the `examples/pom.xml` file to understand the (few) requirements that *libhttpd* has on the project setup.

**Minimal Setup:**
The minimal setup cuts the configuration down to the bare minimum.
Start the `examples.MainMinimal` class.

    $ java -cp examples-0.0.1-SNAPSHOT.jar examples.MainMinimal

This command is blocking, so run the following `curl` commands from a separate terminal or your browser.
The first set of examples illustrates how to access query parameters, path parameters, and how to return regular and failing responses.
You will see in the corresponding source code that only JAX-RS annotations and base classes are used.

    $ curl http://localhost:12345/basic/helloworld?name=John%20Doe
    Hello John Doe!
    $ curl http://localhost:12345/basic/hello/John%20Doe
    Hello John Doe!
    $ curl -v http://localhost:12345/basic/fail
    ...
    < HTTP/1.1 501 Not Implemented
    ...
    you can add your own content

Similarly, it is possible to access the headers of `Request` or `Response` objects.

    $ curl -H "foo: bar" http://localhost:12345/basic/header-in
    Header 'foo' was: bar
    $ curl -v http://localhost:12345/basic/header-out
    ...
    < foo: bar
    ...
    check response header!

By default, resource classes are initialized on request, however, the `Scope` can be defined in the resource registration to achieve a different behavior.
For example, the `CounterService` is configured in `MainMinimal` call as a *singleton*.
Please note that the same behavior could also be achieved by making `CounterService` a singleton in the Guice configuration.
Regardless of the chosen way, the result is that the endpoint becomes stateful and repeated calls will increase the counter.

    $ curl http://localhost:12345/counter
    0
    $ curl http://localhost:12345/counter
    1

Even without further configuration it is already possible to consume and return JSON, which will be automatically parsed and serialized.
This basic handling works as long as the standard serialization is sufficient.
Please note the `@GET` and `@POST` annotations in the examples, which are required to match the different HTTP methods.
Similarly, it is required to annotate methods with `@Consumes` and `@Produces` to match the media type of the request, so the server can select the right object mapper.

*Please note:* This is a concept that many people do not grasp at first.
At no point is it necessary to create or parse JSON yourself.
The object mapper abstracts from that and both inputs and outputs are plain Java objects.


    $ curl http://localhost:12345/basic/returnjson
    [1,2,3]
    $ curl -d '{"first": "John", "last": "Doe"}' --header "Content-Type:application/json" http://localhost:12345/basic/consumejson
    Hello John Doe!


**Advanced Setup with Custom Serialization:**
Sometimes, it becomes necessary to register custom serializers for data structures.
To keep the initial configuration simple, this part has been split out to a second example.
Stop the previous server and start the second main class `examples.MainWithCustomSerializer`.

    $ java -cp examples-0.0.1-SNAPSHOT.jar examples.MainWithCustomSerializer

The setup logic in the main class will register the `CoordinateModule` which provides a custom (de-) serializer for the `Coordinate` data structure, which encodes an X/Y coordinate in the form `x,z`.
Please note that the input coordinate must be provided as valid JSON, i.e., including quotes.

    $ curl -d "\"2,3\"" --header "Content-Type:application/json" http://localhost:12345/coordinates
    "3,6"

*Please note:* Both the input and the output JSON use the shortened encoding (`"2,3"` and `"4,6"`), while the corresponding endpoint method can work with an `Coordinate` object that provides access to `x` and `y`.



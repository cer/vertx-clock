/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.chrisrichardson.vertxclock;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.deploy.Verticle;

public class VertxClock extends Verticle {

  private CloudEnvironment env = new CloudEnvironment();

  public void start() {
    HttpServer server = vertx.createHttpServer();

    server.requestHandler(new Handler<HttpServerRequest>() {
      public void handle(HttpServerRequest req) {
        if (req.path.equals("/")) 
          req.response.sendFile("public/index.html"); 
        else
          req.response.sendFile("public/" + req.path);
      }
    });

    SockJSServer sockServer = vertx.createSockJSServer(server);

    sockServer.installApp(new JsonObject().putString("prefix", "/testapp"), new Handler<SockJSSocket>() {
      public void handle(final SockJSSocket sock) {

        final long timerID = vertx.setPeriodic(1000, new Handler<Long>() {

             int counter = 0;

             public void handle(Long timerID) {
                System.out.println("And every second this is printed: " + counter); 
                sock.writeBuffer(new Buffer(Integer.toString(counter++)));
          }
        });

        sock.endHandler(new Handler<Void>() {
          public void handle(Void data) {
           System.out.println("Cancelling timer"); 
           vertx.cancelTimer(timerID);
          }
        });

        sock.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            sock.writeBuffer(data); // Echo it back
          }
        });
      }
    });

    int port = determinePort();
    server.listen(port);
  }

  private int determinePort() {
    return env.isCloudFoundry() ? env.getInstanceInfo().getPort() : 8080;
  }
}

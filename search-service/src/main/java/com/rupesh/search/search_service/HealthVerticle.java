package com.rupesh.search.search_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class HealthVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.createHttpServer().requestHandler(req -> {
      if ("/health".equals(req.path())) {
        req.response().putHeader("content-type", "text/plain")
          .end("OK");
      } else {
        req.response().setStatusCode(404).end();
      }
    }).listen(8081).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("Health check server started on port 8081");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}

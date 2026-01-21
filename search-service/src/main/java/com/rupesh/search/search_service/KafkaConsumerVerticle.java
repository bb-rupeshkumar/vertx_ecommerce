package com.rupesh.search.search_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerVerticle extends AbstractVerticle {

  private KafkaConsumer<String, String> consumer;
  private static final String TOPIC = "product-search-events";

  @Override
  public void start(Promise<Void> startPromise) {

    System.out.println("🚀 KafkaConsumerVerticle started");

    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", "ecommerce-kafka:29092"); // IMPORTANT
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("group.id", "search-indexer-v3");
    config.put("auto.offset.reset", "earliest");
    config.put("enable.auto.commit", "true");

    consumer = KafkaConsumer.create(vertx, config);

    consumer
      .exceptionHandler(err ->
        System.err.println("❌ Kafka error: " + err.getMessage())
      );

    consumer
      .partitionsAssignedHandler(p ->
        System.out.println("📌 Partitions assigned: " + p)
      );

    consumer.handler(record -> {
      System.out.println(
        "🔥 CONSUMED key=" + record.key() +
          " value=" + record.value() +
          " partition=" + record.partition() +
          " offset=" + record.offset()
      );
    });

    consumer.subscribe(TOPIC)
      .onSuccess(v -> {
        System.out.println("✅ Subscribed to " + TOPIC);
        startPromise.complete();
      })
      .onFailure(err -> {
        System.err.println("❌ Subscribe failed: " + err.getMessage());
        startPromise.fail(err);
      });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (consumer != null) {
      consumer.close().onComplete(stopPromise);
    } else {
      stopPromise.complete();
    }
  }
}

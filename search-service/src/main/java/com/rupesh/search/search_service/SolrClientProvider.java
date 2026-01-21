package com.rupesh.search.search_service;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;

import java.util.concurrent.TimeUnit;

public final class SolrClientProvider {

  private static final String SOLR_URL =
    System.getenv().getOrDefault(
      "SOLR_URL",
      "http://localhost:8983/solr/product"
    );

  private static final SolrClient CLIENT =
    new Http2SolrClient.Builder(SOLR_URL)
      .withConnectionTimeout(5000, TimeUnit.SECONDS)
      .withIdleTimeout(10000, TimeUnit.SECONDS)
      .build();

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        CLIENT.close();
      } catch (Exception ignored) {}
    }));
  }

  private SolrClientProvider() {}

  public static SolrClient getClient() {
    return CLIENT;
  }
}

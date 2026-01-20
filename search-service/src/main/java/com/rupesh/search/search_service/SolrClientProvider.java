package com.rupesh.search.search_service;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class SolrClientProvider {

  private static final String SOLR_URL =
    System.getenv().getOrDefault(
      "SOLR_URL",
      "http://localhost:8983/solr/product"
    );

  private static final SolrClient CLIENT =
    new HttpSolrClient.Builder(SOLR_URL)
      .withConnectionTimeout(5000) // 5 seconds to connect
      .withSocketTimeout(10000)    // 10 seconds to wait for data
      .build();

  private SolrClientProvider() {}

  public static SolrClient getClient() {
    return CLIENT;
  }
}

package com.rupesh.search.search_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;


// /api/search?q=iphone&category=mobile&brand=apple&minPrice=50000&sort=price_desc&page=0&size=10
public class SearchHttpVerticle extends AbstractVerticle {

  private final SolrClient solrClient = SolrClientProvider.getClient();

  @Override
  public void start() {

    vertx.createHttpServer().requestHandler(req -> {

      if (!"/api/search".equals(req.path())) {
        req.response().setStatusCode(404).end();
        return;
      }

      String q = req.getParam("q");
      if (q == null || q.isBlank()) {
        req.response().setStatusCode(400).end("Missing query param: q");
        return;
      }

      String category = req.getParam("category");
      String brand = req.getParam("brand");
      String minPrice = req.getParam("minPrice");
      String maxPrice = req.getParam("maxPrice");
      String sort = req.getParam("sort");

      int page = Integer.parseInt(req.getParam("page", "0"));
      int size = Integer.parseInt(req.getParam("size", "20"));

      vertx.executeBlocking(promise -> {
        try {
          SolrQuery query = new SolrQuery();

          // 🔹 Full-text search with boosting
          query.setQuery("name^5 description^2 text_all:" + q);

          // 🔹 Filters
          if (category != null) {
            query.addFilterQuery("category:" + category);
          }

          if (brand != null) {
            query.addFilterQuery("brand:" + brand);
          }

          if (minPrice != null || maxPrice != null) {
            String min = minPrice != null ? minPrice : "*";
            String max = maxPrice != null ? maxPrice : "*";
            query.addFilterQuery("price:[" + min + " TO " + max + "]");
          }

          // 🔹 Sorting
          if ("price_asc".equals(sort)) {
            query.setSort("price", SolrQuery.ORDER.asc);
          } else if ("price_desc".equals(sort)) {
            query.setSort("price", SolrQuery.ORDER.desc);
          } else if ("newest".equals(sort)) {
            query.setSort("created_at", SolrQuery.ORDER.desc);
          }

          // 🔹 Pagination
          query.setStart(page * size);
          query.setRows(size);

          QueryResponse response = solrClient.query(query);

          JsonArray items = new JsonArray();
          response.getResults().forEach(doc ->
            items.add(JsonObject.mapFrom(doc))
          );

          JsonObject result = new JsonObject()
            .put("total", response.getResults().getNumFound())
            .put("page", page)
            .put("size", size)
            .put("items", items);

          promise.complete(result);

        } catch (Exception e) {
          promise.fail(e);
        }
      }).onSuccess(res -> {
        req.response()
          .putHeader("content-type", "application/json")
          .end(res.toString());
      }).onFailure(err -> {
        err.printStackTrace();
        req.response().setStatusCode(500).end("Search failed");
      });

    }).listen(8888);

    System.out.println("🔍 Search API started on port 8888");
  }
}

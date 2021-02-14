package com.search.match.app.clients;

import feign.Param;
import feign.RequestLine;

public interface AmazonSearchClient {

    @RequestLine("GET /complete?search-alias=aps&client=amazon-search-ui&mkt=1&q={q}")
    Object[] findCurrentTopSearchResults(@Param("q") String q);
}

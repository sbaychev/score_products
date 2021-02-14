package com.search.match.app.services;

import com.search.match.app.response.SearchResponse;

public interface IScoreMatchService {

    SearchResponse scoreMatch(String searchParams);
}

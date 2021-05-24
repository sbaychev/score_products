package com.search.match.app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.match.app.clients.AmazonSearchClient;
import com.search.match.app.clients.AmazonSearchControllerFeignClientBuilder;
import com.search.match.app.exceptions.ServiceOutageException;
import com.search.match.app.model.Search;
import com.search.match.app.model.SearchResource;
import com.search.match.app.response.SearchResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class IScoreMatchServiceImpl implements IScoreMatchService {

    private static final Logger LOG = LoggerFactory.getLogger(IScoreMatchServiceImpl.class);

    private final AmazonSearchClient amazonSearchClient;

    private final ObjectMapper objectMapper;

    private AtomicLong start;

    private static final Pattern LEFT_SIDE_TRIM = Pattern.compile("^\\s+");
    private static final Pattern RIGHT_SIDE_TRIM = Pattern.compile("\\s+$");

    @Autowired
    public IScoreMatchServiceImpl() {
        AmazonSearchControllerFeignClientBuilder feignClientBuilder = new AmazonSearchControllerFeignClientBuilder();
        this.amazonSearchClient = feignClientBuilder.getAmazonSearchClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public SearchResponse scoreMatch(String searchParams) {

//        searchParams = normalizeSearchParams(searchParams);

        List<Integer> listMatches = new ArrayList<>();

        start = new AtomicLong(System.nanoTime());

        try {
            while (timedBoolean()) {
                SearchResource amazonSearch = performAmazonSearch(searchParams);
                // logging as info the results for later comparison for exact matches
                LOG.info("Amazon Search Result: {}", amazonSearch);
                int scoreMatch = performScoreMatch(amazonSearch, searchParams);
                listMatches.add(scoreMatch);
            }
        } catch (Exception e) {
            throw new ServiceOutageException("Remote Service Temporarily Unavailable, try again in a few minutes");
        }

        return new SearchResponse(searchParams, (int) listMatches.stream().mapToInt(i -> i).average().orElse(0));
    }

    private boolean timedBoolean() {
        return System.nanoTime() - start.get() < TimeUnit.SECONDS.toNanos(2);
    }

    private int performScoreMatch(SearchResource amazonSearch, String searchParams) {

        List<String> matches = amazonSearch.getSearch().getSearchResults();
        int index;

        if (matches.isEmpty() || (index = matches.indexOf(searchParams)) < 0) {
            return 0;
        }

        // the search-volume score
        return 100 - (index * 10);
    }

    @Async
    public SearchResource performAmazonSearch(String searchParams) {

        Object[] response = amazonSearchClient.findCurrentTopSearchResults(searchParams);
        List<String> listOfMatches = objectMapper.convertValue(response[1], ArrayList.class);

        return new SearchResource(new Search(String.valueOf(response[0]), listOfMatches));
    }

//    private String normalizeSearchParams(String searchParams) {
//
//        // the API call truncates any leading spaces and trims to single space any lagging such
//        // sample https://completion.amazon.com/search/complete?search-alias=aps&client=amazon-search-ui&mkt=1&q=%20%20%20charger%20red%20%20%20%
//        return RIGHT_SIDE_TRIM.matcher(LEFT_SIDE_TRIM.matcher(searchParams).replaceAll("")).replaceAll(" ");
//    }
}

package com.search.match.app.rest;

import com.search.match.app.exceptions.ServiceOutageException;
import com.search.match.app.response.SearchResponse;
import com.search.match.app.services.IScoreMatchService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScoreSearchMatchController {

    private static final Logger LOG = LoggerFactory.getLogger(ScoreSearchMatchController.class);

    @Autowired
    private IScoreMatchService iScoreMatchService;

    @GetMapping("/estimate")
    @ResponseBody
    public Callable<ResponseEntity<Void>> getScore(@RequestParam(value = "keyword") String keyword) {

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        LOG.info("Time Request Entry: {}", now);

        SearchResponse searchResponse = iScoreMatchService.scoreMatch(keyword);

        LOG.info("Time Request Handled: {}s",
            ((Timestamp.valueOf(LocalDateTime.now()).getTime()) - now.getTime()) / 1000);

        return () -> new ResponseEntity(searchResponse, HttpStatus.FOUND);
    }

    @ExceptionHandler(ServiceOutageException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<String> handleServiceOutageException(ServiceOutageException exception) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(exception.getMessage());
    }
}

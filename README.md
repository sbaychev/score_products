# App for Amazon Auto Complete Weighted-Volume Scoring For Exact Word(s) Match


##Important Observations on the Amazon Auto Complete As is Provided Functionality

1. There is a discrepancy between the visual UI typing in to get suggestions, and the REST API - the Visual UI in 
   Amazon.com is doing deterministic auto-suggestion search based on previous typing within the same typing session.
   The API calls are straight-forward weighted index based and does not provide dynamic real-time result changes based 
   on similar above described.
   
   
2. The API call truncates any leading spaces within the searched term(s) and trims to single space any lagging such.
---
## The Logic Behind My Approach

1. The Amazon Autocomplete Search API - `https://completion.amazon.com/search/complete?search-alias=aps&client=amazon-search-ui&mkt=1&q=`
   (where q= is a Request Param) - Needs its means for access, thus I have used a Fluent Rest API Client - Feign 
   that is extensible to support current and future considerations. Like Service Breaker, Load Balancing and similar 
   needs.
   
2. Have developed within Spring Boot as is per documentation Rest API that has the following Contract:

REQUEST GET http://localhost:8080/estimate?keyword=keyWordValue
RESPONSE    `{
               "score" : 80,
               "Keyword" : "charger red"
             }`
3. The main assumption used in per-say "algorithm" handling is as described logic of Amazon API working:
   " Whenever the API is called, it operates in 2 steps:
   1. Seek: Get all known keywords that match the prefix and create a
      Candidate-Set
   2. Sort/Return: Sort the Candidate-Set by search-volume and return the top 10
      results"
      
=> 3a) Thus observing that the results returned by Amazon seem not to follow any sort of alpha-numerical sorting (nor 
levenstein distance measurements), but other. It was safe to assume that the first appearing result would be most 
recently searched and by thus heavier weighted in terms of search-volume score. 

In essence, the application takes a well-structured REST GET call, normalizes efficiently using REGex the input as per 
Amazon API (above described) specifics and makes an external REST call to Amazon API that is to return ideally a 
list of 10 Candidates. So out of the 10 Amazon API returned result based on this proprietary weighted search-volume score, if there are exact matches cross-section on the 
`keyword` supplied, then depending on the placement it gets a _preliminary score_ from 1-10 (where it belongs as index in the returned List Data Structure). 
Thereupon the `score` value gets calculated by subtracting from 100 the _preliminary score_ multiplied by 10 (As our 
base unit of extremum is 10based).
The edge cases relate to no records returned and the exact word(s) used not found in the returned list. In these cases 
the result is 0 ("practically never searched for");
As the main stipulation is "Exact Word(s) Match".


=> 3b) Following the 10s SLA, each Request is implemented to be non-blocking Asynchronous. This is done both for 
sake of application stability and throughput performance, as we have 3rd party (to Amazon API) requests as well.
There is also a 2s window for multiple Amazon API requests that do an average of the base use case given in 3a). 
Reason is to utilize the available time and provide upmost recent results for this time frame and still regardless
of the application load to meet the 10s SLA. In addition to thus far said, the Feign Client is implemented with a Retry
mechanism for at least 5 calls to the Amazon API on IOExceptions for up to 5 sec total retriable window - still 
within the 10 s SLA limit - additional service availability guarantee.


=> The hint provided is not exactly true as the "comparatively insignificant" is a vague statement for starters.
Multiple tries in determining the weighted logic of Amazon API auto-suggest were made, but each execution of the API
is as is. Results seem to be based on some predetermined search interval that puts the most desired for selection 
(volume) items first. Most Probably Amazon are using Elastic or Lucene like sharding technology that provides 
relatively fast indexing and weighing alongside levenstein and similar algo capabilities, but the API has access to 
mirror image, whereas the UI functionality has near(real)-time access. 

Thus to answer on the precision question, it is not that uncertain that what the API provides are good enough answers,
but they can not be taken as 100 or even 90% accurate. This is why the attempt to make many calls in them 2 sec and 
retry mechanism used, so that some average in case of data changes occur.

Also there are additional observations in ##Important Observations on the Amazon Auto Complete As is Provided Functionality

---
## Technologies Used

1. Java 8
2. Spring Boot | Starter + Web
3. OpenFeign (Client)
4. Jackson   
5. Lombok - Requires Annotation Processing in order to work from IDE
6. Maven
~~~
7. Curl
~~~
---

# Usage

The code provided is as is - Buildable and Usable

1. Do a usual ./mvnw spring-boot:run Or Simply Run from within your IDE
---
### Sample Requests

--> Can use curl or Postman or from within any web browser

returns result      - curl 'http://localhost:8080/estimate?keyword=iphone'

0  - no such word | searchable item  - curl 'http://localhost:8080/estimate?keyword=charger%20red%20green' 

---

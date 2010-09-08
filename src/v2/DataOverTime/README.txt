DATA OVER TIME EXAMPLE
======================

This application allows users to get data over time from the Google Analytics
API. Each query to the Google Analytics Data Feed includes a date range, a 
dimension, and a metric. Typically users will get aggregated metric data for
each dimension across the entire date range. This program attempts to get every
metric data for each dimension, for each day in the date range.

The program allows users to specify a query with one dimension and one metric.
It then gets a list of dimensions for the entire date range. Finally it builds
queries to get data over time for each of the dimension values returned from
the first query.

ARCHITECTURE
------------

After the application gets a list of dimensions, there are 2 approaches to
get data for those metrics over time:
1 - Using individual queries. Each dimension can be set as a filter in a
    query and the application can make a unique request to the API for every
    dimension. This always uses O(n) queries.

2 - Using grouped queries. Multiple dimensions can be set into a filter for
    a query using the OR filter operator. This results in far fewer requests
    to the API, reducing quota usage. Best case is O(1). Worst is O(n).


The DataOverTimeFactory provides static methods to get data using either
approach. The default is to use the grouped method.


RUNNING THE EXAMPLE
-------------------

There are a couple of configurations users can make when running this example.

Authorization: All requests to the Google Analytics Data Export API requires
    authorization. This example uses the Client Login mechanism. You must
    set your Google Account username and password in the MainDemo class.

File Output: The example outputs the result from the API into a file. The
    name of the file can be configured in the MainDemo class.

Data Query: This example can use any query that has one dimension and one
    metric to produce the list of dimensions that gets used to retrieve data
    over time. This query should be configured to use a Google Analytics
    profile the authorized user has access to. The query can be configured in
    the MainDemo class.


BUILDING THE EXAMPLE
--------------------

The code was written using JDK 1.6

The following Google Data libraries are required to build this application:
  gdata-analytics-2.1.jar
  gdata-analytics-meta-2.1.jar
  gdata-core-1.0.jar
  google-collect-1.0-rc1.jar
  jsr305.jar

And can be down loaded from the Google Data Project page here:
http://code.google.com/p/gdata-java-client/downloads/list


Unit tests have also been provided. They use JUnit 4.82 which also needs
to be in your build path to run the tests. JUnit can be downloaded here:
http://www.junit.org/


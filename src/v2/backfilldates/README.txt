DATA OVER TIME EXAMPLE
======================

This demonstrates how to backfill date values returned by the Google Analytics
API. If you request a date dimension along with other dimensions, if any of
the dates have 0 data, no values are returned by the API. This behavior makes
it difficult to compare and analyze data across date ranges.

This example demonstrates how to back fill the 0 values. 


RUNNING THE EXAMPLE
-------------------

There are a couple of configurations users can make when running this example.

Authorization: All requests to the Google Analytics Data Export API requires
    authorization. This example uses the Client Login mechanism. You must
    set your Google Account username and password in appropriate class
    members.

Data Query: This example can use any query that has one dimension and one
    metric to produce the list of dimensions that gets used to retrieve data
    over time. This query should be configured to use a Google Analytics
    profile / tableId the authorized user has access to. The query can be
    configured in the getDataQuery method.


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

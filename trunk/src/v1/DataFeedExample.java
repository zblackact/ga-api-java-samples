// Copyright 2009 Google Inc. All Rights Reserved.

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.Aggregates;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.DataSource;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample program demonstrating how to make a data request to the GA Data Export 
 * using client login authentication as well as accessing important data in the feed.
 */
public class DataFeedExample {

  private static final String CLIENT_USERNAME = "INSERT_LOGIN_EMAIL_HERE";
  private static final String CLIENT_PASS = "INSERT_PASSWORD_HERE";
  private static final String PROFILE_ID = "INSERT_PROFILE_ID_HERE";

  public static void main(String[] args) {

    //------------------------------------------------------
    // Configure GA API
    //------------------------------------------------------
    AnalyticsService as = new AnalyticsService("gaExportAPI_acctSample_v1.0");
    String baseUrl = "https://www.google.com/analytics/feeds/data";
    DataQuery query;

    //------------------------------------------------------
    // Client Login Authentication
    //------------------------------------------------------
    try {
      as.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);
    } catch (AuthenticationException e) {
      System.err.println("Error : " + e.getMessage());
      return;
    }

    //------------------------------------------------------
    // GA Data Feed
    //------------------------------------------------------
    // first build the query
    try {
      query = new DataQuery(new URL(baseUrl));
    } catch (MalformedURLException e) {
      System.err.println("Malformed URL: " + baseUrl);
      return;
    }
    query.setIds("ga:" + PROFILE_ID);
    query.setDimensions("ga:source,ga:medium");
    query.setMetrics("ga:visits,ga:bounces");
    query.setSort("-ga:visits");
    query.setFilters("ga:medium==referral");
    query.setMaxResults(100);
    query.setStartDate("2008-10-01");
    query.setEndDate("2008-10-31");
    URL url = query.getUrl();
    System.out.println("URL: " + url.toString());

    // Send our request to the Analytics API and wait for the results to come back
    DataFeed feed;
    try {
      feed = as.getFeed(url, DataFeed.class);
    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
      return;
    } catch (ServiceException e) {
      System.err.println("Analytics API responded with an error message: " + e.getMessage());
      return;
    }

    outputFeedData(feed);
    outputFeedDataSources(feed);
    outputFeedAggregates(feed);
    outputFeedData(feed);

    String tableData = DataFeedExample.getFeedTable(feed);
    System.out.println(tableData);
  }

  //------------------------------------------------------
  // Format Feed Related Data
  //------------------------------------------------------
  /**
   * Output the information specific to the feed.
   * @param {DataFeed} feed Parameter passed
   *     back from the feed handler.
   */
  public static void outputFeedData(DataFeed feed) {
    System.out.println(
      "\nFeed Title      = " + feed.getTitle().getPlainText() +
      "\nFeed ID         = " + feed.getId() +
      "\nTotal Results   = " + feed.getTotalResults() +
      "\nSart Index      = " + feed.getStartIndex() +
      "\nItems Per Page  = " + feed.getItemsPerPage() +
      "\nStart Date      = " + feed.getStartDate().getValue() +
      "\nEnd Date        = " + feed.getEndDate().getValue());
  }

  /**
  * Output information about the data sources in the feed.
  * Note: the GA Export API currently has exactly one data source.
  * @param {DataFeed} feed Parameter passed
  *     back from the feed handler.
  */
  public static void outputFeedDataSources(DataFeed feed) {
    DataSource gaDataSource = feed.getDataSources().get(0);
    System.out.println(
      "\nTable Name      = " + gaDataSource.getTableName().getValue() +
      "\nTable ID        = " + gaDataSource.getTableId().getValue() +
      "\nWeb Property Id = " + gaDataSource.getProperty("ga:webPropertyId") +
      "\nProfile Id      = " + gaDataSource.getProperty("ga:profileId") +
      "\nAccount Name    = " + gaDataSource.getProperty("ga:accountName"));
  }

  /**
  * Output all the metric names and values of the aggregate data.
  * The aggregate metrics represent values across all of the entries selected 
  *     by the query and not just the rows returned.
  * @param {DataFeed} feed Parameter passed
  *     back from the feed handler.
  */
  public static void outputFeedAggregates(DataFeed feed) {  
    Aggregates aggregates = feed.getAggregates();
    List<Metric> aggregateMetrics = aggregates.getMetrics();
    for (Metric metric : aggregateMetrics) {
      System.out.println(
        "\nMetric Name  = " + metric.getName() +
        "\nMetric Value = " + metric.getValue() +
        "\nMetric Type  = " + metric.getType() +
        "\nMetric CI    = " + metric.getConfidenceInterval().toString());
    }
  }

  /**
   * Output all the important information from the first entry in the data feed.
   * @param {DataFeed} feed Parameter passed
   *     back from the feed handler.
   */
  public static void outputEntryRowData(DataFeed feed) {
    List<DataEntry> entries = feed.getEntries();
    if (entries.size() == 0) {
      System.out.println("No entries found");
      return;
    }
    DataEntry singleEntry = entries.get(0);

    // properties specific to all the entries returned in the feed
    System.out.println("Entry ID    = " + singleEntry.getId());
    System.out.println("Entry Title = " + singleEntry.getTitle().getPlainText());

    // iterate through all the dimensions
    List<Dimension> dimensions = singleEntry.getDimensions();
    for (Dimension dimension : dimensions) {
      System.out.println("Dimension Name  = " + dimension.getName());
      System.out.println("Dimension Value = " + dimension.getValue());
    }

    // iterate through all the metrics
    List<Metric> metrics = singleEntry.getMetrics();
    for (Metric metric : metrics) {
      System.out.println("Metric Name  = " + metric.getName());
      System.out.println("Metric Value = " + metric.getValue());
      System.out.println("Metric Type  = " + metric.getType());
      System.out.println("Metric CI    = " + metric.getConfidenceInterval().toString());
    }
  }

  /**
   * Get the data feed values in the feed as a string.
   * @param {DataFeed} feed Parameter passed
   *     back from the feed handler.
   * @return {String} This returns the contents of the feed.
   */
  public static String getFeedTable(DataFeed feed) {
    List<DataEntry> entries = feed.getEntries();
    if (entries.size() == 0) {
      return "No entries found";
    }
    DataEntry singleEntry = entries.get(0);
    List<Dimension> dimensions = singleEntry.getDimensions();
    List<Metric> metrics = singleEntry.getMetrics();
    List<String> feedDataNames = new ArrayList<String>();
    String feedDataValues = "";

    // put all the dimension and metric names into an array
    for (Dimension dimension : dimensions) {
      feedDataNames.add(dimension.getName());
    }
    for (Metric metric : metrics) {
      feedDataNames.add(metric.getName());
    }

    // put the values of the dimension and metric names into the table
    for (DataEntry entry : entries) {
      for (String dataName : feedDataNames) {
        feedDataValues += "\n" + dataName + "\t= " + entry.stringValueOf(dataName);
      }
      feedDataValues += "\n";
    }
    return feedDataValues;
  }
}

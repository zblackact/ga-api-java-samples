// Copyright 2010 Google Inc. All Rights Reserved.
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
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This application demos the "AnalyticsCsvPrinter" class, which converts a
 * response from the Data Export API into CSV format.
 *
 * @author api.alexl@google.com (Alexander Lucas)
 */
public class AnalyticsCsvDemo {

  private static final String CLIENT_USERNAME = "INSERT_USERNAME_HERE";
  private static final String CLIENT_PASS = "INSERT_PASSWORD_HERE";
  private static final String TABLE_ID = "INSERT_TABLEID_HERE";

  private static AnalyticsCsvPrinter printer = new AnalyticsCsvPrinter();
  /**
   * Grabs the response feed and prints it in CSV format
   * to standard output.
   * @param args Command Line Arguments.
   */
  public static void main(String[] args) {
      DataFeed feed = getDataFeed(getAnalyticsService("cvs_printing_demo"), getDataQuery());

      if(args.length > 0) {
        printFeedToFile(feed, args[0]);
      } else {
        // If no output stream is set, the default is to print to stdout.
        printer.printFeedAsCsv(feed);
      }
  }

  /**
   * Opens a write stream to a file, and sets that as the output stream for the
   * printer object that prints the feed.
   * @param feed The DataFeed to convert to CSV format.
   * @param filename The filename to be written to.
   */
  private static void printFeedToFile(DataFeed feed, String filename) {
    PrintStream stream = null;
    try {
      FileOutputStream fstream = new FileOutputStream(filename);
      stream = new PrintStream(fstream);
      printer.setPrintStream(stream);
      printer.printFeedAsCsv(feed);
    } catch (FileNotFoundException e) {
      System.out.println("File not found: " + e.getMessage());
    } finally {
      if (stream != null) {
        stream.flush();
        stream.close();
      }
    }
  }

  /**
   * Creates and returns a new AnalyticsService object and authorizes the user using
   * Client Login.
   * @return AnalyticsService to be used
   */
  private static AnalyticsService getAnalyticsService(String clientName) {
    try {
      AnalyticsService analyticsService = new AnalyticsService(clientName);
      analyticsService.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);
      return analyticsService;
    } catch (AuthenticationException e) {
      System.err.println("Authentication failed : " + e.getMessage());
      System.exit(-1);
    }
    return null;
  }

  /**
   * Creates and returns the default DataQuery.
   *
   * @return AnalyticsService to be used
   */
  private static DataQuery getDataQuery() {
    DataQuery query = null;
    try {
      // Create a query using the DataQuery Object.
      query = new DataQuery(new URL(
          "https://www.google.com/analytics/feeds/data"));
      query.setStartDate("2009-04-01");
      query.setEndDate("2009-04-30");
      query.setDimensions("ga:pageTitle,ga:pagePath");
      query.setMetrics("ga:visits,ga:pageviews");
      query.setSort("-ga:pageviews");
      query.setMaxResults(10);
      query.setIds(TABLE_ID);
    } catch (MalformedURLException e) {
      System.err.println("Error, malformed URL: " + e.getMessage());
      System.exit(-1);
    }
    return query;
  }

  /**
   * Makes the actual request to the server.
   *
   * @param analyticsService Google Analytics service object that
   *     is authorized through Client Login.
   * @param query the query being sent to the Data Export API.
   * @returns the responds from the Data Export API.
   */
  private static DataFeed getDataFeed(AnalyticsService analyticsService, DataQuery query) {
    DataFeed dataFeed = null;
    try {
      // Make a request to the API.
      dataFeed = analyticsService.getFeed(query.getUrl(), DataFeed.class);
    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
      System.exit(-1);
    } catch (ServiceException e) {
      System.err.println("Analytics API responded with an error message: " + e.getMessage());
      System.exit(-1);
    }
    return dataFeed;
  }
}

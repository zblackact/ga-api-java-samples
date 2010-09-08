// Copyright 2010 Google Inc. All Rights Reserved.

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.util.AuthenticationException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Main Demo illustrating how to programmatically access a metric for
 * a list of dimensions across a date range. To modify this program,
 * just update the User Configuration constants and update the query
 * in the getDataQuery method.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class MainDemo {

  private static final String APP_NAME = "DataOverTimeDemo";
  private static final String BASE_DATA_FEED_URL = "https://www.google.com/analytics/feeds/data";

  // User configurations.
  private static final String USERNAME = "INSERT_YOUR_GOOGLE_ACCOUNT_LOGIN";
  private static final String PASSWORD = "INSERT_YOUR_GOOGLE_ACCOUNT_PASSWORD";
  private static final String TABLE_ID = "INSERT_YOUR_TABLE_ID";
  private static final String fileName = "output.csv";

  private static AnalyticsService analyticsService;

  /**
   * Main Program.
   * @param args Command line arguments.
   */
  public static void main(String args[]) {
    try {
      new MainDemo();
    } catch (FileNotFoundException e) {
      System.err.println ("File " + fileName + "not found");
      return;
    }
  }

  /**
   * The actual main demo. This gets all the data over time as
   * a Results object then prints the results to a file.
   * @throws FileNotFoundException If the file was not found.
   */
  public MainDemo() throws FileNotFoundException  {
    analyticsService = getAnalyticsService();

    DataOverTime dataOverTime = DataOverTimeFactory.getGroupQueries(analyticsService);
    //dataOverTime = DataOverTimeFactory.getIndividualQueries(analyticsService);

    Results results = dataOverTime.getData(getDataQuery());
    results.printCsvToFile(fileName);
  }

  /**
   * Returns a new AnalyticsService object that has been authorized with
   * Client Login authorization mechanism.
   * @return A new AnalyticsService object.
   */
  private static AnalyticsService getAnalyticsService() {
    AnalyticsService analyticsService = new AnalyticsService(APP_NAME);
    try {

      analyticsService.setUserCredentials(USERNAME, PASSWORD);

    } catch (AuthenticationException e) {
      System.err.println("Problem setting credentials: " + e.getMessage());
      System.exit(0);
    }
    return analyticsService;
  }

  /**
   * Returns A DataQuery Object to fetch the initial dimensions to be used
   * in subsequent API calls to get a metric for each dimension over time.
   * This should always have at most one dimension and one metric.
   * @return A DataQuery Object.
   */
  private static DataQuery getDataQuery() {
    DataQuery dataQuery = null;
    try {
      dataQuery = new DataQuery(new URL(BASE_DATA_FEED_URL));
    } catch (MalformedURLException e) {
      System.err.println("There was a problem: " + e.getMessage());
      System.exit(0);
    }

    dataQuery.setIds(TABLE_ID);
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-03-01");
    dataQuery.setDimensions("ga:source");
    dataQuery.setMetrics("ga:visits");
    dataQuery.setSort("-ga:visits");
    dataQuery.setFilters("ga:medium==referral");
    dataQuery.setMaxResults(20);

    return dataQuery;
  }
}

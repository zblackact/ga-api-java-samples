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
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provides a class that back fills date values not returned by the Google
 * Analytics API. If a query is made that includes the date dimensions as
 * well as other dimensions, if any of the days have 0 values, the date is not
 * returned. This makes it difficult to build tables for analysis. This class
 * fills in all the missing date data and outputs the results to the default
 * stream.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class BackfillDates {

  private static final String APP_NAME = "BackFillDemo_v1";
  private static final String DATA_FEED_URL = "https://www.google.com/analytics/feeds/data";
  private static final long millisInDay = 24 * 60 * 60 * 1000;

  // User configurable options.
  private static final String USERNAME = "INSERT_USERNAME";
  private static final String PASSWORD = "INSERT_PASSWORD";
  private static final String TABLE_ID = "INSERT_TABLEID";

  private static SimpleDateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyyMMdd");
  private static Calendar calendar = Calendar.getInstance();

  private int numberOfDays;
  private String expectedStartDate;

  /**
   * The entry point into this applications.
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    BackfillDates demo = new BackfillDates();
    demo.execute();
  }

  /**
   * Executes this program.
   */
  public void execute() {
    DataQuery dataQuery = getDataQuery();
    DataFeed dataFeed = getDataFeed(dataQuery);

    setExpectedStartDate(dataQuery.getStartDate());
    setNumberOfDays(dataQuery);

    printExpectedDates(dataQuery);
    printBackfilledResults(dataFeed);
  }

  /**
   * Sets the expectedStartDate member by formatting the start date found
   * in a DataQuery object.
   * @param startDate The start date format as yyyy-MM-dd
   */
  public void setExpectedStartDate(String startDate) {
    try {
       calendar.setTime(queryDateFormat.parse(startDate));
       expectedStartDate = resultDateFormat.format(calendar.getTime());
    } catch (ParseException e) {
      handleException(e);
    }
  }

  /**
   * Returns the number of days in the date range of the dataQuery object.
   * This includes the final day.
   * @param dataQuery A DataQuery object which has start and end date
   *     parameters set.
   */
  public void setNumberOfDays(DataQuery dataQuery) {
    long startDay = 0;
    long endDay = 0;

    try {
      calendar.setTime(queryDateFormat.parse(dataQuery.getStartDate()));
      startDay = calendar.getTimeInMillis() / millisInDay;

      calendar.setTime(queryDateFormat.parse(dataQuery.getEndDate()));
      endDay = calendar.getTimeInMillis() / millisInDay;
    } catch (ParseException e) {
      handleException(e);
    }

    numberOfDays = (int) (endDay - startDay + 1);
  }

  /**
   * Returns a list of all the dates in the date range.
   * @param dataQuery A dataQuery object whose start and end dates are set.
   * @return A list of all the dates in the date range.
   */
  public List<String> getListOfDates(DataQuery dataQuery) {
    try {
      calendar.setTime(resultDateFormat.parse(expectedStartDate));
    } catch (ParseException e) {
      handleException(e);
    }
    List<String> listOfDates = new ArrayList<String>(numberOfDays);
    listOfDates.add(resultDateFormat.format(calendar.getTime()));

    // Start from 1 since there is already a day in the list.
    for (int i = 1; i < numberOfDays; i++) {
      calendar.add(Calendar.DATE, 1);
      listOfDates.add(resultDateFormat.format(calendar.getTime()));
    }

    return listOfDates;
  }

  /**
   * Prints the expected dates as comma separated values. The first value is
   * the dimension in the original query.
   * @param A data query object whose start and end dates are set.
   */
  public void printExpectedDates(DataQuery dataQuery) {
    StringBuilder output = new StringBuilder();
    output.append(dataQuery.getDimensions());
    for (String date : getListOfDates(dataQuery)) {
      output.append(",").append(date);
    }
    System.out.println(output.toString());
  }

  /**
   * Prints the backFilled results.
   * @param dataFeed The results returned from the Google Analytics API.
   */
  public void printBackfilledResults(DataFeed dataFeed) {
    String expectedDate = "";
    String dimensionValue = "";
    List<Integer> row = null;

    for (DataEntry entry : dataFeed.getEntries()) {
      String tmpDimValue = entry.getDimensions().get(0).getValue();

      // Detect beginning of a series.
      if (!tmpDimValue.equals(dimensionValue)) {
        if (row != null) {
          forwardFillRow(row);
          printRow(dimensionValue, row);
        }

        // Create a new row.
        row = new ArrayList<Integer>(numberOfDays);
        dimensionValue = tmpDimValue;
        expectedDate = expectedStartDate;
      }

      // Backfill row.
      String foundDate = entry.getDimension("ga:date").getValue();
      if (!foundDate.equals(expectedDate)) {
        backFillRow(expectedDate, foundDate, row);
      }

      // Handle the data.
      Metric metric = entry.getMetrics().get(0);
      row.add(new Integer(metric.getValue()));
      expectedDate = getNextDate(foundDate);
    }

    // Add the last row.
    if (row != null) {
      forwardFillRow(row);
      printRow(dimensionValue, row);
    }
  }

  /**
   * Adds 0 values in the row List for the number of days between start and
   * end date. If both dates are equal or if start date is greater than end
   * date, nothing happens.
   * @param startDate The expected date.
   * @param endDate The found date.
   * @param row The current row being added to the table.
   */
  public void backFillRow(String startDate, String endDate, List<Integer> row) {
    long d1 = 0;
    long d2 = 0;

    try {
      calendar.setTime(resultDateFormat.parse(startDate));
      d1 = calendar.getTimeInMillis() / millisInDay;

      calendar.setTime(resultDateFormat.parse(endDate));
      d2 = calendar.getTimeInMillis() / millisInDay;

    } catch (ParseException e) {
      handleException(e);
    }

    long differenceInDays = d2 - d1;
    if (differenceInDays > 0) {
      for (int i = 0; i < differenceInDays; i++) {
        row.add(0);
      }
    }
  }

  /**
   * Pads the end of the list with 0 values until the final row length is
   * rowSize.
   * @param row The row to add 0 values to. Must not be null.
   */
  public void forwardFillRow(List<Integer> row) {
    int remainingElements = numberOfDays - row.size();
    if (remainingElements > 0) {
      for (int i = 0; i < remainingElements; i++) {
        row.add(0);
      }
    }
  }

  /**
   * Returns the following date represented by initialDate.
   * @param initialDate The string representation of the date to increment.
   * @return The next date.
   */
  public String getNextDate(String initialDate) {
    try {
      calendar.setTime(resultDateFormat.parse(initialDate));
      calendar.add(Calendar.DATE, 1);
      return resultDateFormat.format(calendar.getTime());

    } catch (ParseException e) {
      handleException(e);
    }
    return "";
  }

  /**
   * Prints the row name and the values in a row.
   * @param rowName The name of the row.
   * @param row An array of Integers to print.
   */
  public void printRow(String rowName, List<Integer> row) {
    StringBuilder output = new StringBuilder();
    output.append(rowName);
    for (Integer value : row) {
      output.append(",").append(value);
    }
    System.out.println(output.toString());
  }

  /**
   * Returns a new DataQuery Object. You can configure this with your own
   * parameters.
   * @return A new DataQuery Object set with parameters.
   */
  public static DataQuery getDataQuery() {
    DataQuery dataQuery = null;
    try {
      dataQuery = new DataQuery(new URL(DATA_FEED_URL));
    } catch (MalformedURLException e) {
      handleException(e);
    }

    dataQuery.setIds(TABLE_ID);
    dataQuery.setStartDate("2010-03-01");
    dataQuery.setEndDate("2010-03-05");
    dataQuery.setDimensions("ga:landingPagePath,ga:date");
    dataQuery.setMetrics("ga:entrances");
    dataQuery.setSort("ga:landingPagePath,ga:date");
    dataQuery.setFilters("ga:landingPagePath=~^/Accessories/Coffee");
    return dataQuery;
  }

  /**
   * Returns a new AnalyticsService object authorized using the Client Login
   * authorization routine.
   * @return An AnalyticsService object.
   */
  public static AnalyticsService getAnalyticsService() {
    AnalyticsService analyticsService = new AnalyticsService(APP_NAME);
    try {
      analyticsService.setUserCredentials(USERNAME, PASSWORD);
    } catch (AuthenticationException e) {
      handleException(e);
    }
    return analyticsService;
  }

  /**
   * Returns the data feed object from the Google Analytics API. If an error
   * occurs a message is printed and the program is terminated.
   * @param dataQuery The query to execute to the Google Analytics API.
   * @return A DataFeed object with all the data returned from the API.
   */
  public static DataFeed getDataFeed(DataQuery dataQuery) {
    try {

      return getAnalyticsService().getFeed(dataQuery, DataFeed.class);

    } catch (IOException e) {
      handleException(e);
    } catch (ServiceException e) {
      handleException(e);
    }
    return null;
  }

  /**
   * Generic Exception handler for this demo to print the exception message
   * and terminate the program.
   * @param exception The exception be printed.
   */
  public static void handleException(Exception exception) {
    System.out.println("There was an error: " + exception.getMessage());
    System.exit(0);
  }
}

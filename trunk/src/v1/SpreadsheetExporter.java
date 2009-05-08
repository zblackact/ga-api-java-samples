// Copyright 2009 Google Inc. All Rights Reserved.
 
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package com.google.analytics.frontend.feed.examples;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates using the Analytics Data Export API along with the Google
 * Spreadsheets API to retrieve Analytics data in spreadsheet form. To use it,
 * create a Google Spreadsheet. In the header columns, type in the names of
 * some Analytics dimensions and metrics, using the names from the API (e.g.,
 * ga:browser, ga:city, ga:bounces, etc). Then run this program to populate
 * the spreadsheet columns with those headers with Analytics data.
 */
public class SpreadsheetExporter {

  public static void main(String[] args) throws ServiceException, IOException {

    if (args.length != 5) {
      System.err.println("Usage: SpreadsheetExporter "
      + "[username] "
      + "[spreadsheet key to update] "
      + "[Analytics table ID to draw data from] "
      + "[start of date range to draw data from, in format YYYY-MM-DD] "
      + "[end of date range to draw data from, in format YYYY-MM-DD] ");
      return;
    }

    // The user's Google user name and password. This account must have
    // permission to view the data in the Analytics profile
    // and must have full permissions on the spreadsheet to update.
    String username = args[0];
    String password
        = String.valueOf(System.console().readPassword("Enter password for user %1$s: ", username));

    // The key of the spreadsheet to update. For a spreadsheet with URL
    //   http://spreadsheets.google.com/ccc?key=[...]&hl=en
    // the part marked [...] is the spreadsheet's key.
    String spreadsheetKey = args[1];

    // The table ID of the Analytics data to populate the spreadsheet with.
    // Table IDs can be retrieved separately from the Analytics Data API
    // accounts feed. In the Java client library, you can get a table ID
    // for the profile represented by a particular AccountEntry as follows:
    //   String tableId = entry.getTableId();
    String tableId = args[2];

    // The start and end dates for data to retrieve. These should be of the
    // form YYYY-MM-DD.
    String startDate = args[3];
    String endDate = args[4];

    SpreadsheetService spreadsheetService = new SpreadsheetService(APP_NAME);
    spreadsheetService.setUserCredentials(username, password);
    // Workaround for Google Data APIs Java client issue #103
    // (http://code.google.com/p/gdata-java-client/issues/detail?id=103)
    spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V1);

    AnalyticsService analyticsService = new AnalyticsService(APP_NAME);
    analyticsService.setUserCredentials(username, password);

    SpreadsheetExporter exporter = new SpreadsheetExporter(spreadsheetService, analyticsService);
    exporter.exportToSpreadsheet(spreadsheetKey, tableId, startDate, endDate);
  }

  /** The application name to report to the Google Data APIs */
  private static final String APP_NAME = "google-analytics-spreadsheet-example-1";

  /** The service to use for interacting with Google Spreadsheets */
  private final SpreadsheetService spreadsheetService;

  /** The service to use for interacting with Google Analytics */
  private final AnalyticsService analyticsService;

  /**
   * Constructor.
   *
   * @param spreadsheetService The service to use for interacting with Google
   *     Spreadsheets.
   * @param analyticsService The service to use for interacting with Google
   *     Analytics.
   */
  public SpreadsheetExporter(SpreadsheetService spreadsheetService,
                             AnalyticsService analyticsService) {

    this.spreadsheetService = spreadsheetService;
    this.analyticsService = analyticsService;
  }

  /**
   * Populates the default worksheet of the spreadsheet with the given key
   * with Analytics data named by the header columns of that spreadsheet.
   *
   * @param spreadsheetKey The key of the spreadsheet to populate with
   *     Analytics data.
   * @param tableId The Analytics table ID to pull data from
   * @param startDate The starting date to retrieve data from
   * @param endDate The ending date to retrieve data from
   * @throws IOException If a network error occurs while trying to communicate
   *     with Spreadsheets or Analytics
   * @throws ServiceException If an application-level protocol error occurs
   *     while trying to communicate with Spreadsheets or Analytics
   */
  public void exportToSpreadsheet(
      String spreadsheetKey, String tableId, String startDate, String endDate)
      throws IOException, ServiceException {

    SpreadsheetEntry spreadsheet = getSpreadsheetWithKey(spreadsheetKey);
    Worksheet worksheet = new Worksheet(spreadsheet.getDefaultWorksheet(), spreadsheetService);

    ColumnTypeMap columnTypeMap = readAnalyticsColumns(worksheet);
    List<DataEntry> dataToUpload = getAnalyticsData(tableId, columnTypeMap, startDate, endDate);
    addDataToWorksheet(worksheet, columnTypeMap, dataToUpload);
  }

  /**
   * Returns the SpreadsheetEntry for the spreadsheet with the given key.
   *
   * @throws IOException If a network error occurs while trying to communicate
   *     with Spreadsheets
   * @throws ServiceException If an application-level protocol error occurs
   *     while trying to communicate with Spreadsheets
   */
  private SpreadsheetEntry getSpreadsheetWithKey(String key) throws IOException, ServiceException {
    URL metafeedUrl = new URL("http://spreadsheets.google.com/feeds/spreadsheets/private/full");
    SpreadsheetFeed spreadsheetFeed = spreadsheetService.getFeed(
        metafeedUrl,
        SpreadsheetFeed.class);

    List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
    for (SpreadsheetEntry spreadsheet : spreadsheets) {
      if (spreadsheet.getKey().equals(key)) {
        return spreadsheet;
      }
    }
    throw new IllegalStateException("You don't have access to a spreadsheet with key " + key);    
  }

  /**
   * Determines which columns in the given worksheet should be populated with
   * Analytics data.
   *
   * @param worksheet The worksheet whose columns should be populated with data
   * @return A column type map representing the columns to populate and what
   *     Analytics data to populate those columns with
   */
  private ColumnTypeMap readAnalyticsColumns(Worksheet worksheet) {
    ColumnTypeMap columnTypeMap = new ColumnTypeMap();
    for (int columnIndex = 1; columnIndex < worksheet.getColCount(); columnIndex++) {
      // read the headers, which are all in the first row
      CellEntry entry = worksheet.getCell(1, columnIndex);
      Cell cell = entry.getCell();
      String value = cell.getValue();
      if (value != null) {
        value = value.trim();
        if (ANALYTICS_COLUMNS.containsKey(value)) {
          columnTypeMap.put(columnIndex, ANALYTICS_COLUMNS.get(value));
        }
      }
    }
    return columnTypeMap;
  }

  /**
   * Retrieves the requested Analytics data from the Analytics Data Export API.
   *
   * @param tableId The table ID to request data from
   * @param columnTypeMap The column type map representing the columns to
   *     retrieve
   * @param startDate The start date for the range to retrieve
   * @param endDate The end date for the range to retrieve
   * @return A list of DataEntries containing the requested information
   *
   * @throws IOException If a network error occurs while trying to communicate
   *     with Analytics
   * @throws ServiceException If an application-level protocol error occurs
   *     while trying to communicate with Analytics
   */
  private List<DataEntry> getAnalyticsData(
      String tableId,
      ColumnTypeMap columnTypeMap,
      String startDate,
      String endDate) throws IOException, ServiceException {

    DataQuery query = new DataQuery(new URL("https://www.google.com/analytics/feeds/data"));
    query.setIds(tableId);
    query.setDimensions(columnTypeMap.getColumnString(ColumnType.DIMENSION));
    query.setMetrics(columnTypeMap.getColumnString(ColumnType.METRIC));
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    DataFeed feed = analyticsService.getFeed(query, DataFeed.class);
    return feed.getEntries();
  }

  /**
   * Adds the given Analytics data to the given worksheet.
   *
   * @param worksheet The worksheet to update
   * @param columnTypeMap The columns within that worksheet to update
   * @param dataToUpload The Analytics data to upload
   *
   * @throws IOException If a network error occurs while trying to communicate
   *     with Spreadsheets
   * @throws ServiceException If an application-level protocol error occurs
   *     while trying to communicate with Spreadsheets
   */
  private void addDataToWorksheet(
      Worksheet worksheet,
      ColumnTypeMap columnTypeMap,
      List<DataEntry> dataToUpload) throws IOException, ServiceException {

    // It's possible that the worksheet doesn't yet have enough rows to hold
    // all the data we want to upload to it. We can only bulk-UPDATE cells,
    // we can't bulk-ADD them with pre-populated date, so if we don't already
    // have enough rows we first resize the worksheet, then update our cell
    // entry map with the newly-created cells. (We add 1 to the size of the
    // data to upload because our data starts on row 2. Row 1 is for column
    // headers.)
    if (worksheet.getRowCount() < (dataToUpload.size() + 1)) {
      worksheet.setRowCount(dataToUpload.size() + 1);
    }

    // modify all cells that need to be updated (start with 2; 1 is for column
    // headers)
    List<CellEntry> updatedCells = new LinkedList<CellEntry>();
    int rowNumber = 2;
    for (DataEntry dataEntry : dataToUpload) {
      for (Map.Entry<Integer, AnalyticsColumn> column : columnTypeMap.entrySet()) {
        int columnNumber = column.getKey();
        String dimensionName = column.getValue().getName();
        CellEntry cellEntry = worksheet.getCell(rowNumber, columnNumber);
        String value = dataEntry.stringValueOf(dimensionName);
        cellEntry.changeInputValueLocal(value);
        updatedCells.add(cellEntry);
      }
      rowNumber++;
    }

    // send the updates in chunks of 1000 to ensure we don't send too much
    // data in one batch
    List<List<CellEntry>> batches = chunkList(updatedCells, 1000);
    for (List<CellEntry> batch : batches) {
      CellFeed batchFeed = new CellFeed();
      for (CellEntry cellEntry : batch) {
        Cell cell = cellEntry.getCell();
        BatchUtils.setBatchId(cellEntry, "R" + cell.getRow() + "C" + cell.getCol());
        BatchUtils.setBatchOperationType(cellEntry, BatchOperationType.UPDATE);
        batchFeed.getEntries().add(cellEntry);
      }

      Link batchLink = worksheet.getBatchUpdateLink();
      CellFeed batchResultFeed = spreadsheetService.batch(new URL(batchLink.getHref()), batchFeed);
      // Make sure all the operations were successful.
      for (CellEntry entry : batchResultFeed.getEntries()) {
        if (!BatchUtils.isSuccess(entry)) {
          String batchId = BatchUtils.getBatchId(entry);
          BatchStatus status = BatchUtils.getBatchStatus(entry);
          System.err.println("Failed entry");
          System.err.println("\t" + batchId + " failed (" + status.getReason() + ") ");
          return;
        }
      }
    }
  }

  /****************************************************************************
   * HELPER CLASSES
   *
   * These classes are slightly smarter versions of more standard classes,
   * equipped with little bits of extra functionality that are useful for our
   * purposes.
   ****************************************************************************/

  /**
   * Wrapper around Spreadsheets Worksheet entries that adds some utility
   * methods useful for our purposes.
   */
  private static class Worksheet {
    private final SpreadsheetService spreadsheetService;
    private WorksheetEntry backingEntry;
    private CellFeed cellFeed;
    private int rows;
    private int columns;
    private CellEntry[][] cellEntries;

    Worksheet(WorksheetEntry backingEntry, SpreadsheetService spreadsheetService)
        throws IOException, ServiceException {

      this.backingEntry = backingEntry;
      this.spreadsheetService = spreadsheetService;
      this.rows = backingEntry.getRowCount();
      this.columns = backingEntry.getColCount();
      refreshCachedData();
    }

    /**
     * Presents the given cell feed as a map from row, column pair to CellEntry.
     */
    private void refreshCachedData() throws IOException, ServiceException {

      CellQuery cellQuery = new CellQuery(backingEntry.getCellFeedUrl());
      cellQuery.setReturnEmpty(true);
      this.cellFeed = spreadsheetService.getFeed(cellQuery, CellFeed.class);

      // A subtlety: Spreadsheets row,col numbers are 1-based whereas the
      // cellEntries array is 0-based. Rather than wasting an extra row and
      // column worth of cells in memory, we adjust accesses by subtracting
      // 1 from each row or column number.
      cellEntries = new CellEntry[rows][columns];
      for (CellEntry cellEntry : cellFeed.getEntries()) {
        Cell cell = cellEntry.getCell();
        cellEntries[cell.getRow() - 1][cell.getCol() - 1] = cellEntry;
      }
    }

    /**
     * Gets the cell entry corresponding to the given row and column.
     */
    CellEntry getCell(int row, int column) {
      return cellEntries[row - 1][column - 1];
    }

    /** Returns this worksheet's column count. */
    int getColCount() {
      return columns;
    }

    /** Returns this worksheet's row count. */
    int getRowCount() {
      return rows;
    }

    /** Sets this worksheets's row count. */
    void setRowCount(int newRowCount) throws IOException, ServiceException {
      rows = newRowCount;
      backingEntry.setRowCount(newRowCount);
      backingEntry = backingEntry.update();
      refreshCachedData();
    }

    /** Gets a link to the batch update URL for this worksheet. */
    Link getBatchUpdateLink() {
      return cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
    }
  }

  private static enum ColumnType { DIMENSION, METRIC }

  /**
   * Simple container class that represents an Analytics column by pairing its
   * name with its column type.
   */
  private static class AnalyticsColumn {
    private final String name;
    private final ColumnType type;

    AnalyticsColumn(String name, ColumnType type) {
      this.name = name;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public ColumnType getType() {
      return type;
    }
  }

  /**
   * ColumnTypeMap instances represent a group of spreadsheet columns that
   * should be populated with Analytics data. Each key is a spreadsheet column
   * index, and each value is the Analytics dimension or metric that the
   * spreadsheet column should be populated with.
   */
  private static class ColumnTypeMap extends LinkedHashMap<Integer, AnalyticsColumn> {

    /**
     * Gets a string representing the columns of a particular Analytics column
     * type represented as values in this map. The returned string is suitable
     * for supplying to the Analytics API as a dimension or metric specifier.
     */
    String getColumnString(ColumnType columnType) {
      List<String> columnNames = new LinkedList<String>();
      for (AnalyticsColumn c : this.values()) {
        if (c.getType() == columnType) {
          columnNames.add(c.getName());
        }
      }
      return join(columnNames);
    }

    /**
     * Small utility method that joins the list of strings, separating with a
     * comma between each pair.
     */
    private static String join(List<String> strings) {
      Iterator<String> stringIterator = strings.iterator();
      if (!stringIterator.hasNext()) {
        return "";
      }

      StringBuilder s = new StringBuilder(stringIterator.next());
      while (stringIterator.hasNext()) {
        s.append(",").append(stringIterator.next());
      }
      return s.toString();
    }
  }

  /****************************************************************************
   * MISCELLANEOUS HELPERS
   ***************************************************************************/

  /**
   * A set naming every column that should be considered a dimension or metric.
   */
  private static final Map<String, AnalyticsColumn> ANALYTICS_COLUMNS;
  static {
    String[] dimensions = new String[] {
        "ga:date",
        "ga:hour",
        "ga:month",
        "ga:week",
        "ga:year",
        "ga:adGroup",
        "ga:adSlot",
        "ga:adSlotPosition",
        "ga:affiliation",
        "ga:browser",
        "ga:browserVersion",
        "ga:campaign",
        "ga:city",
        "ga:connectionSpeed",
        "ga:content",
        "ga:continent",
        "ga:country",
        "ga:day",
        "ga:daysSinceLastVisit",
        "ga:daysToTransaction",
        "ga:networkDomain",
        "ga:flashVersion",
        "ga:searchUsed",
        "ga:hostname",
        "ga:searchKeyword",
        "ga:searchKeywordRefinement",
        "ga:searchCategory",
        "ga:javaSupport",
        "ga:keyword",
        "ga:language",
        "ga:latitude",
        "ga:longitude",
        "ga:medium",
        "ga:networkLocation",
        "ga:pageDepth",
        "ga:pageTitle",
        "ga:operatingSystem",
        "ga:operatingSystemVersion",
        "ga:productSku",
        "ga:productName",
        "ga:productCategory",
        "ga:referralPath",
        "ga:region",
        "ga:pagePath",
        "ga:landingPagePath",
        "ga:searchStartPage",
        "ga:exitPagePath",
        "ga:searchDestinationPage",
        "ga:screenColors",
        "ga:screenResolution",
        "ga:source",
        "ga:sourceMedium",
        "ga:subContinent",
        "ga:transactionId",
        "ga:userDefinedValue",
        "ga:visitorType"
    };

    String[] metrics = new String[] {
        "ga:totalAbandonmentRate",
        "ga:abandonedFunnels",
        "ga:goal1AbandonmentRate",
        "ga:goal2AbandonmentRate",
        "ga:goal3AbandonmentRate",
        "ga:goal4AbandonmentRate",
        "ga:costPerGoalConversion",
        "ga:costPerTransaction",
        "ga:costPerConversion",
        "ga:goalConversionRate",
        "ga:goal1AbandonedFunnels",
        "ga:goal2AbandonedFunnels",
        "ga:goal3AbandonedFunnels",
        "ga:goal4AbandonedFunnels",
        "ga:perVisitGoalValue",
        "ga:visitors",
        "ga:rpc",
        "ga:bounces",
        "ga:bounceRate",
        "ga:adClicks",
        "ga:CTR",
        "ga:adCost",
        "ga:CPM",
        "ga:CPC",
        "ga:entrances",
        "ga:exits",
        "ga:percentExit",
        "ga:totalGoalCompletions",
        "ga:goal1Completions",
        "ga:goal1ConversionRate",
        "ga:goal2Completions",
        "ga:goal2ConversionRate",
        "ga:goal3Completions",
        "ga:goal3ConversionRate",
        "ga:goal4Completions",
        "ga:goal4ConversionRate",
        "ga:totalGoalStarts",
        "ga:goal1Starts",
        "ga:goal2Starts",
        "ga:goal3Starts",
        "ga:goal4Starts",
        "ga:totalGoalValue",
        "ga:goal1Value",
        "ga:goal2Value",
        "ga:goal3Value",
        "ga:goal4Value",
        "ga:impressions",
        "ga:newVisits",
        "ga:percentNewVisits",
        "ga:timeOnPage",
        "ga:avgTimeOnPage",
        "ga:pageviews",
        "ga:pagesPerVisit",
        "ga:itemQuantity",
        "ga:itemRevenue",
        "ga:uniquePurchases",
        "ga:transactionRevenue",
        "ga:averageValue",
        "ga:perVisitValue",
        "ga:searchDepth",
        "ga:searchDuration",
        "ga:searchExits",
        "ga:searchRefinements",
        "ga:transactionShipping",
        "ga:transactionTax",
        "ga:transactions",
        "ga:searchUniques",
        "ga:uniquePageviews",
        "ga:timeOnSite",
        "ga:avgTimeOnSite",
        "ga:visits",
        "ga:searchVisits"
    };

    ANALYTICS_COLUMNS = new HashMap<String, AnalyticsColumn>();
    for (String dimensionName : dimensions) {
      ANALYTICS_COLUMNS.put(
          dimensionName,
          new AnalyticsColumn(dimensionName, ColumnType.DIMENSION));
    }
    for (String metricName : metrics) {
      ANALYTICS_COLUMNS.put(
          metricName,
          new AnalyticsColumn(metricName, ColumnType.METRIC));
    }
  }

  /**
   * Chunks a list of items into sublists where each sublist contains at most
   * the specified maximum number of items.
   *
   * @param ts The list of elements to chunk
   * @param chunkSize The maximum number of elements per sublist
   * @return A list of sublists, where each sublist has chunkSize or fewer elements
   *     and all elements from ts are present, in order, in some sublist
   */
  private static <T> List<List<T>> chunkList(List<? extends T> ts, int chunkSize) {
    Iterator<? extends T> iterator = ts.iterator();
    List<List<T>> returnList = new LinkedList<List<T>>();
    while (iterator.hasNext()) {
      List<T> sublist = new LinkedList<T>();
      for (int i = 0; i < chunkSize && iterator.hasNext(); i++) {
        sublist.add(iterator.next());
      }
      returnList.add(sublist);
    }
    return returnList;
  }
}

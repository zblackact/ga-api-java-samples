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

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Implements the ResultManager Interface. This will handle results from the API
 * which has multiple rows. Since the Data Export API will not return data for
 * days that have no data, this also handles back filling data.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class ResultManagerGroupImpl implements ResultManager {

  private static final long millisInDay = 24 * 60 * 60 * 1000;

  private Results results;
  private SimpleDateFormat resultDateFormat;
  private Calendar calendar;

  /**
   * Constructor.
   * Creates a new dimple date format and gets a calendar instance.
   */
  public ResultManagerGroupImpl() {
    super();
    resultDateFormat = new SimpleDateFormat("yyyyMMdd");
    calendar = Calendar.getInstance();
  }

  /**
   * Sets the initialized result object to add the data to.
   */
  @Override
  public void setResults(Results results) {
    this.results = results;
  }

  /**
   * Parses a data feed into a table of integers. This method finds each
   * row in the results. It adds each row name to the list of row names.
   * It also ensure metric values for dates not returned by the API are
   * properly set to 0. Finally it detects if any sampling has occurred
   * for the data.
   * @param feed The Data Export API response object filled with data.
   */
  @Override
  public void addRows(DataFeed feed) {
    String startDate = DataQueryUtil.getResultDateFormat(results.getColNames().get(0));
    String expectedDate = "";
    String dimensionValue = "";
    boolean isSampled = false;
    List<Double> row = null;
    int numCols = results.getNumCols();

    for (DataEntry entry : feed.getEntries()) {
      String tmpDimValue = entry.getDimensions().get(0).getValue();

      if (!tmpDimValue.equals(dimensionValue)) {  // Detect beginning of a row.
        if (row != null) {
          forwardFillRow(numCols, row);
          results.addRow(dimensionValue, row);  // Add the row.
        }
        row = new ArrayList<Double>(numCols);  // Create a new row.
        dimensionValue = tmpDimValue;
        expectedDate = startDate;
      }

      String foundDate = entry.getDimension("ga:date").getValue();  // Backfill row.
      if (!foundDate.equals(expectedDate)) {
        backFillRow(expectedDate, foundDate, row);
      }

      // Handle the data.
      Metric metric = entry.getMetrics().get(0);
      if (!isSampled && 0 != metric.getConfidenceInterval()) {
        isSampled = true;
      }
      row.add(new Double(metric.getValue()));
      expectedDate = getNextDate(foundDate);
    }

    // Add the last row.
    if (row != null) {
      forwardFillRow(numCols, row);
      results.addRow(dimensionValue, row);
    }
    results.setIsSampled(isSampled);
  }

  /**
   * Adds 0 values in the row List for the number of days between start and
   * end date. If both dates are equal or if start date is greater than end
   * date, nothing happens.
   * @param startDate The expected date.
   * @param endDate The found date.
   * @param row The current row being added to the table.
   */
  public void backFillRow(String startDate, String endDate, List<Double> row) {
    long d1 = 0;
    long d2 = 0;

    try {
      calendar.setTime(resultDateFormat.parse(startDate));
      d1 = calendar.getTimeInMillis() / millisInDay;

      calendar.setTime(resultDateFormat.parse(endDate));
      d2 = calendar.getTimeInMillis() / millisInDay;

    } catch (ParseException e) {
      System.out.println("Result Table Date Error: " + e.getMessage());
    }

    long differenceInDays = d2 - d1;
    if (differenceInDays > 0) {
      for (int i = 0; i < differenceInDays; i++) {
        row.add(0.0);
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
      System.out.println("Result Table Date Error: " + e.getMessage());
    }
    return "";
  }

  /**
   * Pads the end of the list with 0 values until the final row length is
   * rowSize.
   * @param rowSize The expected size of the row List.
   * @param row The row to add 0 values to. Must not be null.
   */
  public void forwardFillRow(int rowSize, List<Double> row) {
    if (rowSize < 0 || row == null) {
      return;
    }

    int remainingElements = rowSize - row.size();
    if (remainingElements > 0) {
      for (int i = 0; i < remainingElements; i++) {
        row.add(0.0);
      }
    }
  }
}

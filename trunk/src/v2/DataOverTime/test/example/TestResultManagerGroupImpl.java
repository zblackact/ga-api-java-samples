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

import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataFeed;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for ResultManagerGroupImpl. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestResultManagerGroupImpl extends TestCase {

  private DataQuery dataQuery;
  private List<String> dimensionValues;
  private AnalyticsServiceMock asMock;
  private ResultManagerGroupImpl resultManager;
  private Results results;
  private List<List<Double>> table;
  private String[] dimensionNames;

  /**
   * Sets up the test.
   */
  public void setUp() {

    asMock = new AnalyticsServiceMock("TEST_APP");

    // Only used to initialize the results table.
    dimensionValues = new ArrayList<String>();

    dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-01-03");

    resultManager = new ResultManagerGroupImpl();
    dimensionNames = new String[] {"ga:landingPage", "ga:date"};
  }

  /**
   * Tests that addRows gets the dimension name from the result.
   */
  public void testAddRows_FindsDimensions() {
    dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-01-01");

    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "b", "c", "d"},
        {"20100101", "20100101", "20100101", "20100101"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    List<String> dimResults = results.getRowNames();
    assertEquals(expectedDimensionValues[0].length, dimResults.size());

    for (int i = 0; i < expectedDimensionValues[0].length; i++) {
      String expected = expectedDimensionValues[0][i];
      String result = dimResults.get(i);
      assertTrue(expected.equals(result));
    }
  }

  /**
   * Tests adding many rows.
   */
  public void testAddRows_manyRows() {

    dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-01-01");

    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "b", "c", "d"},
        {"20100101", "20100101", "20100101", "20100101"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(4, table.size());

    assertEquals(new Double(1), table.get(0).get(0));
    assertEquals(new Double(2), table.get(1).get(0));
    assertEquals(new Double(3), table.get(2).get(0));
    assertEquals(new Double(4), table.get(3).get(0));
  }

  /**
   * Tests finding 2 rows.
   */
  public void testAddRows_2Rows() {

    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "a", "b", "b", "b"},
        {"20100101", "20100102", "20100103", "20100101", "20100102", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4", "5", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());

    assertEquals(3, table.get(0).size());
    assertEquals(3, table.get(1).size());

    assertEquals(new Double(3), table.get(0).get(2));
    assertEquals(new Double(4), table.get(1).get(0));
  }

  /**
   * Tests the first row's first two values are back filled.
   */
  public void testAddRows_backFill() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "b", "b", "b"},
        {"20100103", "20100101", "20100102", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"3", "4", "5", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());
    assertEquals(3, table.get(0).size());
    assertEquals(new Double(0), table.get(0).get(0));
    assertEquals(new Double(0), table.get(0).get(1));
  }

  /**
   * Tests the first row's middle value is back filled.
   */
  public void testAddRows_backFillFirstMiddleValue() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "b", "b", "b"},
        {"20100101", "20100103", "20100101", "20100102", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"1", "3", "4", "5", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());
    assertEquals(3, table.get(0).size());
    assertEquals(new Double(0), table.get(0).get(1));
  }

  /**
   * Tests the second row's first two values are back filled.
   */
  public void testAddRows_backFillLastRow() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "a", "b"},
        {"20100101", "20100102", "20100103", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());
    assertEquals(3, table.get(1).size());
    assertEquals(new Double(0), table.get(1).get(0));
    assertEquals(new Double(0), table.get(1).get(1));
  }

  /**
   * Tests the second row's middle value is back filled.
   */
  public void testAddRows_backfillLastRowMiddleValue() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "a", "b", "b"},
        {"20100101", "20100102", "20100103", "20100101", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());
    assertEquals(3, table.get(1).size());
    assertEquals(new Double(0), table.get(1).get(1));
  }

  /**
   * Test 0's are added if the last date in the first row is less than
   * the last day in the date range.
   */
  public void testAddRows_forwardFillFirstRow() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "b", "b", "b"},
        {"20100101", "20100101", "20100102", "20100103"}};
    String[][] expectedMetricValues = new String[][] {{"1", "4", "5", "6"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
         expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());

    int expectedSize = DataQueryUtil.getNumberOfDays(dataQuery);
    assertEquals(expectedSize, table.get(0).size());
    assertEquals(new Double(0), table.get(0).get(1));
    assertEquals(new Double(0), table.get(0).get(2));
  }

  /**
   * Test 0s are added if the last date in the final row is less than the
   * last day in the date range.
   */
  public void testAddRows_forwardFillSecondRow() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "a", "b"},
        {"20100101", "20100102", "20100103", "20100101"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues);
    resultManager.addRows(feed);

    table = results.getTable();
    assertEquals(2, table.size());

    int expectedSize = DataQueryUtil.getNumberOfDays(dataQuery);
    assertEquals(expectedSize, table.get(1).size());
    assertEquals(new Double(0), table.get(1).get(1));
    assertEquals(new Double(0), table.get(1).get(2));
  }

  /**
   * Ensure confidence interval flag gets properly set.
   */
  public void testAddRows_confidenceIntervalSet() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    String[][] expectedDimensionValues = new String[][] {{"a", "a", "a", "b"},
        {"20100101", "20100102", "20100103", "20100101"}};
    String[][] expectedMetricValues = new String[][] {{"1", "2", "3", "4"}};

    DataFeed feed = asMock.getDataFeed(dimensionNames, expectedDimensionValues,
        expectedMetricValues, 5.5);
    resultManager.addRows(feed);

    assertTrue(results.getIsSampled());
  }

  /**
   * Tests that 0 values will be added between two dates.
   */
  public void testBackFillRow() {
    List<Double> row = new ArrayList<Double>();
    resultManager.backFillRow("20100101", "20100105", row);
    assertEquals(4, row.size());

    row.clear();
    resultManager.backFillRow("20100101", "20100101", row);
    assertEquals(0, row.size());

    row.clear();
    resultManager.backFillRow("20100105", "20100101", row);
    assertEquals(0, row.size());
  }

  /**
   * Tests back filling rows across date boundries.
   * Day should be:
   * 20100629, 20100630, 20100701, 20100702
   */
  public void testBackFillRow_acrossDateBoundries() {
    List<Double> row = new ArrayList<Double>();
    resultManager.backFillRow("20100629", "20100702", row);
    assertEquals(3, row.size());
  }

  /**
   * Test getting the next date.
   */
  public void testGetNextDate() {
    assertEquals("20100102", resultManager.getNextDate("20100101"));
  }

  /**
   * Tests adding a set number of values to the end of a row.
   */
  public void testForwardFillRow() {
    List<Double> row = null;

    resultManager.forwardFillRow(3, row);
    assertNull(row);

    row = new ArrayList<Double>();
    resultManager.forwardFillRow(-1, row);
    assertEquals(0, row.size());

    resultManager.forwardFillRow(3, row);
    assertEquals(3, row.size());
    assertEquals(new Double(0), row.get(2));
  }
}

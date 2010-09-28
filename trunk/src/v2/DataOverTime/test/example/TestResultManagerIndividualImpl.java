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
 * Test suite for ResultManagerIndividualImpl. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestResultManagerIndividualImpl extends TestCase {

  private DataQuery dataQuery;
  private AnalyticsServiceMock asMock;
  private List<String> dimensionValues;
  private ResultManagerIndividualImpl resultManager;
  private Results results;
  private DataFeed testFeed;
  private String[] dimensionNames;
  private String[][] testDimensions;
  private String[][] testMetrics;

  /**
   * Sets up this test.
   */
  public void setUp() {

    resultManager = new ResultManagerIndividualImpl();
    dataQuery = TestUtil.getFilledDataQuery();

    String[] initialDimValues = new String[] {"20100101", "20100102", "20100103", "20100104"};
    dimensionValues = new ArrayList<String>();
    dimensionValues.addAll(TestUtil.toList(initialDimValues));

    asMock = new AnalyticsServiceMock("TEST_APP");

    dimensionNames = new String[] {"ga:date"};
    testDimensions = new String[][] {initialDimValues};
    testMetrics = new String[][] {{"1", "2", "3", "4"}};
    testFeed = asMock.getDataFeed(dimensionNames, testDimensions, testMetrics);
  }

  /**
   * Tests the metrics are properly added.
   */
  public void testAddRows_metricsAdded() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);
    resultManager.addRows(testFeed);

    List<List<Double>> table = results.getTable();
    for (int i = 0; i < table.get(0).size(); i++) {
      Double value = new Double(testMetrics[0][i]);
      assertEquals(value, table.get(0).get(i));
    }
  }

  /**
   * Tests the dimension values are properly added.
   */
  public void testAddRows_rowNamesAdded() {
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);
    resultManager.addRows(testFeed);

    assertEquals(dimensionValues.size(), results.getOriginalDimensionValues().size());
    String expected = results.getOriginalDimensionValues().get(0);
    String result = testDimensions[0][0];
    assertTrue(expected.equals(result));
  }

  /**
   * Tests Confidence Interval is set.
   */
  public void testAddRows_confidenceIntervalSet() {
    testFeed = asMock.getDataFeed(dimensionNames, testDimensions, testMetrics, 5.0);
    results = new Results();
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);
    resultManager.addRows(testFeed);

    assertTrue(results.getIsSampled());
  }
}

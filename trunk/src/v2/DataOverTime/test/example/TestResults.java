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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for TestResults. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestResults extends TestCase {

  /**
   * Tests initializing the table.
   */
  public void testInitTable() {

    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    int numDays = DataQueryUtil.getNumberOfDays(dataQuery);

    List<String> dimensionValues = new ArrayList<String>(2);
    dimensionValues.add("/foo");
    dimensionValues.add("/bar");

    Results resultTable = new Results();
    resultTable.initTable(dataQuery, dimensionValues);

    String expectedDim = dataQuery.getDimensions();
    String resultDim = resultTable.getDimensionName();
    assertEquals(expectedDim, resultDim);

    assertEquals(numDays, resultTable.getNumCols());
    assertEquals(dimensionValues.size(), resultTable.getNumRows());
    assertEquals(numDays, resultTable.getColNames().size());
    assertNotNull(resultTable.getRowNames());

    // Check the colNames have been set properly.
    List<String> expectedColumnNames = DataQueryUtil.getListOfDates(dataQuery);
    for (int i = 0; i < expectedColumnNames.size(); i++) {
      String expected = expectedColumnNames.get(i);
      String result = resultTable.getColNames().get(i);
      assertEquals(expected, result);
    }
  }
}

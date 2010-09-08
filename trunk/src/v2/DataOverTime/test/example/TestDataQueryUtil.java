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

import java.util.List;
/**
 * Test suite for the DataQueryUtil. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestDataQueryUtil extends TestCase {

  /**
   * Tests getting the number of days in a date range.
   */
  public void testGetNumberOfDays() {
    DataQuery dataQuery1 = TestUtil.getNewDataQuery();
    dataQuery1.setStartDate("2010-01-01");
    dataQuery1.setEndDate("2010-01-15");

    int numDays = DataQueryUtil.getNumberOfDays(dataQuery1);
    assertEquals(15, numDays);

    DataQuery dataQuery2 = TestUtil.getNewDataQuery();
    dataQuery2.setStartDate("2010-02-01");
    dataQuery2.setEndDate("2010-04-02");

    numDays = DataQueryUtil.getNumberOfDays(dataQuery2);
    assertEquals(61, numDays);

    DataQuery dataQuery3 = TestUtil.getNewDataQuery();
    dataQuery3.setStartDate("2010-01-01");
    dataQuery3.setEndDate("2010-06-01");

    numDays = DataQueryUtil.getNumberOfDays(dataQuery3);
    assertEquals(152, numDays);
  }

  /**
   * Tests getting a list of dates in the date range. Inclusive.
   */
  public void testGetListOfDates() {
    DataQuery dataQuery = TestUtil.getNewDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-06-01");

    List<String> testList = DataQueryUtil.getListOfDates(dataQuery);
    assertNotNull(testList);
    int expectedSize = DataQueryUtil.getNumberOfDays(dataQuery);
    assertEquals(expectedSize, testList.size());

    String result = testList.get(0);
    assertTrue("2010-01-01".equals(result));

    result = testList.get(testList.size() - 1);
    assertTrue("2010-06-01".equals(result));
  }

  /**
   * Tests transforming a date format found in the DataQuery to the
   * format returned from the API.
   */
  public void testGetResultDateFormat() {
    String test = DataQueryUtil.getResultDateFormat("2010-10-10");
    assertTrue("20101010".equals(test));
  }
}

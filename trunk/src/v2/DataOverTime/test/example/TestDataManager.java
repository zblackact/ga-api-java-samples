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

import junit.framework.TestCase;

import java.util.List;

/**
 * Test suite for the DataManager. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestDataManager extends TestCase {

  private static final String APP_NAME = "TEST_APP";

  private String[][] testDimensions;
  private String[][] testMetrics;
  private DataManager dataManager;

  /**
   * Sets up the test,
   */
  public void setUp() {
    AnalyticsServiceMock analyticsServiceMock = new AnalyticsServiceMock(APP_NAME);
    testDimensions = new String[][] {{"foo", "bar", "baz", "bat", "boo"}};
    testMetrics = new String[][] {{"1", "2", "3", "4", "5"}};
    analyticsServiceMock.setData(testDimensions, testMetrics);

    dataManager = new DataManager(analyticsServiceMock);
  }

  /**
   * Ensure we can get a list of dimensions from a query.
   */
  public void testGetDimensions() {
    List<String> output = dataManager.getDimensionValues(TestUtil.getNewDataQuery());

    assertTrue(output.size() > 0);

    for (int i = 0; i < output.size(); i++) {
      assertEquals(testDimensions[0][i], output.get(i));
    }
  }

  /**
   * Ensure we can get a feed.
   */
  public void testGetFeed() {
    DataFeed feed = dataManager.getFeed(TestUtil.getNewDataQuery());
    assertNotNull(feed);
    assertEquals(testMetrics[0].length, feed.getEntries().size());

    int i = 0;
    for (DataEntry entry : feed.getEntries()) {
      assertEquals(testDimensions[0][i], entry.getDimensions().get(0).getValue());
      assertEquals(testMetrics[0][i], entry.getMetrics().get(0).getValue());
      i++;
    }
  }
}

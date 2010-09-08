
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gdata.client.analytics.DataQuery;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestDataOverTime extends TestCase {

  private static final String APP_NAME = "Test_App";
  private static final String DATA_FEED_BASE_URL = "https://www.google.com/analytics/feeds/data";

  private DataQuery dataQuery;
  private DataOverTime dataOverTime;
  private AnalyticsServiceMock analyticsServiceMock;
  private DataManagerMock dataManagerMock;
  private List<String> testDimsList;
  private List<List<String>> testMetsTable;

  public TestDataOverTime(String name) {
    super(name);
  }
/*
  public void setUp() {
    dataQuery = getFilledDataQuery();
    
    analyticsServiceMock = new AnalyticsServiceMock(APP_NAME);
    dataManagerMock = new DataManagerMock(analyticsServiceMock);

    testDimsList = new ArrayList<String>();
    testDimsList.add("/foo");
    testDimsList.add("/bar");    
    dataManagerMock.setDimensions(testDimsList);
    
    testMetsTable = new ArrayList<List<String>>();
    testMetsTable.add(toList(new String[] {"1", "2", "3", "4", "5"}));
    testMetsTable.add(toList(new String[] {"6", "7", "8", "9", "0"}));
    dataManagerMock.setFeedData(testMetsTable);

    dataOverTime = new DataOverTime(dataManagerMock, new QueryManager(), new ResultTableImpl());
  }
*/
  /**
   * Ensure table size is being properly set.
   */
/*
  public void testGetData1() {
    
    List<List<Integer>> table = dataOverTime.getData(dataQuery).getTable();
    assertEquals(2, table.size());

    testDimsList.add("/baz");
    dataManagerMock.setDimensions(testDimsList);

    testMetsTable.add(toList(new String[] {"1", "2", "3", "4", "5"}));
    dataManagerMock.setFeedData(testMetsTable);

    table = dataOverTime.getData(dataQuery).getTable();
    assertEquals(3, table.size());
  }
  
  public void testGetData2() {

    List<List<Integer>> table = dataOverTime.getData(dataQuery).getTable();

    int i = 0;
    for (List<Integer> row : table) {
      int j = 0;
      for (Integer value : row) {
        assertEquals(testMetsTable.get(i).get(j), value);
        j++;
      }
      i++;
    }  
  }
  
  private List<String> toList(String[] data) {
    return new ArrayList<String>(Arrays.asList(data));
  }
  
  private DataQuery getNewDataQuery() {
    try {
      return new DataQuery(new URL(DATA_FEED_BASE_URL));
    } catch (MalformedURLException e) { fail(); }
    return null;
  }
  
  private DataQuery getFilledDataQuery() {
    DataQuery dataQuery = getNewDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-01-15");
    dataQuery.setDimensions("ga:landingPagePath");
    dataQuery.setMetrics("ga:entrances");
    return dataQuery;
  }

  public static Test suite() {
    return new TestSuite(TestDataOverTime.class);
  }*/
}

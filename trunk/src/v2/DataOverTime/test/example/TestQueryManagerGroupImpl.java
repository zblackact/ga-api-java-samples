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
 * Test suite for QueryManagerGroupImpl. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestQueryManagerGroupImpl extends TestCase {

  private BucketManager bucketManager;
  private QueryManagerGroupImpl queryManager;

  /**
   * Sets up the test.
   */
  public void setUp(){
    bucketManager = new BucketManager();
    queryManager = new QueryManagerGroupImpl(bucketManager);
  }

  /**
   * Test getting filtered queries.
   */
  public void testGetFilteredQueries() {
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    List<String> dimensionValues = new ArrayList<String>();

    FilteredQueries queries = queryManager.getFilteredQueries(dataQuery, dimensionValues);
    assertNotNull(queries);

    DataQuery testQuery = queries.getQuery();
    assertNotNull(testQuery);

    List<String> filterList = queries.getFilterList();
    assertNotNull(filterList);
  }

  /**
   * Tests that the bucket manager gets initialized.
   */
  public void testGetFilteredQueries_bucketManagerInitialized() {
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    List<String> dimensionValues = new ArrayList<String>();
    dimensionValues.add("google");

    queryManager.getFilteredQueries(dataQuery, dimensionValues);
  }


  /**
   * Tests getting the new updated query.
   */
  public void testUpdateQuery() {
    String dimensionValue = "ga:source";
    DataQuery dataQuery = TestUtil.getNewDataQuery();
    dataQuery.setDimensions(dimensionValue);

    queryManager.updateQuery(dataQuery);

    String expectedDim = dimensionValue + ",ga:date";
    assertTrue(expectedDim.equals(dataQuery.getDimensions()));

    assertTrue(expectedDim.equals(dataQuery.getSort()));

    assertEquals(10000, dataQuery.getMaxResults());
    assertEquals(-1, dataQuery.getStartIndex());
  }

  /**
   * Tests update query works if the filter parameter is null.
   */
  public void testUpdateQuery_noFilter() {
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setFilters(null);

    queryManager.updateQuery(dataQuery);
    assertNull(dataQuery.getFilters());
  }

  /**
   * Tests if a filter has already been set.
   */
  public void testUpdateQuery_filterIsSet() {
    String filter = "ga:visits>5";
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setFilters(filter);

    queryManager.updateQuery(dataQuery);

    String expectedFilter = filter + ";";
    assertTrue(expectedFilter.equals(dataQuery.getFilters()));
  }

  /**
   * Tests getting the maximum filter list length.
   */
  public void testGetFilterMaxListSize() {
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setStartDate("2005-01-01");
    dataQuery.setEndDate("2010-06-01");

    int numDays = DataQueryUtil.getNumberOfDays(dataQuery);
    int maxResults = 2000;
    int expectedValue = maxResults / numDays;

    assertEquals(expectedValue, queryManager.getFilterMaxListSize(maxResults, dataQuery));
  }

  /**
   * Tests getting the maximum character length.
   */
  public void testGetFilterMaxCharLength_noFilter() {
    int maxQueryLen = QueryManagerGroupImpl.MAX_QUERY_LEN;

    DataQuery dataQueryExpected = TestUtil.getFilledDataQuery();
    dataQueryExpected.setFilters("");
    int expectedSize = maxQueryLen - dataQueryExpected.getUrl().toString().length();

    DataQuery dataQueryTest = TestUtil.getFilledDataQuery();
    dataQueryTest.setFilters(null);

    assertEquals(expectedSize, queryManager.getFilterMaxCharLength(maxQueryLen, dataQueryTest));
  }

  /**
   * Tests getting the maximum character length.
   */
  public void testGetFilterMaxCharLength_withFilter() {
    int maxQueryLen = QueryManagerGroupImpl.MAX_QUERY_LEN;

    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setFilters("ga:source==google;");

    int expectedSize =  maxQueryLen - dataQuery.getUrl().toString().length();

    assertEquals(expectedSize, queryManager.getFilterMaxCharLength(maxQueryLen, dataQuery));
  }

  /**
   * Ensures the filters in the DataQuery remain intact.
   */
  public void testSetFilterMaxCharLength_preserveFilter() {
    String filters = "ga:source==foo;ga:visits>5;";
    DataQuery dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setFilters(filters);

    queryManager.getFilterMaxCharLength(2000, dataQuery);
    String result = dataQuery.getFilters();
    assertTrue(filters.equals(result));
  }

  /**
   * Tests the get filter list handles invalid parameters.
   */
  public void testGetFilterList_handlesInvalidParameters() {
    List<String> filterList = queryManager.getFilterList("", null);
    assertNotNull(filterList);

    String dimensionName = "ga:source";
    List<String> dimensionList = new ArrayList<String>();
    dimensionList.add(dimensionName);

    filterList = queryManager.getFilterList("", dimensionList);
    assertNotNull(filterList);
    assertEquals(0, filterList.size());

    // Should return 0 because bucket manager has not been initialized.
    filterList = queryManager.getFilterList(dimensionName, dimensionList);
    assertNotNull(filterList);
    assertEquals(0, filterList.size());

    filterList = queryManager.getFilterList(dimensionName, null);
    assertNotNull(filterList);
    assertEquals(0, filterList.size());

    List<String> emptyList = new ArrayList<String>();
    filterList = queryManager.getFilterList(dimensionName, emptyList);
    assertNotNull(filterList);
    assertEquals(0, filterList.size());
  }

  /**
   * Tests getFilterList returns data. Since the bucket manager is initialized
   * with very large values of maxChars and maxResults, the bucketManager
   * algorithm puts all filters in one string concatenated by OR operators.
   * The actual test of ensuring filters are properly put into buckets occurs
   * in bucketManager. Since bucketManager sorts the list of dimensions as
   * part of it's algorithm, the dimension values in the final filterList
   * value has to be ordered from largest to smallest for this test to pass.
   */
  public void testGetFilterList_returnsData() {
    String dimensionName = "ga:medium";

    List<String> dimensionList = new ArrayList<String>();
    dimensionList.add("cpc");
    dimensionList.add("organic");
    dimensionList.add("referral");

    bucketManager.init(10000, 10000);
    List<String> result = queryManager.getFilterList(dimensionName, dimensionList);

    assertEquals(1, result.size());
    assertEquals("ga:medium==referral,ga:medium==organic,ga:medium==cpc", result.get(0));
  }
}

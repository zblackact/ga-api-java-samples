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
import java.util.Iterator;
import java.util.List;

/**
 * Test suite for QueryManagerIndividualImpl. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestQueryManagerIndividualImlp extends TestCase {

  private QueryManagerIndividualImpl queryManager;
  private FilteredQueries queries;

  /**
   * Sets up the test.
   */
  public void setUp() {
    queryManager = new QueryManagerIndividualImpl();
  }

  /**
   * Tests getting filtered queries.
   */
  public void testGetFilteredQueries() {

    DataQuery dataQueryTest = TestUtil.getFilledDataQuery();
    String dimensionName = "ga:landingPagePath";
    dataQueryTest.setDimensions(dimensionName);

    List<String> dimensionValues = new ArrayList<String>();
    String dimensionValue = "/foo";
    dimensionValues.add(dimensionValue);

    // Ensure list of filters is being set.
    queries = queryManager.getFilteredQueries(dataQueryTest, dimensionValues);
    assertNotNull(queries);
    assertNotNull(queries.getFilterList());
    assertEquals(dimensionValues.size(), queries.getFilterList().size());

    String expectedFilter = Filter.getEqualityFilter(dimensionName, dimensionValue);
    assertEquals(expectedFilter, queries.getFilterList().get(0));

    // Ensure query is updated.
    DataQuery resultQuery = queries.getFilteredQuery(0);

    DataQuery expectedQuery = TestUtil.getFilledDataQuery();
    expectedQuery.setDimensions(dimensionName);

    queryManager.updateQuery(expectedQuery);

    assertEquals(expectedQuery.getMaxResults(), resultQuery.getMaxResults());
    assertTrue(expectedQuery.getSort().equals(resultQuery.getSort()));

    String expectedDim = expectedQuery.getDimensions();
    String resultDim = resultQuery.getDimensions();
    assertTrue(expectedDim.equals(resultDim));
  }

  /**
   * Tests getting that getFiltered work in a loop.
   */
  public void testGetFilteredQueries_InALoop() {
    DataQuery dataQueryTest = TestUtil.getFilledDataQuery();
    String dimensionName = "ga:landingPagePath";
    dataQueryTest.setDimensions(dimensionName);

    List<String> dimensionValues = new ArrayList<String>();
    dimensionValues.add("/foo");
    dimensionValues.add("/bar");
    dimensionValues.add("/bat");

    queries = queryManager.getFilteredQueries(dataQueryTest, dimensionValues);

    DataQuery expectedQuery = TestUtil.getFilledDataQuery();
    expectedQuery.setDimensions(dimensionName);
    queryManager.updateQuery(expectedQuery);

    Iterator<String> dimValueIter = dimensionValues.iterator();
    while (queries.hasNext()) {

      String dimensionValue = dimValueIter.next();
      String expectedFilter = Filter.getEqualityFilter(dimensionName, dimensionValue);
      expectedQuery.setFilters(expectedFilter);

      DataQuery testQuery = queries.next();
      assertEquals(expectedQuery.getUrl().toString(), testQuery.getUrl().toString());
    }
  }

  /**
   * Tests updating the query.
   */
  public void testGetUpdatedQuery() {
    DataQuery query = TestUtil.getFilledDataQuery();
    queryManager.updateQuery(query);

    int maxResults = DataQueryUtil.getNumberOfDays(query);
    assertEquals(maxResults, query.getMaxResults());
    assertTrue("ga:date".equals(query.getSort()));
    assertTrue("ga:date".equals(query.getDimensions()));
    assertEquals(-1, query.getStartIndex());
  }

  /**
   * Tests filters are properly updated.
   */
  public void testUpdateQuery_noFilter() {
    // Test if no filter is set.
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
    DataQuery dataQuery2 = TestUtil.getFilledDataQuery();
    dataQuery2.setFilters(filter);

    queryManager.updateQuery(dataQuery2);

    String expectedFilter = filter + ";";
    assertTrue(expectedFilter.equals(dataQuery2.getFilters()));
  }

  /**
   * Tests getting a filter list.
   */
  public void testGetFilterList() {

    String dimensionName = "ga:source";

    String[] testDims = new String[] {"foo", "bar"};
    List<String> dimensionValues = TestUtil.toList(testDims);

    List<String> filters = queryManager.getFilterList(dimensionName,
        dimensionValues);

    assertNotNull(filters);

    int i = 0;
    for (String filter : filters) {
      String expectedFilter = Filter.getEqualityFilter(dimensionName, testDims[i]);
      assertEquals(expectedFilter, filter);
      i++;
    }
  }
}

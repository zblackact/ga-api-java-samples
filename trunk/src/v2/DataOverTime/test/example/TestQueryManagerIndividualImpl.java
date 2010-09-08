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
 * Test suite for QueryManagerIndividualImpl. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestQueryManagerIndividualImpl extends TestCase {

  private QueryManagerIndividualImpl queryManager = new QueryManagerIndividualImpl();
  private FilteredQueries queries;

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
    assertNotNull(queries.getQuery());
    assertEquals(dimensionValues.size(), queries.getFilterList().size());

    String expectedFilter = Filter.getEqualityFilter(dimensionName, dimensionValue);
    assertEquals(expectedFilter, queries.getFilterList().get(0));

    // Ensure query is updated.
    DataQuery resultQuery = queries.getFilteredQuery(0);

    // The following lines help setup the test. Because getFilteredQueries
    // calls updateQuery, the expectedQuery is also passed through
    // updateQuery.
    DataQuery expectedQuery = TestUtil.getFilledDataQuery();
    expectedQuery.setDimensions(dimensionName);
    queryManager.updateQuery(expectedQuery);

    assertEquals(expectedQuery.getMaxResults(), resultQuery.getMaxResults());
    assertEquals(expectedQuery.getSort(), resultQuery.getSort());
    assertEquals(expectedQuery.getDimensions(), resultQuery.getDimensions());
  }

  /**
   * Tests the iterator implementation of getFileredQueries.
   */
  public void testGetFilteredQueries_iteratorImplementation() {
    DataQuery dataQueryTest = TestUtil.getFilledDataQuery();
    String dimensionName = "ga:landingPagePath";
    dataQueryTest.setDimensions(dimensionName);

    List<String> dimensionValues = TestUtil.toList(new String[]{"/foo", "/bar", "bat"});

    queries = queryManager.getFilteredQueries(dataQueryTest, dimensionValues);

    // The following lines help setup the test. Because getFilteredQueries
    // calls updateQuery, the expectedQuery is also passed through
    // updateQuery.
    DataQuery expectedQuery = TestUtil.getFilledDataQuery();
    expectedQuery.setDimensions(dimensionName);
    queryManager.updateQuery(expectedQuery);

    for (String dimensionValue : dimensionValues) {
      String expectedFilter = Filter.getEqualityFilter(dimensionName, dimensionValue);
      expectedQuery.setFilters(expectedFilter);

      DataQuery testQuery = queries.next();
      assertEquals(expectedQuery.getUrl(), testQuery.getUrl());
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
   * Tests filters are added to the query if the original query didn't
   * have filters.
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
    assertEquals(expectedFilter, dataQuery.getFilters());
  }

  /**
   * Tests getting a filter list.
   */
  public void testGetFilterList() {
    String dimensionName = "ga:source";
    String[] testDimensions = new String[] {"foo", "bar"};
    List<String> dimensionValues = TestUtil.toList(testDimensions);
    List<String> filters = queryManager.getFilterList(dimensionName, dimensionValues);

    assertNotNull(filters);

    int i = 0;
    for (String filter : filters) {
      String expectedFilter = Filter.getEqualityFilter(dimensionName, testDimensions[i]);
      assertEquals(expectedFilter, filter);
      i++;
    }
  }

  /**
   * tests getting a filter list returns an empty array if the parameters are
   * incorrectly set.
   */
  public void testGetFilterList_ifParametersAreIncorrectlySet() {
    List<String> filters = queryManager.getFilterList(null, new ArrayList<String>());
    assertNotNull(filters);

    List<String> dimensionList = TestUtil.toList(new String[] {"/foo", "/bar"});
    filters = queryManager.getFilterList(null, dimensionList);
    assertNotNull(filters);

    filters = queryManager.getFilterList(null, null);
    assertNotNull(filters);

    filters = queryManager.getFilterList("", null);
    assertNotNull(filters);

    filters = queryManager.getFilterList("foo", null);
    assertNotNull(filters);
  }
}

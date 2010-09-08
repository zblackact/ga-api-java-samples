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
 * Test suite for FilteredQueries. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestFilteredQueries extends TestCase {

  private FilteredQueries queries = new FilteredQueries();

  /**
   * Test setQuery stores the original filter.
   */
  public void testSetQuery() {
    String filter = "ga:source==foo";
    DataQuery query = TestUtil.getFilledDataQuery();
    query.setFilters(filter);
    queries.setQuery(query);

    String result = queries.getOriginalFilter();
    assertTrue(filter.equals(result));
  }

  /**
   * Test setQuery stores an empty string if no filter is present.
   */
  public void testSetQuery_noFilter() {
    DataQuery query = TestUtil.getFilledDataQuery();
    query.setFilters(null);
    queries.setQuery(query);

    String result = queries.getOriginalFilter();
    assertTrue("".equals(result));
  }

  /**
   * Tests getting a filtered query.
   */
  public void testGetFilteredQuery() {
    String filter = "ga:landingPagePath==/foo";
    DataQuery expectedQuery = TestUtil.getNewDataQuery();
    expectedQuery.setFilters(filter);

    queries.setQuery(TestUtil.getNewDataQuery());

    queries.setFilterList(new ArrayList<String>());
    assertNull(queries.getFilteredQuery(0));

    List<String> filterList = new ArrayList<String>();
    queries.setFilterList(filterList);
    filterList.add(filter);

    assertEquals(expectedQuery.getUrl(), queries.getFilteredQuery(0).getUrl());
    assertNull(queries.getFilteredQuery(1));
    assertNull(queries.getFilteredQuery(-1));
  }

  /**
   * Tests getting a filtered query from a query that previously has a filter.
   */
  public void testGetFilteredQuery_withPrevFilter() {
    String originalFilter = "ga:source==google;ga:visits>5;";
    DataQuery query = TestUtil.getFilledDataQuery();
    query.setFilters(originalFilter);

    String newFilter = "ga:landingPagePath==/foo";
    List<String> filterList = new ArrayList<String>();
    filterList.add(newFilter);

    queries.setQuery(query);
    queries.setFilterList(filterList);
    DataQuery resultQuery = queries.getFilteredQuery(0);

    String expected = originalFilter + newFilter;
    assertEquals(expected, resultQuery.getFilters());
  }

  /**
   * Tests Iterator hasNext implementation.
   */
  public void testHasNext() {
    assertFalse(queries.hasNext());

    List<String> filterList = new ArrayList<String>();
    filterList.add("/foo");

    queries.setFilterList(filterList);
    assertTrue(queries.hasNext());
  }

  /**
   * Tests Iterator next implementation.
   */
  public void testNext() {
    String filter1 = "ga:source==google";
    String filter2 = "ga:source==yahoo";


    List<String> filterList = new ArrayList<String>();
    filterList.add(filter1);
    filterList.add(filter2);
    queries.setFilterList(filterList);
    queries.setQuery(TestUtil.getNewDataQuery());

    DataQuery expectedQuery = TestUtil.getNewDataQuery();
    expectedQuery.setFilters(filter1);
    assertEquals(expectedQuery.getUrl(), queries.next().getUrl());
    assertEquals(1, queries.getCounter());

    expectedQuery.setFilters(filter2);
    assertEquals(expectedQuery.getUrl(), queries.next().getUrl());
    assertEquals(2, queries.getCounter());

    assertNull(queries.next());
    assertEquals(3, queries.getCounter());
  }
}

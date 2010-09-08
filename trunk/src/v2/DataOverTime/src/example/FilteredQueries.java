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

import java.util.Iterator;
import java.util.List;

/**
 * Provides a container to store a list of queries which only differ by
 * their filter parameter. This class will just store the main query
 * and a list of filters. This implements the Iterator interface and
 * can be used to get each individual query.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class FilteredQueries implements Iterator<DataQuery> {

  private List<String> filterList;
  private String originalFilter;
  private DataQuery query;
  int indexCounter;

  /**
   * Constructor.
   * Initialized the indexCounter to 0.
   */
  public FilteredQueries() {
    indexCounter = 0;
  }

  /**
   * Sets the list of filters to apply to each query
   * @param filterList The list of filters.
   */
  public void setFilterList(List<String> filterList) {
    this.filterList = filterList;
  }

  /**
   * @return The list of filters.
   */
  public List<String> getFilterList() {
    return filterList;
  }

  /**
   * Sets the DataQuery object. Also if the query has a filter, the
   * filter is stored in the originalFilter member (and will be prepended
   * to each query later on). If no filter has been set, the originalFilter
   * is set to the empty string.
   * @param query The main DataQuery object.
   */
  public void setQuery(DataQuery query) {
    this.query = query;
    String testFilter = query.getFilters();
    if (testFilter != null) {
      originalFilter = testFilter;
    } else {
      originalFilter = "";
    }
  }

  /**
   * @return The DataQuery object.
   */
  public DataQuery getQuery() {
    return query;
  }

  /**
   * @return The internal counter.
   */
  public int getCounter() {
    return indexCounter;
  }

  /**
   * Resets the internal counter which is used by the Iterator implementation.
   * This should be called if a program wants to use a FilteredQueries object
   * in multiple loops.
   */
  public void resetCounter() {
    indexCounter = 0;
  }

  /**
   * Sets the original filter.
   * @param originalFilter The original filter to set.
   */
  public void setOriginalFilter(String originalFilter) {
    this.originalFilter = originalFilter;
  }

  /**
   * @return The original filter.
   */
  public String getOriginalFilter() {
    return originalFilter;
  }

  /**
   * Updates the Data Query object with a filter from the filter list and
   * returns the updated object. Returns null if index is negative or
   * greater than the size of the filter list.
   * @param index An index into the filter list to retrieve a specific filter.
   * @return A DaatQuery object.
   */
  public DataQuery getFilteredQuery(int index) {
    if (filterList == null || index < 0 || index >= filterList.size()) {
      return null;
    }
    query.setFilters(originalFilter + filterList.get(index));
    return query;
  }

  /**
   * Returns if there are more queries to be returned from the filter list.
   * Part of the Iterator implementation.
   * @return Whether there are more queries.
   */
  public boolean hasNext() {
    if (filterList == null || filterList.size() < 0 || indexCounter >= filterList.size()) {
      return false;
    }
    return true;
  }

  /**
   * Returns a DataQuery object in which it's filter has the next
   * filter from the filter list, appended to the query objects filter
   * parameter. Part of the Iterator implementation.
   * @return A DataQuery object.
   */
  @Override
  public DataQuery next() {
    DataQuery result = getFilteredQuery(indexCounter);
    indexCounter++;
    return result;
  }

  /**
   * Part of Iterator interface. Not implemented.
   */
  @Override
  public void remove() {}
}

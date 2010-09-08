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

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation of the QueryManager Interface. This will
 * generate a list of filtered queries in which each dimension will be
 * in one filter. This will result in a separate API request for each
 * dimension.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class QueryManagerIndividualImpl implements QueryManager {

  /**
   * Returns a FilteredQueries object in which each dimension has a
   * corresponding query to retrieve data over time.
   * @param dataQuery The DataQuery object used to retrieve a list of
   *     dimensions.
   * @param dimensionValues The list of dimensions returned from the
   *     dataQuery parameter.
   * @return A FilteredQueries object to get data over time for each
   *     dimension.
   */
  @Override
  public FilteredQueries getFilteredQueries(DataQuery dataQuery, List<String> dimensionValues) {
    FilteredQueries queries = new FilteredQueries();
    String dimensionName = dataQuery.getDimensions();
    updateQuery(dataQuery);
    queries.setQuery(dataQuery);
    queries.setFilterList(getFilterList(dimensionName, dimensionValues));
    return queries;
  }

  /**
   * Updates a DataQuery object so it can be used to get
   * data over time.
   * @param dataQuery The DataQuery object to update.
   */
  public void updateQuery(DataQuery dataQuery) {
    dataQuery.setDimensions("ga:date");
    dataQuery.setSort("ga:date");
    dataQuery.setMaxResults(DataQueryUtil.getNumberOfDays(dataQuery));
    dataQuery.setStartIndex(-1); // Un-sets the parameter.
    Filter.addAndOperator(dataQuery);
  }

  /**
   * Returns a list of filter expressions so that each expression has only
   * one dimension specified. An empty ArrayList is returned if
   * originalDimensionName is null or an empty string or dimensionValues is
   * null or has no elements.
   * @param originalDimensionName The dimension name used in the first query.
   */
  public List<String> getFilterList(String originalDimensionName, List<String> dimensionValues) {

    if (originalDimensionName == null || originalDimensionName.equals("")
        || dimensionValues == null || dimensionValues.size() == 0) {
      return new ArrayList<String>();
    }

    List<String> result = new ArrayList<String>(dimensionValues.size());
    for (String dimensionValue : dimensionValues) {
      result.add(Filter.getEqualityFilter(originalDimensionName, dimensionValue));
    }
    return result;
  }
}

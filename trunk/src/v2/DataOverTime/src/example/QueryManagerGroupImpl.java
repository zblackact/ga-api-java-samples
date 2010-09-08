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
 * Provides an implementation of the QueryManager Interface. This will attempt
 * to generate a list of filtered queries using as few queries as possible.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class QueryManagerGroupImpl implements QueryManager {

  static final int MAX_RESULTS = 10000;
  static final int MAX_QUERY_LEN = 2000;
  private BucketManager bucketManager;

  /**
   * Constructor.
   * Sets the BucketManager instance.
   */
  public QueryManagerGroupImpl(BucketManager bucketManager) {
    this.bucketManager = bucketManager;
  }

  /**
   * Returns a FilteredQueries object which can be used to make requests
   * to the Google Analytics API.
   * @param dataQuery The original query used to get a list of dimensions.
   * @param dimensionValues A list of dimension values returned from the
   *     dataQuery object.
   * @return A FilteredQueries object to get data over time.
   */
  @Override
  public FilteredQueries getFilteredQueries(DataQuery dataQuery, List<String> dimensionValues) {
    FilteredQueries queries = new FilteredQueries();
    String originalDimensionName = dataQuery.getDimensions();
    updateQuery(dataQuery);
    queries.setQuery(dataQuery);
    bucketManager.init(getFilterMaxCharLength(MAX_QUERY_LEN, dataQuery),
        getFilterMaxListSize(MAX_RESULTS, dataQuery));
    queries.setFilterList(getFilterList(originalDimensionName, dimensionValues));
    return queries;
  }

  /**
   * Updates a DataQuery with new parameters so that it can get metrics
   * over the period of a date range. This actually changes the original
   * query values. So any previous values must be saved by the user.
   * @param dataQuery The DataQuey object to update.
   */
  public void updateQuery(DataQuery dataQuery) {
    String dimensionAndDate = dataQuery.getDimensions() + ",ga:date";
    dataQuery.setDimensions(dimensionAndDate);
    dataQuery.setSort(dimensionAndDate);
    dataQuery.setMaxResults(MAX_RESULTS);
    dataQuery.setStartIndex(-1);  // Unsets parameter.
    Filter.addAndOperator(dataQuery);
  }

  /**
   * Returns the maximum length for a filter expression based on the maximum
   * length of a URI and the size of an encoded DataQuery object. This assumes
   * that filter expressions will be added to the final URL.
   *
   * If a filter is present in updatedQuery:
   *     The code assume the filter already has an AND operator appended to the
   * filter, and the encoded length of the query is used in the calculation.
   *
   * If no filter is present in updatedQuery:
   *     The code will add a filter string set to "". This will add the
   *     "&filter=" string to URL so that this encoded length is computed
   *     in the final result. Once calculated the filter is removed to preserve
   *     the original state.
   * @param maxUrlLength The maximum allowed size of queries to the API.
   * @param dataQuery The query whose length we are calculating.
   * @returns The maximum length for a filter expression.
   */
  public int getFilterMaxCharLength(int maxUrlLength, DataQuery dataQuery) {
    int queryLength;
    if (dataQuery.getFilters() == null) {
      dataQuery.setFilters("");
      queryLength = dataQuery.getUrl().toString().length();
      dataQuery.setFilters(null);
    } else {
      queryLength = dataQuery.getUrl().toString().length();
    }
    return maxUrlLength - queryLength;
  }

  /**
   * Returns the maximum length for the filter list. Each filter represents
   * one row of data over time. The number of results for each row of data
   * equals the number of days in the date range. The Google Analytics
   * Data Export API allows a maximum of 10000 results to be returned from
   * any one query. To not require pagination of results, only the number of
   * rows (filters) that return less than 10000 results should be used in
   * any query.
   * @param maxResults The total allowable number of results for each query to
   *     the API. Typically 10000.
   * @param dataQuery The data query with the start and end dates set.
   * @return The maximum numbers of filters that can be in each query.
   */
  public int getFilterMaxListSize(int maxResults, DataQuery dataQuery) {
    int numDays = DataQueryUtil.getNumberOfDays(dataQuery);
    return maxResults / numDays;
  }

  /**
   * Returns a list of strings which represent filter expressions. These
   * filters are organized so that no pagination needs to be done when parsing
   * the results. The filters are also optimized to have as few API queries as
   * possible.
   * @param originalDimensionName The dimension name from the first request to
   *     the API.
   * @param dimensionValues A list of dimension values retrieved from the first
   *     API query.
   * @return A list of filter expressions represented as strings.
   */
  public List<String> getFilterList(String originalDimensionName, List<String> dimensionValues) {

    List<String> result = new ArrayList<String>();

    if (!originalDimensionName.equals("") && dimensionValues != null
        && dimensionValues.size() > 0) {

      List<Bucket> buckets =
          bucketManager.getBucketsOfFilters(originalDimensionName, dimensionValues);

      for (Bucket bucket : buckets) {
        result.add(bucket.toString());
      }
    }
    return result;
  }
}

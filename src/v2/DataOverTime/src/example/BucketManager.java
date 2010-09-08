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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the logic to add filters to buckets to result in the most number
 * of filters in each bucket. This yield optimal quota usage.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class BucketManager {

  private int filterMaxCharLength;
  private int filterMaxListSize;

  /**
   * Initializes the bucket manager by setting the query, calculating the
   * maximum results per bucket and maximum character length.
   * @param dataQuery The updated data query that will be used to get data
   *     over time.
   * @param maxChars The maximum number of characters allowed in a API
   *     request URI.
   * @param maxResults The Maximum number of results that can be returned
   *     by the API.
   */
  public void init(int filterMaxCharLength, int filterMaxListSize) {
    this.filterMaxCharLength = filterMaxCharLength;
    this.filterMaxListSize = filterMaxListSize;
  }

  /**
   * Returns a new list of Filter objects sorted from the largest encoded
   * filter size to the smallest size.  None of the filters in the final list
   * should be greater than Bucket.filterMaxCharLength. If any filters are
   * too long, they will make the final URL too long so they are silently
   * dropped.
   * @param dimensionName The dimension name for each of the filters.
   * @param dimensionValues A list of dimension values for each filter.
   * @return A list of Filter objects sorted by encoded filter length.
   */
  public List<Filter> getFiltersOrderedBySize(String dimensionName,
      List<String> dimensionValues) {

    if (dimensionValues == null) {
      return new ArrayList<Filter>();
    }

    List<Filter> filters = new ArrayList<Filter>(dimensionValues.size());
    for (String dimensionValue : dimensionValues) {
      Filter filter = new Filter(dimensionName, dimensionValue);
      if (filter.getEncodedSize() <= filterMaxCharLength) {
        filters.add(filter);
      }
    }
    Collections.sort(filters);
    return filters;
  }

  /**
   * Returns a list of Bucket objects that contain filters such that the size
   * of the returned list is as small as possible. This method gets an ordered
   * list of filters based on the dimension name and list of values. The first
   * filter from this list is added into a new bucket. The following filters
   * try to get added to the next available bucket. If they can't be added to
   * an existing bucket they are added to a new bucket.
   * @param dimensionName The dimension name for each of the filters.
   * @param dimensionValues A list of dimension values for each filter.
   * @return A list of Bucket objects.
   */
  public List<Bucket> getBucketsOfFilters(String dimensionName, List<String> dimensionValues) {
    List<Filter> filterList = getFiltersOrderedBySize(dimensionName, dimensionValues);
    List<Bucket> buckets = new ArrayList<Bucket>();

    if (filterList == null || filterList.size() < 1) {
      return buckets;
    }

    // Always add the first filter to a new bucket.
    buckets.add(new Bucket(filterMaxCharLength, filterMaxListSize, filterList.get(0)));

    // Check if remaining filters can be added to existing buckets.
    Boolean added;
    for (int i = 1; i < filterList.size(); i++) {
      added = false;
      Filter filter = filterList.get(i);
      for (Bucket existingBucket : buckets) {
        added = existingBucket.add(filter);
        if (added) {
          break;
        }
      }
      // If a filter will not fit in an existing bucket, add to a new bucket.
      if (!added) {
        buckets.add(new Bucket(filterMaxCharLength, filterMaxListSize, filter));
      }
    }
    return buckets;
  }
}

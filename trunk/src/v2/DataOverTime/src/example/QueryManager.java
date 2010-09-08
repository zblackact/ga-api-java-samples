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

import java.util.List;

/**
 * Provides an interface to manage queries.
 * 
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public interface QueryManager {

  /**
   * Returns a FilteredQueries object which represents a list of queries that
   * have special filters.
   * @param dataQuery A DataQuery object.
   * @param dimensionValues A List of dimension values returned from the
   *     Data Export API using the data query object.
   * @return A FilteredQueries object representing a list of queries with
   *     filters.
   */
  public FilteredQueries getFilteredQueries(DataQuery dataQuery, List<String> dimensionValues);

}

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
import com.google.gdata.data.analytics.DataFeed;

import java.util.List;

/**
 * Main class to retrieve the metrics for the values of one dimension over
 * time.
 */
public class DataOverTime {
  DataManager dataManager;
  QueryManager queryManager;
  ResultManager resultManager;

  /**
   * Constructor.
   * @param dataManager An implementation of DataManager.
   * @param queryManager An implementation of QueryManager.
   * @param resultManager An implementation of ResultManager.
   */
  public DataOverTime(DataManager dataManager, QueryManager queryManager,
      ResultManager resultManager) {

    this.dataManager = dataManager;
    this.queryManager = queryManager;
    this.resultManager = resultManager;
  }

  /**
   * Main method to retrieve all the data. This first makes a query to get
   * a list of dimensions, then programmatically constructs a list of queries
   * and executes them to get each dimension's metrics over time. Finally
   * the data is returned as a new Results object.
   * @param dataQuery The initial query to get data over time. This should
   *     have only one dimension and one metric.
   * @return A Results object with all the data.
   */
  public Results getData(DataQuery dataQuery) {
    Results results = new Results();

    List<String> dimensionValues = dataManager.getDimensionValues(dataQuery);
    results.initTable(dataQuery, dimensionValues);
    resultManager.setResults(results);

    FilteredQueries queries = queryManager.getFilteredQueries(dataQuery, dimensionValues);

    while (queries.hasNext()) {
      DataQuery query = queries.next();
      DataFeed feed = dataManager.getFeed(query);
      resultManager.addRows(feed);
    }

    return results;
  }
}

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

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the ResultManager interface to handle queries where each
 * request has only one row of data.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class ResultManagerIndividualImpl implements ResultManager {

  private Results results;

  /**
   * Sets the initialized result object to add the data to.
   */
  @Override
  public void setResults(Results results) {
    this.results = results;
  }

  /**
   * Adds all the rows from the DataFeed object returned from the API
   * into the results object. Since each query has only one row of data
   * all the data is added as one row. This method also checks to see
   * if any of the entries has sampled metrics. The isSampled can
   * only be set to true.
   * @param feed The DataFeed object to parse and store in a Results
   *     object.
   */
  @Override
  public void addRows(DataFeed feed) {
    List<Double> row = new ArrayList<Double>(results.getNumRows());
    Metric metric;
    boolean isSampled = false;

    for (DataEntry entry : feed.getEntries()) {
      metric = entry.getMetrics().get(0);
      if (!isSampled && 0 != metric.getConfidenceInterval()) {
        isSampled = true;
      }
      row.add(new Double(metric.getValue()));
    }
    results.addRow(row);
    results.setIsSampled(isSampled);
  }
}

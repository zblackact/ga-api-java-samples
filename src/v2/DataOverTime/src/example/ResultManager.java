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

import com.google.gdata.data.analytics.DataFeed;

/**
 * Interface for handling data over time results from the Data Export API.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public interface ResultManager {

  /**
   * Sets the internal representation of the Results object.
   * @param results A Results object to store data.
   */
  public void setResults(Results results);

  /**
   * Adds a data from a data feed returned from the API as rows to the
   * Results object.
   * @param feed The DataFeed object returned with data from the Data Export
   *     API.
   */
  public void addRows(DataFeed feed);

}

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

import com.google.gdata.client.analytics.AnalyticsService;

/**
 * Factory class to separate instances that get data over time
 * by individual queries or grouped queries.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class DataOverTimeFactory {

  /**
   * Constructor.
   * Shouldn't be used.
   */
  private DataOverTimeFactory() {}

  /**
   * Returns an object that automates retrieving data over time. The object is
   * configured to make an individual query for each
   * dimension.
   * @param analyticsService An authorized AnalyticsService object.
   * @return An object that implements the DataOverTime interface
   */
  public static DataOverTime getIndividualQueries(AnalyticsService analyticsService) {
    return new DataOverTime(
        new DataManager(analyticsService),
        new QueryManagerIndividualImpl(),
        new ResultManagerIndividualImpl());
  }

  /**
   * Returns an object that automates retrieving data over time. The object is
   * configured to reduce the number of queries to the API by grouping queries.
   * @param analyticsService An authorized AnalyticsService object.
   * @return An object that implements the DataOverTime interface
   */
  public static DataOverTime getGroupQueries(AnalyticsService analyticsService) {
    return new DataOverTime(
        new DataManager(analyticsService),
        new QueryManagerGroupImpl(new BucketManager()),
        new ResultManagerGroupImpl());
  }
}

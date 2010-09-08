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
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a wrapper to access data from the Google Analytics API.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class DataManager {

  private AnalyticsService analyticsService;

  /**
   * Constructor.
   * @param analyticsService An AnalyticsService object.
   */
  public DataManager(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  /**
   * Returns a list of all the dimension values.
   * @param dataQuery A Data eport API query to send to the Data Export API.
   * @return A list of dimension values.
   */
  public List<String> getDimensionValues(DataQuery dataQuery) {
    DataFeed resultFeed = getFeed(dataQuery);

    List<String> output = new ArrayList<String>();
    for (DataEntry entry : resultFeed.getEntries()) {
      output.add(entry.getDimensions().get(0).getValue());
    }
    return output;
  }

  /**
   * Retrieves a data from the Google Analytics Data Export API. Any
   * exceptions are printed to the console and the program terminates.
   * @param dataQuery The query to send to the API.
   * @retun The DataFeed response object from the API.
   */
  public DataFeed getFeed(DataQuery dataQuery) {
    try {
      System.out.println(dataQuery.getUrl().toString());
      return analyticsService.getFeed(dataQuery, DataFeed.class);

    } catch (IOException e) {
      System.err.println("IO Exception: " + e.getMessage());
      System.exit(0);
    } catch (ServiceException e) {
      System.err.println("Service Exception: " + e.getMessage());
      System.err.println(dataQuery.getUrl().toString());
      System.exit(0);
    }
    return null;
  }
}

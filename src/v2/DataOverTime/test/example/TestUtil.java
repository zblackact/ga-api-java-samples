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

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Provides any utilities used across tests.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestUtil {

  private static final String DATA_FEED_BASE_URL = "https://www.google.com/analytics/feeds/data";

  /**
   * Returns a new DataQuery object. Catches all exceptions.
   * @return
   */
  public static DataQuery getNewDataQuery() {
    try {
      return new DataQuery(new URL(DATA_FEED_BASE_URL));
    } catch (MalformedURLException e) { TestCase.fail(); }
    return null;
  }

  /**
   * Returns a new DataQuery object that has a set of filled parameters.
   * @return A filled DataQuery object.
   */
  public static DataQuery getFilledDataQuery() {
    DataQuery dataQuery = getNewDataQuery();
    dataQuery.setStartDate("2010-01-01");
    dataQuery.setEndDate("2010-01-15");
    dataQuery.setDimensions("ga:landingPagePath");
    dataQuery.setMetrics("ga:entrances");
    return dataQuery;
  }

  /**
   * Converts a String Array to a list of Strings.
   * @param data
   * @return
   */
  public static List<String> toList(String[] data) {
    return Arrays.asList(data);
  }
}

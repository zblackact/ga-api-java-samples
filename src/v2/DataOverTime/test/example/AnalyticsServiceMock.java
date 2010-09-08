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

import com.google.gdata.client.Query;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.data.IFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock of the AnalyticsService class. Extends Analytics.
 * Used to return a filled DataFeed object.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AnalyticsServiceMock extends AnalyticsService {

  private String[][] dimensions = null;
  private String[][] metrics = null;
  private String[] dimensionNames = null;
  private double confidenceInterval = 0;

  /**
   * Constructor.
   * @param applicationName The name of the application.
   */
  public AnalyticsServiceMock(String applicationName) {
    super(applicationName);
  }

  /**
   * Sets the internal data to be filled in a DataFeed object.
   * @param dimensions NxM array of dimension values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @param metricss NxM array of metric values, where [0][i] and [1][i]
   *     will be in the same entry.
   */
  public void setData(String[][] dimensions, String[][] metrics) {
    this.dimensions = dimensions;
    this.metrics = metrics;
  }

  /**
   * Returns a DataFeed object with the specified data set. All exceptions
   * are caught here.
   * @param dimensions NxM array of dimension values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @param metrics NxM array of metric values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @return A DataFeed object with the test data.
   */
  public DataFeed getDataFeed(String[][] dimensions, String[][] metrics) {
    setData(dimensions, metrics);
    try {
      return getFeed(TestUtil.getNewDataQuery(), DataFeed.class);
    } catch (IOException e) {
      System.exit(0);
    } catch (ServiceException e) {
      System.exit(0);
    }
    return null;
  }

  /**
   * Returns a DataFeed object in which all the dimension names have been set.
   * @param dimensionNames The names of all the dimensions.
   * @param dimensions NxM array of dimension values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @param metrics NxM array of metric values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @return A DataFeed object with the test data.
   */
  public DataFeed getDataFeed(String[] dimensionNames, String[][] dimensions,
      String[][] metrics) {
    this.dimensionNames = dimensionNames;
    return getDataFeed(dimensions, metrics);
  }

  /**
   * Returns a DataFeed object in which all the dimension names have been set.
   * @param dimensionNames The names of all the dimensions.
   * @param dimensions NxM array of dimension values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @param metrics NxM array of metric values, where [0][i] and [1][i]
   *     will be in the same entry.
   * @return A DataFeed object with the test data.
   */
  public DataFeed getDataFeed(String[] dimensionNames, String[][] dimensions,
      String[][] metrics, double confidenceInterval) {
    this.dimensionNames = dimensionNames;
    this.confidenceInterval = confidenceInterval;
    return getDataFeed(dimensions, metrics);
  }

  /**
   * Main method to return a Data Feed object. This overrides the
   * Analytics Service implementation.
   * @param query The Data Query.
   * @param feedClass The class which should be returned.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <F extends IFeed> F getFeed(Query query, Class<F> feedClass)
  throws IOException, ServiceException {

    DataFeed feed = new DataFeed();
    List<DataEntry> entryList = new ArrayList<DataEntry>();

    int numEntries = metrics[0].length;
    for (int entryIndex = 0; entryIndex < numEntries; entryIndex++) {
      DataEntry entry = new DataEntry();

      // Add all dimensions.
      for (int dimensionIndex = 0; dimensionIndex < dimensions.length; dimensionIndex++) {
        Dimension dimension = new Dimension();
        dimension.setValue(dimensions[dimensionIndex][entryIndex]);
        if (dimensionNames != null) {
          dimension.setName(dimensionNames[dimensionIndex]);
        }
        entry.addDimension(dimension);
      }
      // Add all metrics.
      for (int metricIndex = 0; metricIndex < metrics.length; metricIndex++) {
        Metric metric = new Metric();
        metric.setValue(metrics[metricIndex][entryIndex]);
        metric.setConfidenceInterval(confidenceInterval);
        entry.addMetric(metric);
      }
      entryList.add(entry);
    }

    feed.setEntries(entryList);
    return (F) feed;
  }
}

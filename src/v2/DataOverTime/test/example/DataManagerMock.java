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
 * Mock of a Data Manager. Extends DataManger.
 * 
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class DataManagerMock extends DataManager {

  private List<String> dimensions;

  /**
   * Constructor.
   * @param analyticsServiceMock Mock for AnalyticsService.
   */
  public DataManagerMock(AnalyticsServiceMock analyticsServiceMock) {
    super(analyticsServiceMock);
  }

  /**
   * Sets a list of dimensions.
   * @param dimensions List of strings containing dimension values.
   */
  public void setDimensions(List<String> dimensions) {
    this.dimensions = dimensions;
  }

  /**
   * Returns a list of dimensions.
   * @param dataQuery Not used.
   * @return A list of dimension values.
   */
  @Override
  public List<String> getDimensionValues(DataQuery dataQuery) {
    return dimensions;
  }
}

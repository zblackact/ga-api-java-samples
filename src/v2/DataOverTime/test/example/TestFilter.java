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
import com.google.gdata.util.common.base.CharEscapers;

import junit.framework.TestCase;

import java.text.MessageFormat;

/**
 * Test suite for the Filter. Extends testCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestFilter extends TestCase {

  /**
   * Tests constructor.
   */
  public void testFilter() {
    String dimensionName = "ga:source";
    String dimensionValue = "google";

    Filter filter = new Filter(dimensionName, dimensionValue);
    assertNotNull(filter);
    assertTrue(0 < filter.getEncodedSize());

    String result = Filter.getEqualityFilter(dimensionName, dimensionValue);

    assertTrue(filter.toString().equals(result));
    assertTrue(dimensionName.equals(filter.getName()));
    assertTrue(dimensionValue.equals(filter.getValue()));
  }

  /**
   * Tests retrieving an equality filter.
   */
  public void testGetEqualityFilterExpression() {
    String dimensionName = "ga:source";
    String dimensionValue = "google";

    String expectedExpression = MessageFormat.format("{0}=={1}", dimensionName, dimensionValue);
    String result = Filter.getEqualityFilter(dimensionName, dimensionValue);

    assertTrue(expectedExpression.equals(result));
  }

  /**
   * Tests setting the size of the filter.
   */
  public void testSetSize() {

    String expression = "ga:source==google";
    Filter filter = new Filter("", "");
    filter.setEncodedSize(expression);

    String escapedExpression = CharEscapers.uriEscaper().escape(expression);
    int expectedLength = escapedExpression.length();
    int result = filter.getEncodedSize();
    assertEquals(expectedLength, result);
  }

  /**
   * Tests compareTo implementation.
   */
  public void testComapreTo() {
    Filter filterSm = new Filter("a", "b");
    Filter filterLg = new Filter("aaa", "bbb");

    assertEquals(1, filterSm.compareTo(filterLg));
    assertEquals(-1, filterLg.compareTo(filterSm));
    assertEquals(0, filterSm.compareTo(filterSm));
  }

  /**
   * Test equals implementation.
   */
  public void testEquals() {
    Filter one = new Filter("a", "a");
    Filter two = new Filter("a", "a");
    Filter three = new Filter("b", "b");

    assertTrue(one.equals(one));
    assertTrue(one.equals(two));
    assertFalse(one.equals(three));
  }

  /**
   * Test no AND operator is added if a filter has not bee set.
   */
  public void testAddAndOperator_testIfNotSet() {

    DataQuery dataQuery = TestUtil.getNewDataQuery();
    Filter.addAndOperator(dataQuery);
    assertNull(dataQuery.getFilters());

    dataQuery.setFilters(null);
    Filter.addAndOperator(dataQuery);
    assertNull(dataQuery.getFilters());
  }

  /**
   * Test adding an AND operator if a filter is set.
   */
  public void testAddAndOperator_testIfFilterSet() {
    String filter = "ga:source=@google";
    DataQuery dataQuery = TestUtil.getNewDataQuery();
    dataQuery.setFilters(filter);
    Filter.addAndOperator(dataQuery);
    String expected = filter + ";";
    assertTrue(expected.equals(dataQuery.getFilters()));
  }
}

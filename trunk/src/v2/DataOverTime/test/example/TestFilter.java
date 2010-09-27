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
   * Tests setting the equality filter expression.
   */
  public void testSetEqualityFilterExpression() {
    String dimensionName = "ga:pageTitle";
    String dimensionValue = "bad,title\\withchars;";

    String filterExpression = MessageFormat.format("{0}=={1}", dimensionName,
        Filter.escapeValue(dimensionValue));
    int encodedSize = CharEscapers.uriEscaper().escape(filterExpression).length();;

    Filter filter = new Filter();
    filter.setEqualityFilterExpression(dimensionName, dimensionValue);

    assertEquals(dimensionName, filter.getName());
    assertEquals(dimensionValue, filter.getValue());
    assertEquals(filterExpression, filter.getFilterExpression());
    assertEquals(encodedSize, filter.getEncodedSize());
  }

  /**
   * Tests retrieving an equality filter.
   */
  public void testGetEqualityFilter() {
    String dimensionName = "ga:pageTitle";
    String dimensionValue = "bad,title\\withchars;";
    String expectedExpression = MessageFormat.format("{0}=={1}", dimensionName,
        Filter.escapeValue(dimensionValue));

    String filterExpression = Filter.getEqualityFilter(dimensionName, dimensionValue);
    assertEquals(expectedExpression, filterExpression);
  }

  /**
   * Tests properly escapes expression values.
   */
  public void testEscapeValue() {
    String expression = "aaa,bbb;ccc\\ddd";
    String expectedExpression = "aaa\\,bbb\\;ccc\\\\ddd";
    assertEquals(expectedExpression, Filter.escapeValue(expression));
  }

  /**
   * Tests getting the encoded size of the filter.
   */
  public void testGetEncodedSize() {
    String filterExpression = "ga:source==google";
    int encodedSize = CharEscapers.uriEscaper().escape(filterExpression).length();

    Filter filter = new Filter();
    assertEquals(encodedSize, filter.getEncodedSize(filterExpression));
  }

  /**
   * Tests compareTo implementation.
   */
  public void testComapreTo() {
    Filter filterSm = new Filter();
    filterSm.setEqualityFilterExpression("ga:source", "google");
    Filter filterLg = new Filter();
    filterLg.setEqualityFilterExpression("ga:landingPagePath", "/product/toys");

    assertEquals(1, filterSm.compareTo(filterLg));
    assertEquals(-1, filterLg.compareTo(filterSm));
    assertEquals(0, filterSm.compareTo(filterSm));
  }

  /**
   * Test equals implementation.
   */
  public void testEquals() {
    Filter one = new Filter();
    one.setEqualityFilterExpression("ga:keyword", "pretzel");
    Filter two = new Filter();
    two.setEqualityFilterExpression("ga:keyword", "pretzel");
    Filter three = new Filter();
    three.setEqualityFilterExpression("ga:keyword", "sandwich");

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

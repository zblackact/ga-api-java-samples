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

import java.text.CharacterIterator;
import java.text.MessageFormat;
import java.text.StringCharacterIterator;

/**
 * Provides a container to store filter expressions as well a filter's
 * encoded length. This class also implements Comparable so filters can
 * be sorted in a list.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class Filter implements Comparable<Filter> {

  private static final String AND_OPERATOR = ";";
  private static final String OR_OPERATOR = ",";

  private static int operatorSize = CharEscapers.uriEscaper().escape(OR_OPERATOR).length();

  private String dimensionName;
  private String dimensionValue;
  private String filterExpression;
  private int encodedSize;

  /**
   * Sets an equality expression in the filter.
   * The dimension name and value are stored in the filter, the
   * expression and encoded length is also stored.
   * @param dimensionName The dimension name for this filter.
   * @param dimensionValue The dimension value for this filter.
   */
  public void setEqualityFilterExpression(String dimensionName, String dimensionValue) {
    this.dimensionName = dimensionName;
    this.dimensionValue = dimensionValue;
    this.filterExpression = getEqualityFilter(dimensionName, dimensionValue);
    this.encodedSize = getEncodedSize(filterExpression);
  }

  /**
   * Returns an equality expression for a dimension name and dimension value.
   * (e.g. ga:source==google). This also escapes the reserved filter
   * characters.
   * @param dimensionName The dimension name.
   * @param dimensionValue The dimension value.
   * @return An equality filter expression.
   */
  public static String getEqualityFilter(String dimensionName, String dimensionValue) {
    return MessageFormat.format("{0}=={1}", dimensionName, escapeValue(dimensionValue));
  }

  /**
   * Returns a backslash-escaped version of the dimension value. A backslash
   * is prepended to all commas, semicolons and backslashes.
   * @param value The filter value to escape.
   * @return The backslash-escaped value.
   */
  public static String escapeValue(String value) {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(value);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ) {
      if (character == ',' || character == ';' || character == '\\') {
        result.append("\\");
      }
      result.append(character);
      character = iterator.next();
    }
    return result.toString();
  }

  /**
   * Returns the encoded length of the filter expression.
   * @param filterExpression The filter expression whose size we are setting.
   * @return The encoded length of the filter expression.
   */
  public int getEncodedSize(String filterExpression) {
    return CharEscapers.uriEscaper().escape(filterExpression).length();
  }

  /**
   * @return The URI encoded size of the filter.
   */
  public int getEncodedSize() {
    return encodedSize;
  }
  /**
   * @return The dimension name.
   */
  public String getName() {
    return dimensionName;
  }

  /**
   * @return The dimension value.
   */
  public String getValue() {
    return dimensionValue;
  }

  /**
   * @return The filter expression.
   */
  public String getFilterExpression() {
    return filterExpression;
  }

  /**
   * @return The filter expression as the String value for this object.
   */
  public String toString() {
    return filterExpression;
  }

  /**
   * Implementation of the Comparable interface. Allows this object to be
   * sorted in a List. This will order by the largest encoded size first.
   * @return -1 if the current size is greater than the other object. 1 if
   *     the current size is less than the current object. 0 If they are
   *     equal.
   */
  @Override
  public int compareTo(Filter filter) {
    if (encodedSize > filter.getEncodedSize()) {
      return -1;
    } else if (encodedSize < filter.getEncodedSize()) {
      return 1;
    }
    return 0;
  }

  /**
   * Implements the equals method of the object. True if the entire filter
   * expression of both filters is the same.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Filter)) {
      return false;
    }
    Filter filter = (Filter) obj;
    return this.getFilterExpression().equals(filter.getFilterExpression());
  }

  /**
   * Implements the object's hashCode method.
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return getFilterExpression().hashCode();
  }

  /**
   * Returns the encoded filter operator size.
   */
  public static int getOperatorSize() {
    return operatorSize;
  }

  /**
   * Appends an AND operator to the end of a filter in a DataQuery object.
   * If no filter exists, nothing is added.
   */
  public static void addAndOperator(DataQuery dataQuery) {
    if (dataQuery != null) {
      String filter = dataQuery.getFilters();
      if (filter != null) {
        dataQuery.setFilters(filter + AND_OPERATOR);
      }
    }
  }

  /**
   * @return The OR operator.
   */
  public static String getOrOperator() {
    return OR_OPERATOR;
  }

  /**
   * @return The AND operator.
   */
  public static String getAndOperator() {
    return AND_OPERATOR;
  }
}

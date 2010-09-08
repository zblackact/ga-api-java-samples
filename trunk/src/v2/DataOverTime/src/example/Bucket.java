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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides buckets to store a list of Filter objects. This keeps track
 * of the number of filters in each bucket as well the total allowable
 * and current size of each bucket. It also provides a method to check
 * if a given filter can be added to the bucket. To be used with an algorithm
 * to fit the most filters in each bucket.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class Bucket {

  private int filterMaxCharLength;
  private int filterMaxListSize;

  private List<Filter> filterList;
  private int charLength;

  /**
   * Constructor.
   * @param filterMaxCharLength The maximum number of characters allowed in a bucket.
   * @param filterMaxListSize The maximum number of filters allowed in a bucket.
   */
  public Bucket(int filterMaxCharLength, int filterMaxListSize) {
    filterList = new ArrayList<Filter>();
    charLength = 0;
    this.filterMaxCharLength = filterMaxCharLength;
    this.filterMaxListSize = filterMaxListSize;
  }

  /**
   * Constructor.
   * Simplifies creating a bucket by also adding a new filter.
   * @param filterMaxCharLength The maximum number of characters allowed in a bucket.
   * @param filterMaxListSize The maximum number of filters allowed in a bucket.
   * @param filter The first filter to add to a bucket.
   */
  public Bucket(int filterMaxCharLength, int filterMaxListSize, Filter filter) {
    this(filterMaxCharLength, filterMaxListSize);
    add(filter);
  }

  /**
   * Returns whether the filter may be added to the current bucket. The
   * existing number of filters must not exceed filterMaxListSize. Nor must
   * the new size in characters of all the filters combined, exceed the
   * maximum length of any one filter expression. This method accounts
   * for all the OR operators that must go between each filter. This method
   * does not account for any existing filters or operators that prefix the
   * list of OR expressions.
   * @param filter The filter to test if it can be added.
   * @return Whether the filter can be added to this bucket.
   */
  public Boolean canAdd(Filter filter) {
    // Size assumes an extra filter will be added to the end of the current filters.
    int currentSize = charLength + filterList.size() * Filter.getOperatorSize();

    if (filterList.size() < filterMaxListSize &&
        currentSize + filter.getEncodedSize() <= filterMaxCharLength) {
      return true;
    }
    return false;
  }

  /**
   * Returns whether a filter has been added to this bucket. This first checks
   * to see if a filter can be added to this bucket. If it can, it adds a
   * filter to the filterList then updates the character length of all the
   * filters currently in the list. The method then returns true.
   * If the filter can't be added, this method returns false.
   * @param filter The Filter object to add to the list.
   * @return Whether the filter was added to the bucket.
   */
  public Boolean add(Filter filter) {
    if (canAdd(filter)) {
      filterList.add(filter);
      charLength += filter.getEncodedSize();
      return true;
    }
    return false;
  };

  /**
   * Returns a comma separated list of filters.
   * @return The string representation of this object.
   */
  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();

    Iterator<Filter> filterIter = filterList.iterator();
    if (filterIter.hasNext()) {
      output.append(filterIter.next().toString());
      while (filterIter.hasNext()) {
        output.append(",").append(filterIter.next().toString());
      }
    }
    return output.toString();
  }

  /**
   * Returns the filter list.
   * @return the filter list.
   */
  public List<Filter> getFilterList() {
    return filterList;
  }

  /**
   * Returns the length of characters of the encoded filters in the list.
   * This does not include the required operators between each filter.
   * @return the length of characters of the encoded filters in the list.
   */
  public int getCharLength() {
    return charLength;
  }

  /**
   * Sets the filterMaxListSize for all buckets.
   * @param filterMaxListSize The maximum size of the filter list.
   */
  public void setFilterMaxListSize(int filterMaxListSize) {
    this.filterMaxListSize = filterMaxListSize;
  }

  /**
   * @return The maximum size of the filter list.
   */
  public int getFilterMaxListSize() {
    return filterMaxListSize;
  }

  /**
   * Sets the maximum length of filters for all buckets.
   * @param filterMaxCharLength The maximum number of characters in a filter.
   */
  public void setFilterMaxCharLength(int filterMaxCharLength) {
    this.filterMaxCharLength = filterMaxCharLength;
  }

  /**
   * @return The maximum number of characters in a filter.
   */
  public int getFilterMaxCharLength() {
    return filterMaxCharLength;
  }

  /**
   * Compares the equality of two buckets.
   * @return Whether two buckets have the same set of filters.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Bucket)) {
      return false;
    }
    Bucket bucket = (Bucket) obj;
    return this.getFilterList().equals(bucket.getFilterList());
  }

  /**
   * Overrides the object's hashCode method.
   * @return The hashCode.
   */
  public int hashCode() {
    return getFilterList().hashCode();
  }
}

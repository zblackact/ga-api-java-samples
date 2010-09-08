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

import junit.framework.TestCase;

/**
 * Test suite for Filter. Extends TestCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestBucket extends TestCase {

  private Filter filter;

  /**
   * Create a test filter.
   */
  public void setUp() {
    filter = new Filter("ga:source", "google");
  }

  /**
   * Test utility constructor.
   */
  public void testBucket_addAFilter() {
    Bucket bucket = new Bucket(10000, 10, filter);
    assertNotNull(bucket.getFilterList());
    assertEquals(filter.getEncodedSize(), bucket.getCharLength());
    assertEquals(1, bucket.getFilterList().size());
    assertEquals(filter, bucket.getFilterList().get(0));
  }

  /**
   * Tests not exceeding max filter list size.
   */
  public void testCanAdd_exceedsMaxListSize() {
    Bucket bucket = new Bucket(10000, 1);

    assertTrue(bucket.canAdd(filter));
    bucket.add(filter);
    assertFalse(bucket.canAdd(filter));
  }

  /**
   * Tests not exceeding max filter char length.
   */
  public void testCanAdd_exceedsMaxFilterLen() {
    Bucket bucket = new Bucket(filter.getEncodedSize(), 10);

    assertTrue(bucket.canAdd(filter));
    bucket.add(filter);
    assertFalse(bucket.canAdd(filter));
  }

  /**
   * Tests adding a filter and updated the length of all characters
   * in the bucket. Ensure's bucket size constraint is meet in add.
   */
  public void testAdd_bucketSize() {
    Bucket bucket = new Bucket(10000, 1);

    assertTrue(bucket.add(filter));
    assertEquals(1, bucket.getFilterList().size());
    assertEquals(filter.getEncodedSize(), bucket.getCharLength());

    assertFalse(bucket.add(filter));

    bucket.setFilterMaxListSize(2);
    bucket.setFilterMaxCharLength(10000);
    assertTrue(bucket.add(filter));
    assertEquals(2, bucket.getFilterList().size());
    assertEquals(filter.getEncodedSize() * 2, bucket.getCharLength());
  }

  /**
   * Tests adding a filter to a bucket whose size is constrained
   * by maxChars.
   */
  public void testAdd_maxCharLength() {
    Bucket bucket = new Bucket(0, 1000);
    assertFalse(bucket.add(filter));

    bucket.setFilterMaxListSize(1000);
    bucket.setFilterMaxCharLength(filter.getEncodedSize());
    assertTrue(bucket.add(filter));
  }

  /**
   * Tests adding 2 filters. Ensure the operator size is accounted for.
   */
  public void testAdd_add2Filters() {
    int maxSize = (filter.getEncodedSize() * 2) + Filter.getOperatorSize();
    Bucket bucket = new Bucket(maxSize, 1000, filter);
    bucket.add(filter);
    assertEquals(2, bucket.getFilterList().size());
  }

  /**
   * Tests string output.
   */
  public void testToString() {
    Bucket bucket = new Bucket(10000, 10000, filter);
    String expectedOutput = filter.toString();
    assertTrue(expectedOutput.equals(bucket.toString()));

    bucket.getFilterList().add(filter);
    expectedOutput += "," + filter.toString();
    assertTrue(expectedOutput.equals(bucket.toString()));

    bucket.getFilterList().add(filter);
    expectedOutput += "," + filter.toString();
    assertTrue(expectedOutput.equals(bucket.toString()));
  }

  /**
   * Tests the equals implementation.
   */
  public void testEquals() {
    Filter filter1 = new Filter("a", "b");
    Filter filter2 = new Filter("c", "d");
    Filter filter3 = new Filter("e", "f");

    Bucket bucket1 = new Bucket(10000, 10000);
    bucket1.getFilterList().add(filter1);
    bucket1.getFilterList().add(filter2);
    bucket1.getFilterList().add(filter3);

    assertTrue(bucket1.equals(bucket1));

    Bucket bucket2 = new Bucket(10000, 10000);
    bucket2.getFilterList().add(filter1);
    bucket2.getFilterList().add(filter2);

    assertFalse(bucket1.equals(bucket2));
    bucket2.getFilterList().add(filter3);
    assertTrue(bucket1.equals(bucket2));
  }
}

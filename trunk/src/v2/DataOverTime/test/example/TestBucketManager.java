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

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for BucketManager. Extends TestCase.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TestBucketManager extends TestCase {

  private BucketManager bucketManager = new BucketManager();
  private DataQuery dataQuery;

  private Filter filter1;
  private Filter filter2;
  private Filter filter3;
  private Filter filter4;
  private Filter filter5;

  private String dimensionName;

  private String dimensionValue1;
  private String dimensionValue2;
  private String dimensionValue3;
  private String dimensionValue4;
  private String dimensionValue5;
  private List<String> dimensionValues = new ArrayList<String>(5);

  /**
   * Sets up the test.
   */
  public void setUp() {
    dataQuery = TestUtil.getFilledDataQuery();
    dataQuery.setFilters("ga:medium==cpc");

    dimensionName = "ga:source";
    dimensionValue1 = "01234567890123456789012";
    dimensionValue2 = "012345678901234567";
    dimensionValue3 = "0123456789012";
    dimensionValue4 = "01234567";
    dimensionValue5 = "012";

    dimensionValues.add(dimensionValue1);
    dimensionValues.add(dimensionValue2);
    dimensionValues.add(dimensionValue3);
    dimensionValues.add(dimensionValue4);
    dimensionValues.add(dimensionValue5);

    // The numbers represent the encoded size of each value.
    // This helps in determining what the expected output of
    // the tests below.
    filter1 = new Filter(dimensionName, dimensionValue1); // 40.
    filter2 = new Filter(dimensionName, dimensionValue2); // 35.
    filter3 = new Filter(dimensionName, dimensionValue3); // 30.
    filter4 = new Filter(dimensionName, dimensionValue4); // 25.
    filter5 = new Filter(dimensionName, dimensionValue5); // 20.
  }

  /**
   * Tests bad parameters don't cause errors.
   */
  public void testGetFiltersOrderedBySize_nullDimensionValues() {
    bucketManager.init(1000, 10000);
    List<Filter> filters = bucketManager.getFiltersOrderedBySize(dimensionName, null);
    assertNotNull(filters);
    assertEquals(0, filters.size());
  }

  /**
   * Tests empty dimension values don't cause errors.
   */
  public void testGetFiltersOrderedBySize_emptyDimensionValues() {
    bucketManager.init(0, 10000);
    List<String> dimensionValues = new ArrayList<String>();
    List<Filter> filters = bucketManager.getFiltersOrderedBySize(dimensionName,
        dimensionValues);

    assertNotNull(filters);
    assertEquals(0, filters.size());
  }

  /**
   * Tests that the return list is ordered by the size of each filter.
   */
  public void testGetFiltersOrderedBySize_noMaxCharLength() {
    List<String> dimensionValues = new ArrayList<String>();
    dimensionValues.add(dimensionValue4);
    dimensionValues.add(dimensionValue2);
    dimensionValues.add(dimensionValue1);
    dimensionValues.add(dimensionValue5);
    dimensionValues.add(dimensionValue3);

    bucketManager.init(1000, 10000);
    List<Filter> filters = bucketManager.getFiltersOrderedBySize(dimensionName,
        dimensionValues);

    assertNotNull(filters);
    assertEquals(dimensionValues.size(), filters.size());
    assertTrue(filter1.equals(filters.get(0)));
    assertTrue(filter2.equals(filters.get(1)));
    assertTrue(filter3.equals(filters.get(2)));
    assertTrue(filter4.equals(filters.get(3)));
    assertTrue(filter5.equals(filters.get(4)));
  }

  /**
   * Test that no filters greater than max char length get added to this list.
   */
  public void testGetFiltersOrderedBySize_hasMaxCharLength() {
    List<String> dimensionValues = new ArrayList<String>();
    dimensionValues.add(dimensionValue4);
    dimensionValues.add(dimensionValue2);
    dimensionValues.add(dimensionValue1);
    dimensionValues.add(dimensionValue5);
    dimensionValues.add(dimensionValue3);

    int filterMaxCharLength = filter3.getEncodedSize() - 1;
    bucketManager.init(filterMaxCharLength, 10000);
    List<Filter> filters = bucketManager.getFiltersOrderedBySize(dimensionName,
        dimensionValues);

    assertNotNull(filters);
    assertEquals(2, filters.size());
    assertTrue(filter4.equals(filters.get(0)));
    assertTrue(filter5.equals(filters.get(1)));
  }

  /**
   * Test returning all filters in 1 bucket. No constraints.
   * Expected output should be:
   * 1, 2, 3, 4, 5
   */
  public void testGetListOfBuckets_allFiltersInOneBucket() {
    int filterMaxCharLength = 50000;
    int filterMaxListSize = 1000;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(1, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter2);
    bucket1.add(filter3);
    bucket1.add(filter4);
    bucket1.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
  }

  /**
   * Test adding bucket size constraint to 2. No char size constraint.
   * Expected output should be:
   * 1, 2
   * 3, 4
   * 5
   */
  public void testGetListOfBuckets_twoFiltersPerBucket() {
    int filterMaxCharLength = 50000;
    int filterMaxListSize = 2;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(3, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter2);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter3);
    bucket2.add(filter4);

    Bucket bucket3 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket3.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
    assertTrue(bucket3.equals(buckets.get(2)));
  }

  /**
   * Test adding char size constraint to the size of 2 and 3 + operator size.
   * (68 chars)
   * No bucket size constraint. Expected output should be:
   * 1 (40 chars), (25 chars)
   * 2 (35 chars), (30 chars)
   * 3 (20 chars)
   */
  public void testGetListOfBuckets_filter2And3InABucket() {

    int filterMaxCharLength = filter2.getEncodedSize() + filter3.getEncodedSize() +
        Filter.getOperatorSize();
    int filterMaxListSize = 50000;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(3, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter4);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter2);
    bucket2.add(filter3);

    Bucket bucket3 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket3.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
    assertTrue(bucket3.equals(buckets.get(2)));
  }

  /**
   * Test adding char size constraint to the size of 1 and 3 + operator size.
   * (73 chars)
   * No bucket size constraint. Expected output should be:
   * 1 (40 chars), (30 chars)
   * 2 (35 chars), (25 chars)
   * 3 (20 chars)
   */
  public void testGetListOfBuckets_filter1And3InABucket() {

    int filterMaxCharLength = filter1.getEncodedSize() + filter3.getEncodedSize() +
        Filter.getOperatorSize();
    int filterMaxListSize = 50000;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(3, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter3);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter2);
    bucket2.add(filter4);

    Bucket bucket3 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket3.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
    assertTrue(bucket3.equals(buckets.get(2)));
  }

  /**
   * Test adding char size constraint to the size of 1 and 2 + operator size.
   * (78 chars)
   * No bucket size constraint. Expected output should be:
   * 1 (40 chars), (35 chars)
   * 2 (30 chars), (25 chars)
   * 3 (20 chars)
   */
  public void testGetListOfBuckets_filter1And2InABucket() {
    int filterMaxCharLength = filter1.getEncodedSize() + filter2.getEncodedSize() +
        Filter.getOperatorSize();
    int filterMaxListSize = 50000;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(3, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter2);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter3);
    bucket2.add(filter4);

    Bucket bucket3 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket3.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
    assertTrue(bucket3.equals(buckets.get(2)));
  }

  /**
   * Test adding char size constraint to the size of 3, 4 and 5 + 2 operators.
   * (81 chars)
   * No bucket size constraint. Expected output should be:
   * 1 (40 chars), (35 chars)
   * 2 (30 chars), (25 chars), (20 chars)
   */
  public void testGetListOfBuckets_filters3And4And5InAbucket() {
    int filterMaxCharLength = filter3.getEncodedSize() + filter4.getEncodedSize() +
        filter5.getEncodedSize() + (2 * Filter.getOperatorSize());
    int filterMaxListSize = 50000;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(2, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter2);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter3);
    bucket2.add(filter4);
    bucket2.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
  }

  /**
   * Test adding char size constraint to the size of 3, 4 and 5 + operators.
   * (81 chars)
   * Bucket size constraint of 2. Expected output should be:
   * 1 (40 chars), (35 chars)
   * 2 (30 chars), (25 chars)
   * 3 (20 chars)
   */
  public void testGetListOfBuckets_filter3And4InAbucketFilter5InAnewBucket() {
    int filterMaxCharLength = filter3.getEncodedSize() + filter4.getEncodedSize() +
        filter5.getEncodedSize() + (2 * Filter.getOperatorSize());
    int filterMaxListSize = 2;

    bucketManager.init(filterMaxCharLength, filterMaxListSize);
    List<Bucket> buckets = bucketManager.getBucketsOfFilters(dimensionName, dimensionValues);
    assertEquals(3, buckets.size());

    Bucket bucket1 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket1.add(filter1);
    bucket1.add(filter2);

    Bucket bucket2 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket2.add(filter3);
    bucket2.add(filter4);

    Bucket bucket3 = new Bucket(filterMaxCharLength, filterMaxListSize);
    bucket3.add(filter5);

    assertTrue(bucket1.equals(buckets.get(0)));
    assertTrue(bucket2.equals(buckets.get(1)));
    assertTrue(bucket3.equals(buckets.get(2)));
  }
}

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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all tests in the test suite.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AllTests {

  /**
   * Test suite entry point.
   * @param args Command line args.
   */
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Create and executes the entire suite.
   * @return The created suite.
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(TestDataManager.class);
    suite.addTestSuite(TestFilteredQueries.class);
    suite.addTestSuite(TestQueryManagerIndividualImpl.class);
    suite.addTestSuite(TestQueryManagerGroupImpl.class);
    suite.addTestSuite(TestResults.class);
    suite.addTestSuite(TestResultManagerIndividualImpl.class);
    suite.addTestSuite(TestResultManagerGroupImpl.class);
    suite.addTestSuite(TestBucket.class);
    suite.addTestSuite(TestFilter.class);
    suite.addTestSuite(TestDataQueryUtil.class);
    suite.addTestSuite(TestBucketManager.class);

    return suite;
  }
}


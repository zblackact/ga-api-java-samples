// Copyright 2010 Google Inc. All Rights Reserved.
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.data.analytics.AnalyticsLink;
import com.google.gdata.data.analytics.Destination;
import com.google.gdata.data.analytics.Engagement;
import com.google.gdata.data.analytics.Goal;
import com.google.gdata.data.analytics.ManagementEntry;
import com.google.gdata.data.analytics.ManagementFeed;
import com.google.gdata.data.analytics.Segment;
import com.google.gdata.data.analytics.Step;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;

/**
 * Sample program demonstrating how to make a data request to the Google 
 * Analytics Management API using client login authorization as well as
 * accessing important data in the Management feed.
 * @author api.alexl@google.com (Alexander Lucas)
 */
public class ManagementFeedExample {

  public static AnalyticsService analyticsService;

  public static final String CLIENT_USERNAME = "INSERT_USERNAME_HERE";
  public static final String CLIENT_PASSWORD = "INSERT_PASSWORD_HERE";
  public static final String CLIENT_NAME = "XTREME Sample App!";
  public static final String BASE_URL = "https://www.google.com/analytics/feeds/datasources/ga/";
  
  private ManagementFeedExample() {
    
  }
  /**
   * Each of these methods makes a request to the management API, and prints
   * the first entry of the response.  You can look at each one individually
   * to see how it works.
   */
  public static void main(String[] args) {
    
    initializeAnalyticsService();

    printFirstAccount();

    printFirstWebProperty();

    printFirstProfile();
    
    printFirstGoal();

    printFirstSegment();    
  }

  /**
   * Creates a new service object.  This is the object that will be used to
   * communicate with the API.
   */
  public static void initializeAnalyticsService() {
    try {
      analyticsService = new AnalyticsService(CLIENT_NAME);
      analyticsService.setUserCredentials(CLIENT_USERNAME, CLIENT_PASSWORD);
    } catch (AuthenticationException e) {
      System.err.println("Authentication failed : " + e.getMessage());
    } 
  }
  
  /**
   * Helper method which, given the URL to a Management Feed, uses the service
   * object to request that feed from the API.
   * 
   * @param url the URL for the feed being requested.
   * @return The server response in the form of a ManagementFeed object.
   */
  public static ManagementFeed getFeed(String url) {
    ManagementFeed resultFeed = null;
    try {
      URL feedURL = new URL(url);
      resultFeed = analyticsService.getFeed(feedURL, ManagementFeed.class);
    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed " + 
          url + ", Message: " + e.getMessage());
    } catch (ServiceException e) {
      System.err.println("Analytics API responded with an error message: " + e.getMessage());
    }    
    return resultFeed;
  }

  /**
   * Retrieves the account feed from the Management API and prints the 
   * first entry.
   */
  public static void printFirstAccount() {
    // Get feed
    ManagementFeed accountsFeed = getFeed(BASE_URL + "accounts?max-results=1");    
    // Get Account
    ManagementEntry account = accountsFeed.getEntries().get(0);
    // Print Account
    System.out.println("--- Account Entry ---");
    System.out.println("Account ID: " + account.getProperty("ga:accountId"));
    System.out.println("Account Name: " + account.getProperty("ga:accountName"));
  }

  /**
   * Retrieves the web property feed from the Management API and prints the 
   * first entry.
   */
  public static void printFirstWebProperty() {
    ManagementFeed webPropertiesFeed = getFeed(BASE_URL + 
        "accounts/~all/webproperties?max-results=1");

    ManagementEntry webProperty = webPropertiesFeed.getEntries().get(0);

    System.out.println("--- Web Property Entry ---");    
    System.out.println("Account ID: " + webProperty.getProperty("ga:accountId"));
    System.out.println("Web Property ID: " + webProperty.getProperty("ga:webPropertyId"));
  }

  /**
   * Retrieves the profile feed from the Management API and prints the 
   * first entry.
   */
  public static void printFirstProfile() {
    ManagementFeed profilesFeed = getFeed(BASE_URL + 
        "accounts/~all/webproperties/~all/profiles?max-results=1");

    ManagementEntry profile = profilesFeed.getEntries().get(0);
    
    System.out.println("--- Profile Entry ---");
    System.out.println("Account ID: " + profile.getProperty("ga:accountId"));
    System.out.println("Web Property ID: " + profile.getProperty("ga:webPropertyId"));
    System.out.println("Profile ID: " + profile.getProperty("ga:profileId"));
    System.out.println("Currency: " + profile.getProperty("ga:currency"));
    System.out.println("Timezone: " + profile.getProperty("ga:timezone"));
    System.out.println("Table ID: " + profile.getProperty("dxp:tableId"));
  }

  /**
   * Retrieves the goal feed from the Management API and prints the 
   * first entry.
   */
  public static void printFirstGoal() {
    ManagementFeed goalFeed = getFeed(BASE_URL +
        "accounts/~all/webproperties/~all/profiles/~all/goals?max-results=1");
    
    ManagementEntry goalEntry = goalFeed.getEntries().get(0);
    printGoal(goalEntry);
  }

  /**
   * Given a ManagementEntry object containing a goal, extracts the goal object,
   * determines the *type* of that goal object (Destination or Engagement), and 
   * prints out relevant data.
   * 
   * @param goalEntry The ManagementEntry representing a single goal.
   */
  public static void printGoal(ManagementEntry goalEntry) {

    Goal goal = goalEntry.getGoal();
    System.out.println("Printing Goal #" + goal.getNumber());
    System.out.println("Name: " + goal.getName());
    System.out.println("Active? " + goal.getActive());
    System.out.println("Value: " + goal.getValue());

    if (goal.getDestination() != null) {
      Destination destination = goal.getDestination();
      System.out.println("Goal Type:  Destination");
      System.out.println("Destination - Case Sensitive: " +  destination.getCaseSensitive());
      System.out.println("Destination - Expression: " +  destination.getExpression());
      System.out.println("Destination - Match Type: " +  destination.getMatchType());
      System.out.println("Destination - Step 1 Required: : " +  destination.getStep1Required());

      System.out.println("Goal Steps: ");

      for (Step step : goal.getDestination().getSteps()) {
        System.out.println("Step: " + step.getNumber());
        System.out.println("Name: " + step.getName());
        System.out.println("Path: " + step.getPath());

      } 
    } else if (goal.getEngagement() != null) {
      Engagement engagement = goal.getEngagement();
      System.out.println("Goal Type:  Engagement");
      System.out.println("Engagement - Type: " +  engagement.getType());
      System.out.println("Engagement - Threshhold Value: " +  engagement.getThresholdValue());
      System.out.println("Engagement - Comparison: " +  engagement.getComparison());

    }
  }
  
  /**
   * Retrieves the goal feed from the Management API and prints the 
   * first entry.
   */
  public static void printFirstSegment() {
    ManagementFeed segmentFeed = getFeed(BASE_URL + "segments");
        
    ManagementEntry segmentEntry = segmentFeed.getEntries().get(0);
    Segment segment = segmentEntry.getSegment();    
    System.out.println("--- Segment ---");
    System.out.println("Advanced Segment ID: " + segment.getId());
    System.out.println("Advanced Segment Name: " + segment.getName());
    System.out.println("Advanced Segment Definition: " + segment.getDefinition().getValue());
  }

  /**
   * Helper method which prints out the values contained within an 
   * AnalyticsLink object.
   */     
  public static void printLinkDetails(AnalyticsLink link) {    
    // An AnalyticsLink has 4 basic properties:
    
    // Rel:  The relationship between the feed represented by this link object,
    // and the ManagementEntry object containing it.
    System.out.println("Link rel: " + link.getRel());
    
    // Href: the actual URL this AnalyticsLink object represents. 
    System.out.println("Link href: " + link.getHref());
    
    // TargetKind - The kind of data represented in the management feed this 
    // AnalyticsLink targets. For instance, an Account Feed's child link is 
    // going to have a target kind of "analytics#webproperty", indicating the 
    // feed will be of web properties.
    System.out.println("Link target kind: " + link.getTargetKind());
  }
}

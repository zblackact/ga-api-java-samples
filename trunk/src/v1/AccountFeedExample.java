// Copyright 2009 Google Inc. All Rights Reserved.
 
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample program demonstrating how to make a data request to the GA Data Export API
 * using client login authentication as well as accessing important data in the account feed.
 */
public class AccountFeedExample {

  private static final String CLIENT_USERNAME = "INSERT_LOGIN_EMAIL_HERE";
  private static final String CLIENT_PASS = "INSERT_PASSWORD_HERE";

  public static void main(String args[]) {

    //------------------------------------------------------
    // Configure GA API
    //------------------------------------------------------
    AnalyticsService as = new AnalyticsService("gaExportAPI_acctSample_v1.0");
    String baseUrl = "https://www.google.com/analytics/feeds/accounts/default";

    //------------------------------------------------------
    // Client Login Authentication
    //------------------------------------------------------
    try {
      as.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);
    } catch (AuthenticationException e) {
      System.err.println("Authentication failed : " + e.getMessage());
      return;
    }

    //------------------------------------------------------
    // GA Account Feed
    //------------------------------------------------------
    URL queryUrl;
    try {
      queryUrl = new URL(baseUrl);
    } catch (MalformedURLException e) {
      System.err.println("Malformed URL: " + baseUrl);
      return;
    }

    // Send our request to the Analytics API and wait for the results to come back
    AccountFeed accountFeed;
    try {
      accountFeed = as.getFeed(queryUrl, AccountFeed.class);
    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
      return;
    } catch (ServiceException e) {
      System.err.println("Analytics API responded with an error message: " + e.getMessage());
      return;
    }

    //------------------------------------------------------
    // Format Feed Related Data
    //------------------------------------------------------
    // Print top-level information about the feed
    System.out.println(
      "\nFeed Title     = " + accountFeed.getTitle().getPlainText() + 
      "\nTotal Results  = " + accountFeed.getTotalResults() +
      "\nStart Index    = " + accountFeed.getStartIndex() +
      "\nItems Per Page = " + accountFeed.getItemsPerPage() +
      "\nFeed Id        = " + accountFeed.getId());

    // Print the feeds' entry data
    for (AccountEntry entry : accountFeed.getEntries()) {
      System.out.println(
        "\nWeb Property Id = " + entry.getProperty("ga:webPropertyId") +
        "\nAccount Name    = " + entry.getProperty("ga:accountName") +
        "\nAccount Id      = " + entry.getProperty("ga:accountId") +
        "\nProfile Id      = " + entry.getProperty("ga:profileId") +
        "\nProfile Name    = " + entry.getTitle().getPlainText() +
        "\nEntry Id        = " + entry.getId() +
        "\nTable Id        = " + entry.getTableId().getValue());
    }
  }
}

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

import com.google.api.adwords.lib.AdWordsService;
import com.google.api.adwords.lib.AdWordsServiceLogger;
import com.google.api.adwords.lib.AdWordsUser;
import com.google.api.adwords.v200909.cm.*;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.AuthenticationException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.xml.rpc.ServiceException;

/**
 * Sample application demonstrating how to make data requests to the Analytics
 * Export API and correlate it with data from the AdWords API.  Application
 * will pull data from Analytics, use it to pull associated data from AdWords,
 * and print out the data in CSV format.
 * Usage of the AdWords API is governed by the AdWords API terms and conditions
 * available at http://code.google.com/apis/adwords/docs/terms.html
 * @author api.alexl@google.com (Alexander Lucas)
 */
public class AnalyticsAdWordsAPISample {

  private static final String USERNAME = "INSERT_USERNAME_HERE";
  private static final String PASSWORD = "INSERT_PASSWORD_HERE";
  private static final String PROFILE_ID = "INSERT_GA_PROFILEID_HERE";
  private static final String CLIENT_NAME = "GA-Export-API-Sample";

  private static final String GA_START_DATE = "2010-03-01";
  private static final String GA_END_DATE = "2010-03-31";
  private static final int MAX_RESULTS = 100;


  public static void main(String[] args) {
    // Entrance point of the application
    AnalyticsAdWordsAPISample example = new AnalyticsAdWordsAPISample();
    DataFeed analyticsData;
    AdGroupCriterionPage criterionPage;

    try {

      // Get Data Feed from Google Analytics.
      analyticsData = example.getAnalyticsData();

      // Get an array of AdWords IDs from the Google Analytics Data Feed.
      AdGroupCriterionIdFilter[] criterionFilters = example.getCriterionFilters(analyticsData);

      // Get data from AdWords.
      criterionPage = example.getAdGroupCriterionPage(criterionFilters);

    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
      return;
    } catch (ServiceException e) {
      System.err.println("API responded with an error message" + e.getMessage());
      return;
    } catch (AuthenticationException e) {
      System.err.println("There has been an error authenticating: " + e.getMessage());
      return;
    } catch (com.google.gdata.util.ServiceException e) {
      System.err.println("API responded with error message");
      return;
    }

    // Output data in CSV format.  Copy/Paste to your favorite spreadsheet app.
    example.printDataAsCSV(analyticsData, criterionPage);
  }

  /**
   * Creates a new AnalyticsService object and authorizes the user using
   * Client Login. Then constructs a query, makes a request to the
   * Google Analytics API and returns the entire feed.
   * @return A set of data from Google Analytics API.
   * @throws IOException If there's an error trying to retrieve data from
   *         the network.
   * @throws com.google.gdata.util.ServiceException If API responds
   *         with an error.
   */
  public DataFeed getAnalyticsData() throws  IOException,
      com.google.gdata.util.ServiceException {

    AnalyticsService analyticsService = new AnalyticsService(CLIENT_NAME);
    analyticsService.setUserCredentials(USERNAME, PASSWORD);

    DataQuery dataQuery = new DataQuery(new URL("https://www.google.com/analytics/feeds/data"));
    dataQuery.setIds(PROFILE_ID);
    dataQuery.setStartDate(GA_START_DATE);
    dataQuery.setEndDate(GA_END_DATE);

    // Dimensions:  Boundaries for the data being requested.
    dataQuery.setDimensions("ga:year,ga:month,ga:adwordsCampaignID," +
        "ga:adwordsAdGroupID,ga:adwordsCriteriaID");

    // Metrics:  The data being requested
    dataQuery.setMetrics("ga:visits,ga:entrances,ga:bounces,ga:transactions," +
        "ga:transactionRevenue,ga:goalCompletionsAll,ga:goalValueAll");

    // A descending sort (highest values first) can be requested by placing a negative symbol in
    // front of the metric to sort by.
    dataQuery.setSort("-ga:transactionRevenue,-ga:entrances");

    // This example is specifically comparing Analytics data to keywords set up in AdWords.
    // AdWords returns clicks achieved via Content Targetting (think adsense on other websites)
    // as having a Criteria ID of 3000000.  While important, these aren't relevant to this example.
    // Thus, filter them out.
    dataQuery.setFilters("ga:adwordsCriteriaID!=3000000");
    dataQuery.setMaxResults(MAX_RESULTS);
    return analyticsService.getFeed(dataQuery.getUrl(), DataFeed.class);
  }

  /**
   * Takes data from Google Analytics Export API and converts it into an array
   * of Criterion Filters.  These are used to instruct the AdWords API on what
   * data we want to retrieve from the API.
   * @param analyticsData DataFeed from Export API - Gives us the IDs of
   *        entities in AdWords API.
   * @return An array of "Criterion filters" which define what AdWords
   *         Criterion we want to pull.
   */
  public AdGroupCriterionIdFilter[] getCriterionFilters(DataFeed analyticsData) {
    int numFilters = analyticsData.getEntries().size();
    AdGroupCriterionIdFilter[] critFilters = new AdGroupCriterionIdFilter[numFilters];
    for (int i = 0; i < numFilters; i++) {

      // This is the crossover point between the two APIs.
      // In the Analytics request, the data returned was grouped by AdWords Group ID and
      // Criterion ID.  Now those two pieces of information will be retrieved from the data received
      // from Google Analytics, and placed into a set of "filters" that will be part of the request
      // sent ot the AdWords API.

      DataEntry entry = analyticsData.getEntries().get(i);

      // All dimensions are returned from the API as Strings, but in the case of groupID and
      // criterionID, they're really longs.  Cast as long using Long.parseLong(str).
      Long groupID = Long.parseLong(entry.stringValueOf("ga:adwordsAdGroupID"));
      Long critID = Long.parseLong(entry.stringValueOf("ga:adwordsCriteriaID"));

      // Add AdWords IDs from Google Analytics to criteria filter array.
      AdGroupCriterionIdFilter critFilter = new AdGroupCriterionIdFilter();
      critFilter.setAdGroupId(groupID);
      critFilter.setCriterionId(critID);
      critFilters[i] = critFilter;
    }
    return critFilters;
  }

  /**
   * Pulls information from the AdWords API for criterion specified in the set
   * of filters passed in.
   * @param criterionFilters Set of filters determining which entities are
   *        pulled via the API.
   * @return The first page of AdWords Criterion Data corresponding to the
   *         Analytics data passed to the method.
   * @throws IOException If there's an error trying to retrieve data from
   *         the network.
   * @throws ServiceException If API responds with an error.
   */
  public AdGroupCriterionPage getAdGroupCriterionPage(AdGroupCriterionIdFilter[] criterionFilters)
      throws IOException, ServiceException {

    // While not strictly necessary, the Java wrapper for the AdWords API utilizes log4j logging.
    // For debugging purposes, as you modify or play with this code, it's highly recommended that
    // this logging functionality stay on.  To turn it off, just comment out this line.
    AdWordsServiceLogger.log();

    // Get AdWordsUser from "~/adwords.properties". If you're not using an adwords.properties file
    // to store your config, the constructor will look like
    // AdWordsUser user = new AdWordsUser(USER, PASS, CLIENTID, USERAGENT, DEVTOKEN, USE_SANDBOX);
    AdWordsUser user = new AdWordsUser();

    // The Selector object collects and organizes all the information we're eventually going to
    // send out to the AdWords API in our request.
    AdGroupCriterionSelector agcSelector = new AdGroupCriterionSelector();
    agcSelector.setCriterionUse(CriterionUse.BIDDABLE);
    agcSelector.setUserStatuses(new UserStatus[] {UserStatus.ACTIVE, UserStatus.DELETED,
        UserStatus.PAUSED});

    // Analytics and AdWords use slightly different string formats for their dates.  The date format
    // used by AdWords is similar, but does not include hyphens.
    String awStartDate = GA_START_DATE.replaceAll("-", "");
    String awEndDate = GA_END_DATE.replaceAll("-", "");
    DateRange dateRange = new DateRange(awStartDate, awEndDate);

    StatsSelector statsSelector = new StatsSelector();
    statsSelector.setDateRange(dateRange);
    agcSelector.setStatsSelector(statsSelector);
    // Set start index and number of items per "page" desired in the response.
    agcSelector.setPaging(new Paging(0, MAX_RESULTS));
    agcSelector.setIdFilters(criterionFilters);

    AdGroupCriterionServiceInterface agcService = user.getService(
        AdWordsService.V200909.ADGROUP_CRITERION_SERVICE);

    return agcService.get(agcSelector);
  }

  /**
   * Cross-References Analytics data with AdWords data, prints results in
   * table-style CSV format.
   * @param dataFeed The data from Google Analytics which is going to be
   *        printed out.
   * @param criterionPage A HashMap of the AdWords data, keyed on Criterion ID.
   */
  public void printDataAsCSV(DataFeed dataFeed, AdGroupCriterionPage criterionPage) {

    // Data wrangling.  We have a small set of Analytics data, and a small set of AdWords data.
    // A database would be overkill.  What to do?  Hashmaps to the rescue!
    HashMap<String, AdGroupCriterion> critDict = new HashMap<String, AdGroupCriterion>();
    for (AdGroupCriterion criterion : criterionPage.getEntries()) {
      critDict.put(criterion.getCriterion().getId().toString(), criterion);
    }

    System.out.println("Year,Month,Campaign ID,Ad Group ID,Criteria ID,Visits," +
        "Entrances,Bounces,Transactions,Transaction Revenue,Goal Completions,Goal Value," +
        "Keyword,Quality Score,Impressions,Clicks,Average Position,Cost,Average CPC, Max CPC");

    for (DataEntry entry : dataFeed.getEntries()) {
      StringBuffer buffer = new StringBuffer();
      for (Dimension dimension : entry.getDimensions()) {
        buffer.append(entry.stringValueOf(dimension.getName()) + ",");
      }

      for (Metric metric : entry.getMetrics()) {
        buffer.append(entry.stringValueOf(metric.getName()) + ",");
      }

      String criterionID = entry.stringValueOf("ga:adwordsCriteriaID");
      BiddableAdGroupCriterion bagc = (BiddableAdGroupCriterion) critDict.get(criterionID);

      if (bagc != null) {
        // The stats object contains lots of interesting data.  This example will be printing out
        // the number of impressions, clicks, and average position of the criterion.  The list of
        // stats available via this object is available here:
        // http://code.google.com/apis/adwords/v2009/docs/reference/AdGroupCriterionService.Stats.html
        Stats stats = bagc.getStats();

        // Max and average bids
        double avgCPC = moneyInDollars(stats.getAverageCpc());
        // Total spent on this criterion
        double cost = moneyInDollars(stats.getCost());

        // Getting the max CPC - First need to check if the bids are "Manual CPC" type-
        // otherwise the idea of Max CPC doesn't really apply.
        double maxCPC = 0.0;
        if (bagc.getBids() instanceof ManualCPCAdGroupCriterionBids) {
          ManualCPCAdGroupCriterionBids bids = (ManualCPCAdGroupCriterionBids) bagc.getBids();
          maxCPC = moneyInDollars(bids.getMaxCpc().getAmount());
        }

        if (bagc.getCriterion() instanceof Keyword) {
          Keyword keyword = (Keyword) (bagc.getCriterion());
          buffer.append(keyword.getText());
          buffer.append("," + bagc.getQualityInfo().getQualityScore().toString());
          buffer.append("," + stats.getImpressions());
          buffer.append("," + stats.getClicks());
          buffer.append("," + stats.getAveragePosition());
          buffer.append("," + cost);
          buffer.append("," + avgCPC);
          buffer.append("," + maxCPC);
        }
      }
      System.out.println(buffer.toString());
    }
  }

  /**
   * Money objects in the AdWords system return currency amounts in a unit
   * called a "micro".  The conversion rate is 1 million micros = 1 US Dollar.
   * This is a simple helper method to convert money objects received via the
   * AdWords API into US dollars. Reference:
   * http://code.google.com/apis/adwords/docs/developer/adwords_api_services.html#moneyunits
   * @param money object representing the amount you want to convert.
   * @return Value in dollars.
   */
  public double moneyInDollars(Money money) {
    return money.getMicroAmount() / 1000000.0;
  }
}

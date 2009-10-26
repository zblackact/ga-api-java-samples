// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Makes all requests to the Google Analytics Data Export API.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AnalyticsServiceWrapper {

  /** The URL all Google Analytics Data Feed requests start with */
  private static final String BASE_DATA_FEED_URL = "https://www.google.com/analytics/feeds/data";

  private AnalyticsService analyticsService;
  private AuthorizationService authService;
  private String tableId;
  private Boolean tokenValid;
  private String accountListError;
  private String dataListError;

  /**
   * Constructor.
   * @param analyticsService The AnalyticsService object to make requests to the
   *     Google Analytics API.
   * @param authService The AuthorizationService implementation used to add the authorization
   *     token into the Google Analytics service object.
   * @param tableId the ids parameter of the Data Feed query that corresponds to the Google
   *     Analytics tableId.
   */
  public AnalyticsServiceWrapper(AnalyticsService analyticsService,
      AuthorizationService authService, String tableId) {

    this.analyticsService = analyticsService;
    this.authService = authService;
    this.tableId = tableId;

    // Increase the request timeout to 10 seconds for App Engine.
    analyticsService.setConnectTimeout(10000);
  }

  /**
   * Sets the token into the analytics service object. The logic to set the token is in the
   * AuhtorizationSerivice because each authorization implementation handles setting the
   * token differently.
   */
  public void setToken(UserToken userToken) {
    tokenValid = false;
    if (userToken.hasSessionToken()) {
      authService.putTokenInGoogleService(userToken, analyticsService);

      // Assume the token is valid.
      tokenValid = true;
    }
  }

  /**
   * Returns the table id.
   * @return the table id.
   */
  public String getTableId() {
    return tableId;
  }

  /**
   * Returns if the token was valid.
   * @return if the token was valid.
   */
  public Boolean isTokenValid() {
    return tokenValid;
  }

  /**
   * Returns a URL to query the Google Analytics API account feed. This query retrieves the 1000
   * 1000 accounts the current authorized user has access to.
   * @return the account feed URL.
   * @throws MalformedURLException if the URL isn't correct formed.
   */
  private URL getAccountFeedQuery() throws MalformedURLException {
    return new URL("https://www.google.com/analytics/feeds/accounts/default");
  }
  
  /**
   * Requests the Google Analytics account information profiles for the currently authorized user.
   * Then extract the the profile name and table id into a list.
   * @return a list of string arrays with the data from the API.
   */
  public List<String []> getAccountList() {

    if (!isTokenValid()){
      return null;
    }

    Boolean firstEntry = true;
    List<String []> accountList = new ArrayList<String[]>();

    try {
      // Make a request to the Account Feed.
      AccountFeed accountFeed = analyticsService.getFeed(getAccountFeedQuery(), AccountFeed.class);

      // Put the results in a list of String arrays.
      for (AccountEntry entry : accountFeed.getEntries()) {
        accountList.add(new String[] {
          entry.getTableId().getValue(),
          entry.getTitle().getPlainText()
        });

        // If no tableId has been configured, use the first tableId from the 
        // Account feed as a default.
        if (firstEntry && tableId == null) {
          firstEntry = false;
          tableId = entry.getTableId().getValue();
        }
      }
    } catch (ServiceException e) {
      // If the token in the data store has been revoked, a 401 error is thrown. To continue,
      // the user can just request another token from the authorization routine. So no error
      // message is sent to the user.
      if (e.getHttpErrorCodeOverride() == 401) {
        tokenValid = false;
      } else {
        accountListError = e.getMessage();
      }
    } catch (IOException e) {
      accountListError = e.getMessage();
    }
    return accountList;
  }

  /**
   * Returns a URL to query the Google Analytics API Data Feed. This query retrieves the top 100
   * landing pages and their entrance and bounce metrics sorted by entrances for the last 14 days
   * starting yesterday.
   * @return a Google Analytics API query.
   * @throws MalformedURLException
   */
  private URL getDataFeedQuery() throws MalformedURLException {
    // Get yesterday's date.
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_MONTH, -1);

    // Get the date 14 days before yesterday.
    Calendar twoWeeksAgo = Calendar.getInstance();
    twoWeeksAgo.add(Calendar.DAY_OF_MONTH, -15);

    // Formatter for date.
    SimpleDateFormat gaDate = new SimpleDateFormat("yyyy-MM-dd");

    // Make a query.
    DataQuery query = new DataQuery(new URL(BASE_DATA_FEED_URL));
    query.setIds(tableId);
    query.setDimensions("ga:landingPagePath");
    query.setMetrics("ga:entrances,ga:bounces");
    query.setSort("-ga:entrances"); 
    query.setMaxResults(500);
    query.setStartDate(gaDate.format(twoWeeksAgo.getTime()));
    query.setEndDate(gaDate.format(yesterday.getTime()));
    return query.getUrl();
  }

  /**
   * Requests the Google Analytics profile information for the table id set in this object. If no
   * table id has been set, the table id of the first account retrieved in the setAccountList
   * method is used. This method then extracts the the top 10 ga:source, ga:medium dimensions
   * along with the ga:visits, ga:bouces metrics and sets them in this object's dataList member.
   * @return a list of string arrays with the data from the API.
   */
  public List<String []> getDataList() {
    List<String []> dataList = new ArrayList<String[]>();
    Double bounceRate = 0.0;
    DecimalFormat twoPlaces = new DecimalFormat("#.00");

    if (!isTokenValid()) {
      return dataList;
    }

    try {
      // Make a request to the Data Feed.
      DataFeed dataFeed = analyticsService.getFeed(getDataFeedQuery(), DataFeed.class);
      dataList.add(new String[] {"Landing Page", "Entrances", "Bounces", "Bounce Rate"});

      // Put the results in a list of String arrays.
      for (DataEntry entry : dataFeed.getEntries()) {
        // Calculate bounce rate.
        bounceRate = entry.doubleValueOf("ga:bounces") / entry.doubleValueOf("ga:entrances") * 100;
          
        dataList.add(new String[] {
          entry.stringValueOf("ga:landingPagePath"),
          entry.stringValueOf("ga:entrances"),
          entry.stringValueOf("ga:bounces"),
          twoPlaces.format(bounceRate) + '%'
        });
      }
    } catch (ServiceException e) {
      dataListError = e.getMessage();
    } catch (IOException e) {
      dataListError = e.getMessage();
    }
    return dataList;
  }

  /**
   * Returns the message from any errors encountered by retrieving account list.
   * @return the error encountered by retrieving the account list.
   */
  public String getAccountListError() {
    return accountListError;
  }

  /**
   * Returns the message from any errors encountered by retrieving the data list.
   * @return the error encountered by retrieving the data list.
   */
  public String getDataListError() {
    return dataListError;
  }
}


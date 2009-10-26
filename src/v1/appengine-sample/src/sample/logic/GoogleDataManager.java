// Copyright 2009 Google Inc. All Rights Reserved.

package sample.logic;

import com.google.appengine.api.users.UserService;

import sample.controller.AuthorizationServlet;
import sample.controller.MainServlet;
import sample.data.AnalyticsServiceWrapper;
import sample.data.GoogleData;
import sample.data.TokenDao;

/**
 * Handles all the logic to make requests to Google Data APIs.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class GoogleDataManager {

  /** The scope this manager needs to fulfill the Google Data requests */
  static final String GOOGLE_DATA_SCOPE = "https://www.google.com/analytics/feeds/";

  private UserService userService;
  private AnalyticsServiceWrapper analyticsWrapper;

  /**
   * Constructor. Gets the logged in user's id, retrieves an authorization token from the data
   * store for the user (if one exists), then passes the token to the analyticsWrapper object
   * so it can make authenticated Google Data API requests.
   * @param analyticsWrapper handles all the requests to the Analytics API.
   */
  public GoogleDataManager(UserService userService, TokenDao tokenDao,
      AnalyticsServiceWrapper analyticsWrapper) {
    this.userService = userService;
    this.analyticsWrapper = analyticsWrapper;

    // Get user id from logged in user.
    String userId = null;
    if (userService.getCurrentUser() != null) {
      userId = userService.getCurrentUser().getUserId();
    }
    // Initialize the AnalyticsServiceWrapper.
    analyticsWrapper.setToken(tokenDao.retrieveTokenById(userId));
  }

  /**
   * Retrieves Google Analytics API data using the session token in the userToken parameter, and
   * returns the results as a GoogleData object.
   * @param userToken holds the authorized session token.
   */
  public GoogleData getGoogleData() {

    // Put the data from the API requests into a Google Data object.
    GoogleData googleData = new GoogleData();

    // Get authentication data.
    String authenticationUrl = userService.isUserLoggedIn()
        ? userService.createLogoutURL(MainServlet.MAIN_URL)
        : userService.createLoginURL(MainServlet.MAIN_URL);
    googleData.setAuthenticationUrl(authenticationUrl);
    googleData.setIsLoggedIn(userService.isUserLoggedIn());

    // Get Google Analytics account data.
    googleData.setAccountList(analyticsWrapper.getAccountList());
    googleData.setAccountListError(analyticsWrapper.getAccountListError());

    // Get Google Analytics profile data.
    googleData.setTableId(analyticsWrapper.getTableId());
    googleData.setDataList(analyticsWrapper.getDataList());
    googleData.setDataListError(analyticsWrapper.getDataListError());

    // Get authorization data.
    googleData.setTokenValid(analyticsWrapper.isTokenValid());
    String authorizationUrl = analyticsWrapper.isTokenValid()
        ? AuthorizationServlet.REVOKE_TOKEN_HANDLER
        : AuthorizationServlet.AUTHORIZATION_HANDLER;
    googleData.setAuthorizationUrl(authorizationUrl);

    return googleData;
  }
}


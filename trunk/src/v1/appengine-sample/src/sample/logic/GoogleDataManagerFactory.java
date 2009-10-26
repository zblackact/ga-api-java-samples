// Copyright 2009 Google Inc. All Rights Reserved.

package sample.logic;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import sample.data.AnalyticsServiceWrapper;
import sample.data.AuthorizationServiceAuthSubImpl;
import sample.data.AuthorizationServiceOauthImpl;
import sample.data.PMF;
import sample.data.TokenDaoJdoImpl;

/**
 * Factory to create GoogleDataManager objects. 
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class GoogleDataManagerFactory {

  /**
   * Constructor.
   */
  private GoogleDataManagerFactory() {}

  /**
   * Builds a new GoogleDataManager class to retrieve data from the Google Analytics API
   * using authSub authorization.
   * @param applicationName The name to pass to the AnalyticsService Object
   * @param tableId the Google Analytics table id.
   * @return a GoogleDataManager object.
   */
  public static GoogleDataManager getAuthSubManager(String applicationName, String tableId) {
    return new GoogleDataManager(
        UserServiceFactory.getUserService(),
        new TokenDaoJdoImpl(
            PMF.getInstance()),
        new AnalyticsServiceWrapper(
            new AnalyticsService(applicationName),
            new AuthorizationServiceAuthSubImpl(
                GoogleDataManager.GOOGLE_DATA_SCOPE),
            tableId));
  }
  
  /**
   * Builds a new GoogleDataManager class to retrieve data from the Google Analytics API
   * using oAuth authorization.
   * @param applicationName The name to pass to the AnalyticsService Object
   * @param tableId the Google Analytics table id.
   * @return a GoogleDataManager object.
   */
  public static GoogleDataManager getOauthManager(String applicationName, String tableId) {
    return new GoogleDataManager(
        UserServiceFactory.getUserService(),
        new TokenDaoJdoImpl(
            PMF.getInstance()),
        new AnalyticsServiceWrapper(
            new AnalyticsService(applicationName),
            new AuthorizationServiceOauthImpl(
                GoogleDataManager.GOOGLE_DATA_SCOPE,
                new GoogleOAuthParameters()),
            tableId));
  }
}

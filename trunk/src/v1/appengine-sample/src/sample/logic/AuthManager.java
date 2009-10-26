// Copyright 2009 Google Inc. All Rights Reserved.

package sample.logic;

import com.google.appengine.api.users.UserService;

import sample.controller.AuthorizationServlet;
import sample.data.AuthorizationService;
import sample.data.GoogleData;
import sample.data.TokenDao;
import sample.data.UserToken;

/**
 * Handles logic to get an authenticated authorization token.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AuthManager {

  private UserService userService;
  private TokenDao tokenDao;
  private AuthorizationService authService;

  private static final String HOME_URL = "/main";

  /**
   * Constructor.
   */
  public AuthManager(UserService userService, AuthorizationService authService,
      TokenDao tokenDao) {

    this.userService = userService;
    this.authService = authService;
    this.tokenDao = tokenDao;
  }

  /**
   * Returns an AuthData object with the authentication and authorization data along with
   * any errors that occurred.
   * @param tokenValid if the authorization token was valid.
   * @return the authentication and authorization data.
   */
  public GoogleData getGoogleData() {
    GoogleData googleData = new GoogleData();

    // Set the authentication data.
    googleData.setIsLoggedIn(userService.isUserLoggedIn());
    String authenticationUrl = userService.isUserLoggedIn()
        ? userService.createLogoutURL(HOME_URL)
        : userService.createLoginURL(HOME_URL);
    googleData.setAuthenticationUrl(authenticationUrl);

    // Set the authorization data.
    googleData.setAuthorizationUrl(AuthorizationServlet.AUTHORIZATION_HANDLER);
    googleData.setAuthorizationErrorMessage(authService.getAuthServiceError());

    return googleData;
  }

  /**
   * Stores an authorization for the user in the data store and is called after the user
   * is redirected back from Google with a token in the URL query parameter. This method,
   * gets the current user id, upgrades the token from the request to a long lived session
   * token. Finally the token is stored in the Data Store using the user id as a key.
   * @param requestQuery The query string with a token parameter.
   */
  public void storeAuthorizedToken(String requestQuery) {

    String userId = userService.getCurrentUser().getUserId();
    String sessionToken = authService.getSessionTokenFromString(requestQuery);

    UserToken tmpUserToken = new UserToken(userId, sessionToken);
    tokenDao.storeToken(tmpUserToken);
  }

  /**
   * Revokes the current user's token and removes it from the data store.
   */
  public void revokeToken() {

    String userId = userService.getCurrentUser().getUserId();
    UserToken tmpUserToken = tokenDao.retrieveTokenById(userId);
    authService.revokeToken(tmpUserToken);
    tokenDao.removeTokenById(userId);
  }

  /**
   * Returns the URL that takes a user to Google and grant this application access to their data.
   * @param request The HTTPServletRequest object from the AuthorizationServlet.
   * @return a URL to redirect a user to Google to get an authorization token.
   */
  public String getAuthorizationRedirectUrl(StringBuffer requestUrl) {
    return authService.getAuthorizationRedirectUrl(requestUrl);
  }

  /**
   * Returns the name of the parameter that will hold a token returned from the Google
   * Authorization service.
   * @return the name of token parameter returned from Google.
   */
  public String getTokenParam() {
    return authService.getTokenParam();
  }
}


// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import com.google.gdata.client.GoogleService;

/**
 * Handles communicating with the authorization service.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public interface AuthorizationService {

  /**
   * Returns the authorization end point URL to redirect a user to so they can grant this
   * application access to their data.
   * @param the requestUrl made to this servlet.
   * @return the authorization URL for a user to go to and authorize.
   */
  public String getAuthorizationRedirectUrl(StringBuffer requestUrl);
  
  /**
   * Revokes the authorization token for the current user.
   * @param userToken Contains the token to revoke.
   */
  public void revokeToken(UserToken userToken);
  
  /**
   * Converts the single use token found in the requestQuery parameter into
   * a long lived session token. 
   * @param requestQuery the queryString that has a token parameter whose value is a 
   *     single use token.
   * @return the long lived session token.
   */
  public String getSessionTokenFromString(String requestQuery);

  /**
   * Returns an error message if any of the AuthSub methods encountered an error.
   * @return the error message.
   */
  public String getAuthServiceError();

  /**
   * Handles putting the sessionToken into the a GoogleService object. This task is defined here
   * because each authorization implementation has a different way of setting the token.
   * @param userToken contains the authenticated token.
   * @param googleService The token is set into this Google Data API service object.
   */
  public void putTokenInGoogleService(UserToken userToken, GoogleService googleService);

  /**
   * Returns the name of the parameter that holds the token returned from the
   * Google Accounts service.
   * @return the name of the token parameter.
   */
  public String getTokenParam();
}


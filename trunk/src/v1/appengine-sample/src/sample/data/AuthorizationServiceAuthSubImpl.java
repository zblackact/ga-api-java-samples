// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;

import sample.controller.AuthorizationServlet;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Handles making AuthSub requests to Google to get an authenticated Google Data token.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AuthorizationServiceAuthSubImpl implements AuthorizationService {

  /** Name of the AuthSub token parameter in the URL returned from Google */
  private static final String TOKEN_PARAM = "token";

  private String scope;
  private String authServiceError;

  /**
   * Constructor.
   * @param scope The AuthSub scope to retrieve a token for.
   */
  public AuthorizationServiceAuthSubImpl(String scope) {
    this.scope = scope;
  }

  /**
   * Returns the AuthSub URL to redirect a user to so they can grant this application access to
   * their data.
   * @param requestUrl the query parameters from the HTTP servlet request.
   * @return the AuthSub URL for a user to go to and authorize.
   */
  public String getAuthorizationRedirectUrl(StringBuffer requestUrl) {
    // Extract protocol, port and domain from URL.
    int index = requestUrl.indexOf("/", 9);
    requestUrl.delete(index, requestUrl.length());
    requestUrl.append(AuthorizationServlet.AUTH_HANDLER);

    return AuthSubUtil.getRequestUrl(
        requestUrl.toString(),
        scope,
        false,
        true);
  }

  /**
   * Revokes the AuthSub token for the current user.
   * @param userToken Contains the token to revoke.
   */
  public void revokeToken(UserToken userToken) {
    try {
      AuthSubUtil.revokeToken(userToken.getSessionToken(), null);
    } catch (AuthenticationException e) {
      authServiceError = e.getMessage();
    } catch (GeneralSecurityException e) {
      authServiceError = e.getMessage();
    } catch (IOException e) {
      authServiceError = e.getMessage();
    }
  }

  /**
   * Handles the AuthSub logic of converting the single use token retrieved from the
   * Google Accounts API and exchanging into a long lived session token.
   * @param requestQuery the queryString that has a token parameter with the value of a
   *     single use authSub token.
   * @return the session token or null if no token was retrieved.
   */
  public String getSessionTokenFromString(String requestQuery) {

    // Retrieve single use token from the URL.
    String singleUseToken = AuthSubUtil.getTokenFromReply(requestQuery);

    if (singleUseToken != null) {
      try {
        // Exchange single use token for session token.
        return AuthSubUtil.exchangeForSessionToken(singleUseToken, null);

      } catch (AuthenticationException e) {
        authServiceError = e.getMessage();
      } catch (GeneralSecurityException e) {
        authServiceError = e.getMessage();
      } catch (IOException e) {
        authServiceError = e.getMessage();
      }
    }
    return null;
  }

  /**
   * Returns an error message if any of the AuthSub methods encountered an error.
   * @return the error message.
   */
  public String getAuthServiceError() {
    return authServiceError;
  }

  /**
   * Sets the authSub token into the specified Google Service.
   */
  public void putTokenInGoogleService(UserToken userToken, GoogleService googleService) {
    googleService.setAuthSubToken(userToken.getSessionToken());
  }

  /** 
   * The name of the parameter that will hold the single user AuthSub token returned form Google.
   */
  public String getTokenParam() {
    return TOKEN_PARAM;
  }
}


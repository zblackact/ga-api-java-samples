// Copyright 2009 Google Inc. All Rights Reserved.

package sample.logic;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import sample.data.AuthorizationServiceAuthSubImpl;
import sample.data.AuthorizationServiceOauthImpl;
import sample.data.PMF;
import sample.data.TokenDaoJdoImpl;

/**
 * Factory to create an AuthManager. 
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AuthManagerFactory {

  /**
   * Constructor.
   */
  private AuthManagerFactory() {}

  /**
   * Returns an AuthManager object that implements AuthSub to get authentication tokens.
   * @return an AuthManager object to get authenticated tokens.
   */
  public static AuthManager getAuthSubManager() {
    return new AuthManager(
        UserServiceFactory.getUserService(),
        new AuthorizationServiceAuthSubImpl(
            GoogleDataManager.GOOGLE_DATA_SCOPE),
        new TokenDaoJdoImpl(PMF.getInstance()));
  }

  /**
   * Returns an AuthManager object that implements oAuth to get authentication tokens.
   * @return an AuthManager object to get authenticated tokens.
   */
  public static AuthManager getOauthManager() {
    return new AuthManager(
        UserServiceFactory.getUserService(),
        new AuthorizationServiceOauthImpl(
            GoogleDataManager.GOOGLE_DATA_SCOPE,
            new GoogleOAuthParameters()),
        new TokenDaoJdoImpl(PMF.getInstance()));
  }
}

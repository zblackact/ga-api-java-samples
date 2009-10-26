// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

import sample.controller.AuthorizationServlet;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Handles making OAuth requests to Google to get an authenticated Google Data token.
 * The comments follow the authorization steps from this document
 * http://code.google.com/apis/accounts/docs/OAuth.html
 * The actual Java calls and OAuth helper classes are documented here:
 * http://code.google.com/apis/gdata/oauth.html
 * 
 * Use the Google manage domain tool to register your domain with Google.
 * https://www.google.com/accounts/ManageDomains
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class AuthorizationServiceOauthImpl implements AuthorizationService {

  /** Name of the OAuth token parameter in the URL returned from Google */
  private static final String TOKEN_PARAM = "oauth_token";

  /** oAuth specific information */
  private static final String CONSUMER_KEY = "PUT_YOUR_CONSUMER_KEY_HERE";
  private static final String CONSUMER_SECRET = "PUT_YOUR_CONSUMER_SECRET_HERE";
  
  private GoogleOAuthParameters oAuthParameters;
  private String authServiceError;

  /**
   * Constructor. 
   * @param scope The oAuth Scope to retrieve a token for.
   * @param oAuthParameters holds parameters for working with oAuth.
   */
  public AuthorizationServiceOauthImpl(String scope, GoogleOAuthParameters oAuthParameters) {
    this.oAuthParameters = oAuthParameters;

    oAuthParameters.setOAuthConsumerKey(CONSUMER_KEY);
    oAuthParameters.setScope(scope);
  }

  /**
   * Returns the oAuth URL to redirect a user to so they can grant this application access to
   * their data.
   * @param requestUrl the query parameters from the HTTP servlet request.
   * @return the oAuth URL for a user to go to and authorize.
   */
  public String getAuthorizationRedirectUrl(StringBuffer requestUrl) {
    // Extract protocol, port and domain from URL.
    int index = requestUrl.indexOf("/", 9);
    requestUrl.delete(index, requestUrl.length());
    requestUrl.append(AuthorizationServlet.AUTH_HANDLER);

    oAuthParameters.setOAuthCallback(requestUrl.toString());

    try {
      OAuthRsaSha1Signer signer = new OAuthRsaSha1Signer(getPrivateKey());
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);

      // Step 1. Request token.
      oauthHelper.getUnauthorizedRequestToken(oAuthParameters);

      // Step 3. Return URL user will be redirected to authenticate.
      return oauthHelper.createUserAuthorizationUrl(oAuthParameters);

    } catch (OAuthException e) {
      authServiceError = e.getMessage();
    }
    return "";
  }

  /**
   * Handles the oAuth logic of converting the single use token retrieved from the
   * Google Accounts API and exchanging into a long lived session token.
   * @param requestQuery the queryString that has a token parameter with the value of a
   *     single use authSub token.
   * @return the session token or null if no token was retrieved.
   */
  public String getSessionTokenFromString(String requestQuery) {
    OAuthSigner signer = null;

    try {
      // Step 7. Exchange authorized request token for access token.
      signer = new OAuthRsaSha1Signer(getPrivateKey());
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);
      oauthHelper.getOAuthParametersFromCallback(requestQuery, oAuthParameters);

      // Step 8. Upgrade to access token.
      return oauthHelper.getAccessToken(oAuthParameters);
    } catch (OAuthException e) {
      authServiceError = e.getMessage();
    }
    return null;
  }

  /**
   * Revokes the authorization token for the current user.
   * @param userToken Contains the token to revoke.
   */
  public void revokeToken(UserToken userToken) {
    OAuthSigner signer = null;
    oAuthParameters.setOAuthToken(userToken.getSessionToken());

    try {
      signer = new OAuthRsaSha1Signer(getPrivateKey());
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);
      oauthHelper.revokeToken(oAuthParameters);
    } catch (OAuthException e) {
        authServiceError = e.getMessage();
    }
  }

  public void putTokenInGoogleService(UserToken userToken, GoogleService googleService) {
    OAuthSigner signer = null;
    oAuthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
    oAuthParameters.setOAuthToken(userToken.getSessionToken());

    try {
      signer = new OAuthRsaSha1Signer(getPrivateKey());
      googleService.setOAuthCredentials(oAuthParameters, signer);
    } catch (OAuthException e) {
      authServiceError = e.getMessage();
    }
  }

  /**
   * Returns a private key to be used with the oAuthHelper class.
   * @return the private key to use with the oAuthHelper class.
   */
  private PrivateKey getPrivateKey() {
    KeyFactory fac;
    EncodedKeySpec privKeySpec;
    PrivateKey privateKey = null;
    try {
      fac = KeyFactory.getInstance("RSA");
      privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(getRsaKey()));
      privateKey = fac.generatePrivate(privKeySpec);
    } catch (GeneralSecurityException e) {
      authServiceError = e.getMessage();
    } catch (Base64DecoderException e) {
      authServiceError = e.getMessage();
    }
    return privateKey;
  }

  /**
   * Returns the RSA PKCS#8 format key. See this doc for more information on how to get your
   * own key: http://code.google.com/apis/gdata/oauth.html#GeneratingKeyCert 
   * @return the RSA PKCS#8 format key.
   */
  private static String getRsaKey() {
    StringBuilder key = new StringBuilder();
    // -----BEGIN PRIVATE KEY-----
    key.append("Get your own key at this URL");
    key.append("http://code.google.com/apis/gdata/oauth.html#GeneratingKeyCert");
    // -----END PRIVATE KEY-----
    return key.toString();
  }

  /** 
   * The parameter in the URL that will hold the oAuth token returned form Google.
   * @return the parameter in the URL that will hold the oAuth token.
   */
  public String getTokenParam() {
    return TOKEN_PARAM;
  }

  /**
   * Returns an error message if any of the AuthSub methods encountered an error.
   * @return the error message.
   */
  public String getAuthServiceError() {
    return authServiceError;
  }
}

// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Stores a user id along with the user's session token.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserToken {

  @PrimaryKey
  @Persistent
  /** The user's id. */
  private String userId;

  @Persistent
  /** The user's authenticated session token. */
  private String sessionToken;

  /**
   * Constructor. Assumes token is not valid.
   * @param userId a unique id for the user.
   * @param token a session token to store in the data store.
   */
  public UserToken(String userId, String token) {
    this.userId = userId;
    this.sessionToken = token;
  }

  /**
   * Constructor.
   */
  public UserToken() {}

  /**
   * Sets the user id in the token.
   * @param userId the user id.
   */
  public void setUserId(String userId) {
    this.userId = userId;  
  }

  /**
   * The user id.
   * @return the id of the user.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the session token.
   * @param sessionToken the session token.
   */
  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  /**
   * Returns the session token.
   * @return the session token.
   */
  public String getSessionToken() {
    return sessionToken;
  }

  /**
   * Whether the token is set.
   * @return if the token was set.
   */
  public Boolean hasSessionToken() {
    return (sessionToken == null) ? false : true; 
  }
}


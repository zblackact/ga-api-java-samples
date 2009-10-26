// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

/**
 * Handles all the communication with the data store.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public interface TokenDao {

  /**
   * Stores the session token in the data store.
   * @param sessionToken The token to store.
   */
  public void storeToken(UserToken userToken);
  
  /**
   * Retrieve's the token data from the data store and store in the user token parameter.
   * @param userId from which to retrieve a token for.
   * @return the token from the data store.
   */
  public UserToken retrieveTokenById(String userId);
  
  /**
   * Removes the session token from the data store for the user id in the user token parameter.
   * @param userId unique user to associate the token with.
   */
  public void removeTokenById(String userId);
}

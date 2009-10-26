// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * Encapsulates all the logic to store and retrieve tokens for the current user from
 * the App Engine Data Store.
 * 
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class TokenDaoJdoImpl implements TokenDao {
  PersistenceManagerFactory pmf;

  /**
   * Constructor.
   * Sets the persistence manager.
   * @param pm the persistence manager.
   */
  public TokenDaoJdoImpl(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  /**
   * Returns a persistence manager object from the factory instance. The manager is
   * returned from the method in case we need to do any manager pooling in the future.
   * @return persistence manager object.
   */
  private PersistenceManager getPersistenceManager() {
    return pmf.getPersistenceManager();
  }
  /**
   * Stores the session token in the data store. If the user id already exists in the
   * data store, the App Engine Data Store will overwrite the old session token.
   * @param sessionToken The token to store.
   */
  public void storeToken(UserToken userToken) {
    
    PersistenceManager pm = getPersistenceManager();
    try {
      pm.makePersistent(userToken);
    } finally {
      pm.close();
    }
  }

  /**
   * Retrieve's the token from the data store for the current user and puts it into the
   * userToken passed to this method.
   * @param userId from which to retrieve a token for.
   * @return the token from the data store or {@code null} if no UserToken could be found.
   */
  public UserToken retrieveTokenById(String userId) {
    PersistenceManager pm = getPersistenceManager();
    UserToken userToken = new UserToken();
    try {
      if (userId != null) {
        Key key = KeyFactory.createKey(UserToken.class.getSimpleName(), userId);
        userToken = pm.getObjectById(UserToken.class, key);
      }
    } catch (JDOObjectNotFoundException e) {
      return new UserToken(userId, null);
    }
    return userToken;
  }

  /**
   * Removes the session token from the data store for the specific user id.
   * @param userId unique user to associate the token with.
   */
  public void removeTokenById(String userId) {
    PersistenceManager pm = getPersistenceManager();
    UserToken userToken;
    try {
        if (userId != null) {
          Key key = KeyFactory.createKey(UserToken.class.getSimpleName(), userId);
          userToken = pm.getObjectById(UserToken.class, key);
          pm.deletePersistent(userToken);
        }
      } catch (JDOObjectNotFoundException e) {
        // If nothing is found, then it's OK, the token has already been deleted.
      } finally {
      pm.close();
    }
  }
}


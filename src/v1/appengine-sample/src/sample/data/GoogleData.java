// Copyright 2009 Google Inc. All Rights Reserved.

package sample.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the data from the servlet that will be passed onto the the JSP.
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class GoogleData {

  // Authentication data.
  private String authenticationUrl;
  private Boolean isLoggedIn;

  // Authorization data.
  private String authorizationUrl;
  private String authorizationErrorMessage;
  private Boolean tokenValid;

  // Google Analytics specific data.
  private String tableId;
  private List<String[]> accountList;
  private List<String[]> dataList;
  private String accountListError;
  private String dataListError;

  /**
   * Constructor.
   */
  public GoogleData() {
    accountList = new ArrayList<String[]>();
    dataList = new ArrayList<String[]>();
  }

  /**
   * Sets the URL to allow a user to login/logout of the application.
   * @param url The user a user should click to authenticate.
   */
  public void setAuthenticationUrl(String url) {
    authenticationUrl = url;
  }

  /**
   * Returns the URL a user must goto to login/logout of this application.
   * @return the URL to start the authentication process.
   */
  public String getAuthenticationUrl() {
    return authenticationUrl;
  }

  /**
   * Sets the value if the user has logged in.
   * @param loggedIn if the user is logged in.
   */
  public void setIsLoggedIn(Boolean loggedIn) {
    isLoggedIn = loggedIn;
  }

  /**
   * Whether the user is logged in.
   * @return whether the user is logged in.
   */
  public Boolean isLoggedIn() {
    return isLoggedIn;
  }

  /**
   * Sets the URL a user must goto to grant this application access to their Google Data.
   * @param the URL to start the authorization process.
   */
  public void setAuthorizationUrl(String url) {
    authorizationUrl = url;
  }

  /**
   * Returns the URL a user must goto to grant this application access to their Google Data.
   * @return the URL to start the authorization process.
   */
  public String getAuthorizationUrl() {
    return authorizationUrl;
  }

  /**
   * Sets an error message from the authorization service.
   * @param errorMsg An authorization error message.
   */
  public void setAuthorizationErrorMessage(String errorMsg) {
    authorizationErrorMessage = errorMsg;
  }

  /**
   * Returns an error message from the authorization routine.
   * @return the error message.
   */
  public String getAuthorizationErrorMessage() {
    return authorizationErrorMessage;
  }

  /**
   * Sets the Google Analytics Table Id to retrieve profile data.
   * @param the Table Id of the analytics profile being accessed.
   */
  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  /**
   * Returns the Google Analytics Table Id.
   * @return the Google Analytics Table Id.
   */
  public String getTableId() {
    return tableId;
  }

  public void setTokenValid(Boolean tokenValid) {
    this.tokenValid = tokenValid;
  }
  
  public Boolean isTokenValid() {
    return tokenValid;
  }

  /**
   * Sets a List of strings with account data from the Google Analytics API.
   * @param accountList the list of accounts.
   */
  public void setAccountList(List<String []> accountList) {
    this.accountList = accountList;
  }

  /**
   * Returns a List of strings with the data from the Google Analytics API's Account Feed.
   * @return a list of string arrays with Google Analytics Account Data.
   */
  public List<String []>getAccountList() {
    return accountList;
  }

  /**
   * Sets a List of strings with profile data from the Google Analytics API.
   * @param dataList the list of profile data.
   */
  public void setDataList(List<String []> dataList) {
    this.dataList = dataList;
  }

  /**
   * Returns a List of the data with the data from the Google Analytics API's Data Feed.
   * @return a list of string arrays with Google Analytics Profile Data.
   */
  public List<String []>getDataList() {
    return dataList;
  }

  /**
   * Sets the value of an error generated from retrieving the account list data.
   * @param accountListError the error generated from retrieving the account list data.
   */
  public void setAccountListError(String accountListError) {
    this.accountListError = accountListError;  
  }

  /**
   * Returns the error when retrieving the account list.
   * @return the error when retrieving the account list.
   */
  public String getAccountListError() {
    return accountListError;
  }

  /** 
   * Sets the value of an error generated from retrieving the profile data. 
   * @param dataListError the error generated from retrieving the profile data.
   */
  public void setDataListError(String dataListError) {
    this.dataListError = dataListError;
  }

  /**
   * Returns the error when retrieving the profile data.
   * @return the error when retrieving the profile data.
   */
  public String getDataListError() {
    return dataListError;
  }
}

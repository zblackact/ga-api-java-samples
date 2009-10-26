// Copyright 2009 Google Inc. All Rights Reserved.

package sample.controller;

import sample.data.GoogleData;
import sample.logic.AuthManager;
import sample.logic.AuthManagerFactory;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for handing authentication requests. When a user initiates getting a new token,
 * they are sent to this servlet and then redirected to Google to authorize this application to 
 * access their Google Data. Once a user has authorized, they are redirected back to this servlet
 * to finish the authorization process. This servlet also handles revoking authorization tokens.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
@SuppressWarnings("serial")
public class AuthorizationServlet extends HttpServlet {

  /** Base URL of the Authorization Servlet */
  public static final String AUTH_HANDLER = "/tokenHandler";
  /** URL to revoke a token */
  public static final String REVOKE_TOKEN_HANDLER = "/tokenHandler?action=revoke";
  /** URL to authorize the user */
  public static final String AUTHORIZATION_HANDLER = "/tokenHandler?action=authorize";

  /**
   * Handles GET requests to this servlet. If there is a query parameter with the name "action"
   * and the value "authorize" the user will be redirected to Google to authorize this application
   * to access their Google Data. Once a user has authorized, they are redirected from Google back
   * to this servlet with a query parameter whose value is a one time use token. This servlet then
   * continues the authorization process and exchanges the one time use token for a long lived
   * token. If there is a query parameter with the name "action" and value "revoke" the uer's token
   * will be revoked.
   * @param request the the request object for this servlet.
   * @param response the response object for this servlet.
   * @throws IOException if a network error occurs.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // This example supports both non-signed AuthSub and signed oAuth authorization methods. To
    // switch between both authroization methods simply replace retrieving a AuthManager
    // instance with the getOauthManager method for the getAuthSubManager method. This can be
    // done by uncommenting the code below. There is a similar change that must occur in the
    // MainServlet class.

    AuthManager authManager = AuthManagerFactory.getAuthSubManager();
    //AuthManager authManager = AuthManagerFactory.getOauthManager();
    String nextUrl = "";

    if (request.getParameter(authManager.getTokenParam()) != null) {
      // The user is being redirected back from google.com to exchange the AuthSub one time
      // use token for a session token.
      authManager.storeAuthorizedToken(request.getQueryString());
      nextUrl = MainServlet.MAIN_URL;

    } else if (request.getParameter("action") != null) {

      if (request.getParameter("action").equals("revoke")) {
        // The user is revoking their token.
        authManager.revokeToken();
        nextUrl = MainServlet.MAIN_URL;

      } else if (request.getParameter("action").equals("authorize")) {
        // The user is starting the AuthSub authorization process.
        nextUrl = authManager.getAuthorizationRedirectUrl(request.getRequestURL());
      }
    }

    GoogleData googleData = authManager.getGoogleData();

    if (googleData.getAuthorizationErrorMessage() != null) {
      // Forward users to the authorization page.
      request.setAttribute("googleData", googleData);
      ServletContext sc = getServletContext();
      RequestDispatcher dispatcher = sc.getRequestDispatcher(MainServlet.AUTHORIZATION_VIEW_URL);
      dispatcher.forward(request, response);
    } else {
      // Redirect users to the next page.
      response.sendRedirect(nextUrl);
    }
  }
}


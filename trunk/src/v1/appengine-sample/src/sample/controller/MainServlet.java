// Copyright 2009 Google Inc. All Rights Reserved.

package sample.controller;

import sample.data.GoogleData;
import sample.logic.GoogleDataManager;
import sample.logic.GoogleDataManagerFactory;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows users to retrieve Google Analytics data. This class handles logging into the
 * application, getting an authorization token from the data store and forwarding the
 * data from the Analytics API to a JSP page depending where the user is in the
 * authorization process.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {

  /** URL that handles this servlet. */
  public static final String MAIN_URL = "/main";

  /** URLs of views this servlet redirects to. */
  public static final String LOGIN_VIEW_URL = "/WEB-INF/views/loginView.jsp";
  public static final String AUTHORIZATION_VIEW_URL = "/WEB-INF/views/authorizationView.jsp";
  public static final String RESULTS_VIEW_URL = "/WEB-INF/views/resultsView.jsp";

  static final String APPLICATION_NAME = "Analytics-Java-Demo-v1";

  private String nextUrl;

  /**
   * Handles GET requests for the main application to retrieve data from the Google Analytics API.
   * The user first logins into the application, authorizes access to their Google Analytics
   * account, then get data from the Google Analytics API. The user will be forwarded on
   * to a particular JSP page that corresponding on which step they are in the process of 
   * retrieving data.
   *
   * @param request the the request object for this servlet.
   * @param response the response object for this servlet.
   * @throws IOException if a network error occurs.
   * @throws ServletException if an error with the servlet occurs.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // This example supports both non-signed AuthSub and signed oAuth authorization methods. To
    // switch between both authroization methods simply replace retrieving a GoogleDataManager
    // instance with the getOauthManager method for the getAuthSubManager method. This can be
    // done by uncommenting the code below. There is a similar change that must occur in the
    // AuthorizationServlet class.
    GoogleDataManager googleDataManager = GoogleDataManagerFactory.getAuthSubManager(
        APPLICATION_NAME,
        request.getParameter("ids"));
    /*
    GoogleDataManager googleDataManager = GoogleDataManagerFactory.getOauthManager(
        APPLICATION_NAME,
        request.getParameter("ids"));
    */
    GoogleData googleData = googleDataManager.getGoogleData();

    // First, users must login to use the application.
    nextUrl = LOGIN_VIEW_URL;
    if (googleData.isLoggedIn()) {

      // Second, users must authorize this application to access their Google Analytics account.
      nextUrl = AUTHORIZATION_VIEW_URL;
      if (googleData.isTokenValid()) {

        // Third, users can see results.
        nextUrl = RESULTS_VIEW_URL;
      }
    }

    // Forward the data onto the JSP.
    request.setAttribute("googleData", googleData);
    ServletContext sc = getServletContext();
    RequestDispatcher dispatcher = sc.getRequestDispatcher(nextUrl);
    dispatcher.forward(request, response);
  }
}


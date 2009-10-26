<%-- Copyright 2009 Google Inc. All Rights Reserved. --%>
<%--
  This page is contains the view to show the Google Analytics API results to the end user.
  At this point a user will have logged in and authorized this application to access their
  Google Analytics data. The Analytics data is returned to this JSP through the googleData
  request attribute. The JSP then builds a drop down to select a Google Analytics Account
  and displays the analytics profile data as an HTML table.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="sample.data.GoogleData" %>
<%
  GoogleData googleData = (GoogleData)request.getAttribute("googleData");
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title>Google Analytics API - Bounce Rate Demo</title>
    <link rel="stylesheet" href="css/treemap.css" type="text/css" />
    <link rel="stylesheet" href="css/no-theme/jquery-ui-1.7.2.custom.css"
        type="text/css" />
    <script
        src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js">
    </script>
    <script src="js/ui.core.js"></script>
    <script src="js/ui.slider.js"></script>
    <script src="js/protovis-r3.0.js"></script>
    <script src="js/ga.treemap.js"></script>
  </head>
  <body>
    <h2>Google Analytics API - Bounce Rate Treemap Demo</h2>
    <div class="small-text">
      <a href="<%= googleData.getAuthenticationUrl() %>">Log Out</a>
      <p>You have authorized this application to access your Google Analytics account.
        <a href="<%= googleData.getAuthorizationUrl() %>">Revoke access</a>
      </p>
      <div id="authError">
    <%
      if (googleData.getAuthorizationErrorMessage() != null) {
        out.println(String.format("<p>Error authorizing access to this applications: %s</p>",
            googleData.getAuthorizationErrorMessage()));
      }
    %>
      </div>
    </div>
    <div class="intro small-text">
      <p>Internet users go to websites to meet a need (eg.
        communication, entertainment, information, commerce). If a user enters
        a website on a page and can't find what they are looking for, they will
        exit and go to another website. This user behavior can be measured as a
        one page visit and in Google Analytics is defined as a
        "<strong>bounce</strong>". Bounces are <strong>BAD</strong> because
        they represent a disconnect between a user's intent and the experience
        the site delivers. Optimizing pages by improving the user experience
        will reduce bounces and generally increase the performance of the
        website.
      </p>
      <p>Use the graph below to identify poor performing pages with high
        bounce rates.<br/
        >The data displayed below from the last 14 days of data in the
        selected Google Analytics profile.
      </p>
    </div>
      <div id="treeHelp" class="small-text">
        <div id="controls">
            <div id="accountData">
          <%
            if (googleData.getAccountListError() != null) {
              out.println(googleData.getAccountListError());
            } else {
              // Create a dropdown for users to select a Google Analytics account to view.
          %>
              <form name="accountForm" id="accountForm" method="GET" action="/main">
                <select name="ids">
                <%
                  if (googleData == null || googleData.getAccountList().size() == 0) {
                    out.println("<option>No Accounts Found for this User</option>");
                  } else {
                    for (String[] accountData : googleData.getAccountList()) {
                      out.println(String.format("<option%s value=\"%s\">%s</option>",
                          accountData[0].equals(googleData.getTableId()) ? " SELECTED" : "",
                          accountData[0],
                          accountData[1]));
                    }
                  }
                %>
                </select>
                <input type="submit" value="Get Data"/>
              </form>
          <%
            }
          %>
            </div> <!-- end id accountData -->
          <ul>
            <li class="c-c1">Show Top Level Directories</li>
            <li class="c-radio">
              <input type="radio" name="radioTitles" value="yes" checked>Yes</li>
            <li class="c-radio">
              <input type="radio" name="radioTitles" value="no">No</li>
          </ul>
          <ul>
            <li class="c-c1">Query Params Into Directories</li>
            <li class="c-radio">
              <input type="radio" name="queryDirs" value="yes">Yes</li>
            <li class="c-radio">
              <input type="radio" name="queryDirs" value="no" checked>No</li>
          </ul>
          <ul>
            <li class="c-c1">Filter Out Entrances Above</li>
            <li id="sliderVal">-1</li>
            <li><div id="slider"></div></li>
          </ul>
        </div>
        <ul id="instructions">
          <li><strong>Instructions:</strong></li>
          <li>Each Box Represents a Landing Page</li>
          <li>Size = Traffic to the page (entrances)</li>
          <li>Color = Bounce Rate</li>
            <ul>
              <li>
                <span class='green'>Green</span> is
                <span class='green'>Good</span> - Low Bounce Rate.</li>
              <li>
                <span class='red'>Red</span> is
                <span class='red'>Bad</span> - High Bounce Rate</li>
            </ul>
        </ul>
      </div> <!-- end class treeHelp -->
  <div id="treemap"></div>
  <hr/>
  <div id="dataTable">
    <%
       if (googleData.getDataListError() != null) {
         out.println(googleData.getDataListError());
       }
       else {
         // Create an HTML table of the Google Analytics profile data.
         if (googleData.getDataList().size() == 0) {
           out.println("No Data Found");
         } else {
           Boolean first = true;
           StringBuffer table = new StringBuffer("<table>");
           for (String[] profileData : googleData.getDataList()) {
             table.append("<tr>");
             for (int i = 0; i < profileData.length; i++) {
               if (first) {
                 table.append(String.format("<th>%s</th>", profileData[i]));
               } else {
                 table.append(String.format("<td>%s</td>", profileData[i]));
               }
             }
             table.append("</tr>\n");
             first = false;
           }
           table.append("</table>");
           out.println(table.toString());
         }
       }
    %>
  </div>
  <div id="tooltip">
    <div id="tt-title"></div>
    <ul>
      <li>
        <div class="tt-c1">Entrances </div>
        <div class="tt-c2" id="tt-e"></div>
      </li>
      <li>
        <div class="tt-c1">Bounces </div>
       <div class="tt-c2" id="tt-b"></div>
      </li>
      <li>
        <div class="tt-c1">Bounce Rate </div>
        <div class="tt-c2" id="tt-br"></div>
      </li>
    </ul>
  </div>
  <script>
    $(document).ready(function() {
      ga.treemap.init();
    });
  </script>
</body>
</html>


<%-- Copyright 2009 Google Inc. All Rights Reserved. --%>
<%--
  This page contains the view shown after users have logged in but need to get a valid
  authorization token.
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
</head>
<body>
  <h2>Google Analytics API - Bounce Rate Demo</h2>
  <a href=" <%= googleData.getAuthenticationUrl() %>">Log Out</a>

  <p class="small-text">This application requires access to your Google
    Analytics account. When you click the link below,<br/>
    you will be taken to a page hosted on Google to grant this application
    access.
  </p>

  <a href="<%= googleData.getAuthorizationUrl() %>">Authorize with Google Analytics</a>
<%
  if (googleData.getAuthorizationErrorMessage() != null) {
    out.println(String.format("<p>Error authorizing access to this applications: %s</p>",
        googleData.getAuthorizationErrorMessage()));
  }
%>
</body>
</html>


<%-- Copyright 2009 Google Inc. All Rights Reserved. --%>
<%--
  This page is contains the view when a user hasn't logged into the application.
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
  <h2>Google Analytics API - Bounce Rate Treemap Demo</h2>
  <p class="small-text">This is a demo that shows you how to retrieve Google Analytics Data on App Engine.
    To use this application you must go through 3 steps:
    <ol class="small-text">
      <li>Login</li>
      <li>Authorize this application to access your Google Analytics Data</li>
      <li>View the results</li>
    </ol>
  </p>
  <a href="<%= googleData.getAuthenticationUrl() %>">Log In</a>
<%
  if (googleData.getAuthorizationErrorMessage() != null) {
    out.println(String.format("<p>Error authorizing access to this applications: %s</p>",
        googleData.getAuthorizationErrorMessage()));
  }
%>
</body>
</html>


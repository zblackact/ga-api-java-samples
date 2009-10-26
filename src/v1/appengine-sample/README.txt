This doc explains how to build the Google Analytics Treemap example using the
App Java SDK, the Eclipse IDE, the Google Plugin for Eclipse and Java 6.
The Google Plugin adds a number of new files and libraries when you create a
new application. So the easiest way to get started is to first create a new
Google Web Application, then add all the example specific code to that
application.


Building The Example
========
1. Before you you begin, you will need to install the Google Plugin for
Eclipse.
Home page: http://code.google.com/eclipse/
Quick Start guide: http://code.google.com/eclipse/docs/getting_started.html

2. You will need to download the latest version of the Google Data API
Java Client Library.
Project Page: http://code.google.com/p/gdata-java-client/
Download Page: http://code.google.com/p/gdata-java-client/downloads/list

3. Start Eclipse if you haven't done so already.

4. Create a new Google Web Application project.

5. Copy the following JARs from the Google Data API Java Client Library into
the /war/WEB_INF/lib directory
/lib/gdata-analytics-2.0.jar
/lib/gdata-analytics-meta-2.0.jar
/lib/gdata-core.1.0.jar
/deps/google-collect-1.0-rc1.jar
/deps/jsr305.jar

6. Add these JARs to your Project's Build Path by going to:
Project -> Properties -> Java Build Path -> Libraries tab -> Add JARs...

7. Copy the directory with the Java files from the example
/src/sample directory into your new application's /src directory.

8. Copy the directory with the JSP files from the example
/war/WEB-INF/views directory into your application's /war/WEB-INF/ directory.

9. Copy both the /war/js and /war/css directories from this example into
the /war directory of your application.

10. Replace your application's /war/WEB-INF/web.xml with the example
/war/WEB-INF/web.xml file

11. in your application's /war/WEB-INF/appengine-web.xml file add the follow
property inside of the existing <system-preperties> tag.
<property name="com.google.gdata.DisableCookieHandler" value="true"/>

12. Everything is setup. Before launching to App Engine, click the debug
button to run the development server on your local machine. Go through
the authorization process to save a token to the data store. On the
development server App Engine will automatically create a new index file
for the Data Store: http://code.google.com/appengine/docs/java/datastore/overview.html#Queries_and_Indexes

13. You're done! Upload to App Engine.


Switching Authorization Methods
========
This example supports two authorization methods. Unregisted AuthSub and OAuth.

Using unregistered AuthSub requires no extra work.
Using OAuth requires you to register your domain and get a private key.
Details here: http://code.google.com/apis/gdata/oauth.html#GeneratingKeyCert

To switch between Authorization mechanisms. You must uncomment/comment the
section of code that retrieves the manager objects in both the MainServlet
and AuthoriazationServlets.

To support OAuth you will also need to modify the AuthorizationServiceOauthImpl.java
  CONSUMER_KEY
  CONSUMER_SECRET
constants with your own values.

Additionally you will also need to update the getRsaKey method by entering your
own RSA key in PKCS#8 format.

Here is how you can generate your own RSA key: http://code.google.com/apis/gdata/oauth.html#GeneratingKeyCert
Which you can use to register your domain with Google here: https://www.google.com/accounts/ManageDomains



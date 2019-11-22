# rezolve_sdk_sampleapp_android
Sample app to show case the minimum implementation of SDK to get a developer up and running

#### Configuration
Paste following code into ***~/.gradle/gradle.properties*** and fill with your credentials data.
```
NEXUS_REZOLVE_REPO_URL_PUBLIC=https://nexus.rezo.lv/repository/maven-sdk-releases/
NEXUS_REZOLVE_READ_USERNAME=<proper username here>
NEXUS_REZOLVE_READ_PASSWORD=<proper password here>
REZOLVE_SDK_API_URL="https://sandbox-api-tw.rzlvtest.co/"
REZOLVE_SDK_ENVIRONMENT="https://sandbox-api-tw.rzlvtest.co/api"
REZOLVE_SDK_API_KEY="<proper API key here>"
REZOLVE_SDK_JWT_SECRET="<proper JWT secrtet here>"
DEMO_AUTH_SERVER="proper RUA server url"
DEMO_AUTH_USER="proper RUA username"
DEMO_AUTH_PASSWORD="proper RUA password"
```
This sample code comes configured to use a Rezolve-hosted authentication server, referred to by Rezolve as a RUA server (Rezolve User Authentication).
You **SHOULD NOT** use this server for production apps, it is for testing and Sandbox use only.

This sample auth configuration is provided so that:
1) you may compile and test the sample code immediately upon receipt, without having to configure your own auth server, and
2) so that the partner developer may see an example of how the SDK will utilize an external auth server to obtain permission to talk with the Rezolve APIs.
If you have an existing app with an existing authenticating user base, you will want to utilize YOUR auth server to issue JWT tokens, which the Rezolve API will accept.
Details on this process are available here: http://docs.rezolve.com/docs/#jwt-authentication
If you do not have an existing app, or do not have an existing app server, you have the option to either implement your own auth server and use JWT authentication as described above, or to have Rezolve install a RUA server for you (the same type auth server this sample code is configured to use).

Please discuss authentication options with your project lead and/or your Rezolve representative.
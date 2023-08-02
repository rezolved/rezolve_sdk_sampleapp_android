# rezolve_sdk_sampleapp_android
Sample app to show case the minimum implementation of SDK to get a developer up and running

#### Configuration
Paste following code into ***~/.gradle/gradle.properties*** and fill with your credentials data.
```
NEXUS_REZOLVE_REPO_URL_PUBLIC=https://nexus.rezolve.com/repository/maven-sdk-releases/
NEXUS_REZOLVE_READ_USERNAME=<proper username here>
NEXUS_REZOLVE_READ_PASSWORD=<proper password here>
REZOLVE_SDK_API_URL="https://core.sbx.eu.rezolve.com/"
REZOLVE_SDK_ENVIRONMENT="https://core.sbx.eu.rezolve.com/api"
REZOLVE_SDK_API_KEY="<proper API key here>"
REZOLVE_SDK_JWT_SECRET="<proper JWT secret here>"
DEMO_AUTH_SERVER="<proper RUA server url>"
DEMO_AUTH_USER="<proper RUA username>"
DEMO_AUTH_PASSWORD="<proper RUA password>"
AUTH0_CLIENT_ID="<proper Auth0 client ID>"
AUTH0_CLIENT_SECRET="<proper Auth0 client secret>"
AUTH0_API_KEY="<proper Auth0 api key>"
AUTH0_AUDIENCE="<proper Auth0 audienc>"
AUTH0_ENDPOINT="<proper Auth0 endpoint>"
SSP_ENDPOINT="<proper SSP endpoint>"
SSP_ENGAGEMENT_ENDPOINT="<proper SSP engagement endpoint>"
SSP_ACT_ENDPOINT="<proper SSP act endpoint>"
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
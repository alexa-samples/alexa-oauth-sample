# Alexa OAuth Sample
This is sample code for Alexa Skill developers to set up an OAuth 2.0 server. 
An OAuth 2.0 server is required for Skill account linking, which lets you connect user identities across different account systems.
Reciprocal authorization and client token management are also included to support mutual account linking for calling Alexa APIs outside of Alexa Skills.

# Description
## Supported Grant Types
With the code sample and AWS Cloud Formation template, a standard OAuth 2.0 service is provided that supports the following grant types:
* [Authorization Code](https://oauth.net/2/grant-types/authorization-code/)
* [Implicit](https://oauth.net/2/grant-types/implicit/)
* [Password](https://oauth.net/2/grant-types/password/)
* [Client Credentials](https://oauth.net/2/grant-types/client-credentials/)
* [Refresh Token](https://oauth.net/2/grant-types/refresh-token/)

and an Alexa specific grant type (for reciprocal account linking):
* [Alexa Reciprocal Authorization Code](https://developer.amazon.com/docs/account-linking/account-linking-for-health.html#get-your-tokens) *(Only required for mutual account linking)*

## Endpoints
* **/oauth/authorize**: The authorization endpoint is the endpoint on the authorization server where the resource owner logs in, and grants authorization to the client application.
* **/oauth/token**: The token endpoint is the endpoint on the authorization server where the client application exchanges the authorization code, client ID and client secret, for an access token.
* **/api/reciprocal/authorize**: The reciprocal authorization endpoint will be invoked by Alexa to send a LWA auth code. *(Only required for mutual account linking)*

## OAuth Management Portal
* **/login**: The portal used for administration of OAuth Clients and Partners, as well as users to manage their approvals to other clients.

## Integrate with your authentication server
For testing purpose, a sample user **(user/password)** and a sample administrator **(admin/password)** are created for testing.

To integrate with your own authentication server or user DB, update the TODO task in ```AuthenticationServiceProvider``` class.

```java
public class AuthenticationServiceProvider {
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        //TODO: Integrate with your authentication system to return UserDetails.
    }
}
```

# Server Setup
To setup your own OAuth server,
1. Clone the repository from [alexa-oauth-sample](https://github.com/alexa/alexa-oauth-sample). Optionally, you can modify the repository name
2. ![CreateStack](images/cloudformation-launch-stack.png) with this [template](https://github.com/alexa/alexa-oauth-sample/blob/master/template.json)
3. Add your Domain Certificate and bind it to your load balancer (LB port: 443, Instance port: 80). *(HTTPS is required for Alexa Skill Account Linking)*

![CreateStack](images/Infrastructure.png)

# Integrate with Alexa Skill Account Linking
* [Understand Skill Account Linking](https://developer.amazon.com/docs/account-linking/understand-account-linking.html)

#### Create an OAuth client for Alexa
1. Login to the [Alexa Skill console](https://developer.amazon.com/alexa/console/ask) and select your skill
2. In the **"PERMISSIONS"** section, write down your Client Id and Client Secret
![Permissions](images/Permissions.png)
3. In the **"ACCOUNT LINKING"** section, write down your Redirect URLs
![RedirectURLs](images/RedirectURLs.png)
4. Login to [https://YOUR_DOMAIN/login]() as an administrator. Create an OAuth client that you vend to Alexa (e.g. alexa_skill_client)
![AddClient](images/AddClient.png)
4. (Only for mutual account linking) Login to [https://YOUR_DOMAIN/login]() as an administrator, create an OAuth partner to call Alexa APIs
**IMPORTANT**: clientId and clientSecret originate from your Alexa Skill (Under the PERMISSION tab). The partnerId should be the same as the "clientId" in previous step.
![AddPartner](images/AddPartner.png)

#### Steps
1. Login to [Alexa Skill console](https://developer.amazon.com/alexa/console/ask) and select your skill
2. Click **"ACCOUNT LINKING"** section, fill the form with the following:
![AccountLinking](images/AccountLinking.png)
   * Authorization URI: [https://YOUR_DOMAIN/oauth/authorize]()
   * Access Token URI:  [https://YOUR_DOMAIN/oauth/token]()
   * Client ID: the clientId of the client you have created in the previous section
   * Client Secret: the clientSecret of the client you have created in the previous section
   * Client Authentication Scheme: Choose credentials in request body
   * Scope: OAuth scopes to define the permissions. (This is used for your resource server, leave empty if you do not have one)
   * Domain List: YOUR DOMAIN (e.g. domain.com, www.domain.com)



# License
This library is licensed under the [Amazon Software License 1.0](LICENSE).
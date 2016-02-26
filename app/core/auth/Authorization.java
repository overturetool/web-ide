package core.auth;

public class Authorization {
//    /** Directory to store user credentials. */
//    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/dailymotion_sample");
//
//    /**
//     * Global instance of the {@link //DataStoreFactory}. The best practice is to make it a single
//     * globally shared instance across your application.
//     */
//    private static FileDataStoreFactory DATA_STORE_FACTORY;
//
//    /** OAuth 2 scope. */
//    private static final String SCOPE = "read";
//
//    /** Global instance of the HTTP transport. */
//    private static final HttpTransport transport = new NetHttpTransport();
//
//    /** Global instance of the JSON factory. */
//    static final JsonFactory jsonFactory = new JacksonFactory();
//
//    private static final String TokenServerUrl = "https://github.com/login/oauth/access_token";
//    private static final String AuthorizationServerUrl = "https://github.com/login/oauth/authorize";
//
//    private AuthorizationCodeFlow flow;
//    private final String callbackURI = "http://localhost:9000/oauth2callback";
//
//    public Authorization() throws IOException {
////        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
////        final Credential credential = authorize();
////        HttpRequestFactory requestFactory =
////                transport.createRequestFactory(new HttpRequestInitializer() {
////                    @Override
////                    public void initialize(HttpRequest request) throws IOException {
////                        credential.initialize(request);
////                        request.setParser(new JsonObjectParser(jsonFactory));
////                    }
////                });
//    }
//
//    public AuthorizationCodeFlow authorize() throws IOException {
//        Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
//        GenericUrl tokenServerUrl = new GenericUrl(TokenServerUrl);
//        ClientParametersAuthentication clientAuthentication = new ClientParametersAuthentication(OAuth2ClientCredentials.ClientID, OAuth2ClientCredentials.API_SECRET);
//
//        flow = new AuthorizationCodeFlow.Builder(
//                accessMethod,
//                transport,
//                jsonFactory,
//                tokenServerUrl,
//                clientAuthentication,
//                OAuth2ClientCredentials.ClientID,
//                AuthorizationServerUrl).build();
////                    .setScopes(Arrays.asList(SCOPE))
////                    .setDataStoreFactory(DATA_STORE_FACTORY).build();
//        return flow;
//    }
//
//    public String getUserInfoJson(final String authCode) throws IOException {
////        TokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(callbackURI).execute();
////        final Credential credential = flow.createAndStoreCredential(response, null);
////        final HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
////
////        // Make an authenticated request
////        final GenericUrl url = new GenericUrl(USER_INFO_URL);
////        final HttpRequest request = requestFactory.buildGetRequest(url);
////        request.getHeaders().setContentType("application/json");
////        return request.execute().parseAsString();
//        return null;
//    }
}

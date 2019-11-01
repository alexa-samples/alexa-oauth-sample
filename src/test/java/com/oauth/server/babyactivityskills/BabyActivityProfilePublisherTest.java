package com.oauth.server.babyactivityskills;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.oauth.server.authentication.PartnerTokenManager;
import com.oauth.server.babyactivityskills.model.Capability;
import com.oauth.server.babyactivityskills.model.Name;
import com.oauth.server.babyactivityskills.model.Profile;
import com.oauth.server.babyactivityskills.model.ProfileReport;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BabyActivityProfilePublisherTest {
    private static final String BEARER_ACCESS_TOKEN = "test-token";
    private static final String LWA_ACCESS_TOKEN = "lwa-access-token";
    private static final String USER_ID = "user";
    private static final String PARTNER_ID = "test-partner";
    private static final Set<Profile> BABY_PROFILES = ImmutableSet.of(
            new Profile("user/baby-1",
                    new Name("Maggie", "Simpson", ImmutableSet.of("Maggie", "Mag")),
                    ImmutableSet.of(Capability.WEIGHT, Capability.DIAPER_CHANGE, Capability.INFANT_FEEDING, Capability.SLEEP)));

    @Mock
    private TokenStore mockTokenStore;

    @Mock
    private PartnerTokenManager mockTokenManager;

    @Mock
    private HttpClientFactory mockHttpClientFactory;

    @Mock
    private CloseableHttpClient mockHttpClient;

    private BabyActivityProfilePublisher babyActivityProfilePublisher;

    @Before
    public void setup() {
        when(mockHttpClientFactory.createDefault()).thenReturn(mockHttpClient);
        babyActivityProfilePublisher = new BabyActivityProfilePublisher(mockTokenStore,
                mockTokenManager, mockHttpClientFactory, mock(ExecutorService.class));
    }

    @Test
    public void testPublishProfilesGivenValidClientTokenShouldPostProfiles() throws IOException {
        final OAuth2Authentication mockUserForBearerToken = mock(OAuth2Authentication.class);
        when(mockUserForBearerToken.getName()).thenReturn(USER_ID);
        when(mockTokenStore.readAuthentication(BEARER_ACCESS_TOKEN)).thenReturn(mockUserForBearerToken);

        final OAuth2AccessToken mockLwaAccessToken = mock(OAuth2AccessToken.class);
        when(mockLwaAccessToken.getValue()).thenReturn(LWA_ACCESS_TOKEN);
        when(mockTokenManager.getAccessToken(USER_ID, PARTNER_ID)).thenReturn(mockLwaAccessToken);

        final CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);
        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockHttpResponse);

        babyActivityProfilePublisher.publishProfiles(BEARER_ACCESS_TOKEN, PARTNER_ID);

        final ArgumentCaptor<HttpPost> postRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(mockHttpClient).execute(postRequestCaptor.capture());

        final HttpPost actualPostRequest = postRequestCaptor.getValue();
        final ProfileReport actualProfileReport = getProfileReport(actualPostRequest);
        assertThat(actualProfileReport.getProfiles()).hasSameElementsAs(BABY_PROFILES);
    }

    private ProfileReport getProfileReport(final HttpPost postRequest) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, ProfileReport.class);
        final Map<String, ProfileReport> profileReport = mapper.reader().forType(type).readValue(postRequest.getEntity().getContent());
        return profileReport.get("report");
    }
}

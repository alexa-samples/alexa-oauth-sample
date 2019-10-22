package com.oauth.server.babyactivityskills;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Wrapper around HttpClients to enable mocking of
 * http client in unit tests.
 */
public class HttpClientFactory {

    public CloseableHttpClient createDefault() {
        return HttpClients.createDefault();
    }
}

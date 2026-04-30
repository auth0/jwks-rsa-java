package com.auth0.jwk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default {@link JwksHttpClient} implementation using {@link java.net.URLConnection}.
 *
 * <p>This preserves the exact HTTP behavior the library had before the pluggable
 * client interface was introduced. It is used automatically when no custom
 * {@link JwksHttpClient} is provided to the builder.</p>
 */
final class DefaultJwksHttpClient implements JwksHttpClient {

    private final Integer connectTimeout;
    private final Integer readTimeout;
    private final Proxy proxy;
    private final Map<String, String> headers;

    /**
     * Creates a default HTTP client with the given configuration.
     *
     * @param connectTimeout connection timeout in milliseconds (null for system default)
     * @param readTimeout    read timeout in milliseconds (null for system default)
     * @param proxy          proxy server to use (null for direct connection)
     * @param headers        request headers to send (null defaults to Accept: application/json)
     */
    DefaultJwksHttpClient(Integer connectTimeout, Integer readTimeout,
                          Proxy proxy, Map<String, String> headers) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.proxy = proxy;
        this.headers = (headers != null) ? headers :
                Collections.singletonMap("Accept", "application/json");
    }

    @Override
    public JwksHttpResponse fetch(URL url) throws IOException {
        final URLConnection c = (proxy == null) ? url.openConnection() : url.openConnection(proxy);

        if (connectTimeout != null) {
            c.setConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            c.setReadTimeout(readTimeout);
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            c.setRequestProperty(entry.getKey(), entry.getValue());
        }

        String body;
        try (InputStream in = c.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString();
        }

        Map<String, List<String>> responseHeaders = c.getHeaderFields();
        return new JwksHttpResponse(body, responseHeaders);
    }
}

package com.auth0.jwk;

import java.io.IOException;
import java.net.URL;

/**
 * Abstraction for fetching JWKS JSON over HTTP.
 *
 * <p>Implement this interface to control how the library makes HTTP requests.
 * This allows customization of TLS settings, proxy authentication, HTTP version,
 * and any other transport concern.</p>
 *
 * <p>This is a functional interface, so a lambda can be used:</p>
 * <pre>{@code
 * JwksHttpClient client = url -> {
 *     // Use any HTTP library (OkHttp, Apache HC, Java 11 HttpClient, etc.)
 *     Request req = new Request.Builder().url(url).build();
 *     try (Response resp = okHttp.newCall(req).execute()) {
 *         return new JwksHttpResponse(resp.body().string(), resp.headers().toMultimap());
 *     }
 * };
 *
 * JwkProvider provider = new JwkProviderBuilder(domain)
 *     .httpClient(client)
 *     .build();
 * }</pre>
 *
 * <p>If no custom client is provided, the library uses a default implementation
 * based on {@link java.net.URLConnection}.</p>
 */
@FunctionalInterface
public interface JwksHttpClient {

    /**
     * Fetch the JWKS JSON from the given URL.
     *
     * <p>Implementations should:</p>
     * <ul>
     *   <li>Make an HTTP GET request to the URL</li>
     *   <li>Return the response body and headers wrapped in a {@link JwksHttpResponse}</li>
     *   <li>Throw {@link IOException} on any network or protocol error</li>
     * </ul>
     *
     * @param url the JWKS endpoint URL
     * @return the HTTP response containing the body and headers
     * @throws IOException on any network or protocol error
     */
    JwksHttpResponse fetch(URL url) throws IOException;
}

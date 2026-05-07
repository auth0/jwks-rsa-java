package com.auth0.jwk;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the HTTP response from a JWKS endpoint.
 * Contains both the JSON body and the response headers.
 */
public final class JwksHttpResponse {

    private final String body;
    private final Map<String, List<String>> headers;

    /**
     * Creates a new response with body and headers.
     *
     * @param body    the response body (JWKS JSON)
     * @param headers the response headers (e.g., Cache-Control)
     */
    public JwksHttpResponse(String body, Map<String, List<String>> headers) {
        this.body = body;
        this.headers = (headers != null) ? headers : Collections.<String, List<String>>emptyMap();
    }

    /**
     * Creates a new response with body only (no headers).
     *
     * @param body the response body (JWKS JSON)
     */
    public JwksHttpResponse(String body) {
        this(body, Collections.<String, List<String>>emptyMap());
    }

    /**
     * Returns the response body as a string (the JWKS JSON).
     *
     * @return the response body
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns all response headers.
     *
     * @return an unmodifiable map of header names to their values
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Returns the first value of a response header (case-insensitive lookup).
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * String cacheControl = response.getHeaderValue("Cache-Control");
     * }</pre>
     *
     * @param name the header name (case-insensitive)
     * @return the first header value, or null if not present
     */
    public String getHeaderValue(String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                List<String> values = entry.getValue();
                return (values != null && !values.isEmpty()) ? values.get(0) : null;
            }
        }
        return null;
    }
}

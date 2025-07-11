package com.auth0.jwk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Jwk provider that loads them from a {@link URL}
 */
@SuppressWarnings("WeakerAccess")
public class UrlJwkProvider implements JwkProvider {

    @VisibleForTesting
    static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

    private final AtomicReference<List<Jwk>> cachedJwks = new AtomicReference<>();

    final URL url;
    final Proxy proxy;
    final Map<String, String> headers;
    final Integer connectTimeout;
    final Integer readTimeout;

    private final ObjectReader reader;

    /**
     * Creates a provider that loads from the given URL
     *
     * @param url to load the jwks
     */
    public UrlJwkProvider(URL url) {
        this(url, null, null, null, null);
    }

    /**
     * Creates a provider that loads from the given URL using a specified proxy server
     *
     * @param url            to load the jwks
     * @param connectTimeout connection timeout in milliseconds (null for default)
     * @param readTimeout    read timeout in milliseconds (null for default)
     * @param proxy          proxy server to use when making the connection
     */
    public UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout, Proxy proxy) {
        this(url, connectTimeout, readTimeout, proxy, null);
    }

    /**
     * Creates a provider that loads from the given URL using custom request headers.
     *
     * @param url            to load the jwks
     * @param connectTimeout connection timeout in milliseconds (default is null)
     * @param readTimeout    read timeout in milliseconds (default is null)
     * @param proxy          proxy server to use when making the connection (default is null)
     * @param headers        a map of request header keys to values to send on the request. Default is "Accept: application/json".
     */
    public UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout, Proxy proxy, Map<String, String> headers) {
        Util.checkArgument(url != null, "A non-null url is required");
        Util.checkArgument(connectTimeout == null || connectTimeout >= 0, "Invalid connect timeout value '" + connectTimeout + "'. Must be a non-negative integer.");
        Util.checkArgument(readTimeout == null || readTimeout >= 0, "Invalid read timeout value '" + readTimeout + "'. Must be a non-negative integer.");

        this.url = url;
        this.proxy = proxy;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.reader = new ObjectMapper().readerFor(Map.class);

        this.headers = (headers == null) ?
                Collections.singletonMap("Accept", "application/json") : headers;
    }

    /**
     * Creates a provider that loads from the given URL
     *
     * @param url            to load the jwks
     * @param connectTimeout connection timeout in milliseconds (null for default)
     * @param readTimeout    read timeout in milliseconds (null for default)
     */
    public UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout) {
        this(url, connectTimeout, readTimeout, null, null);
    }

    /**
     * Creates a provider that loads from the given domain's well-known directory.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * If the domain url contains a path, e.g. 'https://auth.example.com/some-resource', the path is preserved and the
     * default jwks path is appended.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * If the domain string is "https://auth.example.com/some-resource", the jwks url that will be used is
     * "https://auth.example.com/some-resource/.well-known/jwks.json"
     * <br><br> Use {@link #UrlJwkProvider(URL)} if you need to pass a full URL.
     *
     * @param domain where jwks is published
     */
    public UrlJwkProvider(String domain) {
        this(urlForDomain(domain));
    }

    @VisibleForTesting
    void setCachedJwks(List<Jwk> jwks) {
        this.cachedJwks.set(jwks);
    }

    static URL urlForDomain(String domain) {
        Util.checkArgument(!Util.isNullOrEmpty(domain), "A domain is required");

        if (!domain.startsWith("http")) {
            domain = "https://" + domain;
        }

        try {
            final URI uri = new URI(domain + WELL_KNOWN_JWKS_PATH).normalize();
            return uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
    }

    private Map<String, Object> getJwks() throws SigningKeyNotFoundException {
        try {
            final URLConnection c = (proxy == null) ? this.url.openConnection() : this.url.openConnection(proxy);
            if (connectTimeout != null) {
                c.setConnectTimeout(connectTimeout);
            }
            if (readTimeout != null) {
                c.setReadTimeout(readTimeout);
            }

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }

            try (InputStream inputStream = c.getInputStream()) {
                return reader.readValue(inputStream);
            }
        } catch (IOException e) {
            throw new NetworkException("Cannot obtain jwks from url " + url.toString(), e);
        }
    }

    public List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = new ArrayList<>();
        @SuppressWarnings("unchecked") final List<Map<String, Object>> keys = (List<Map<String, Object>>) getJwks().get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new SigningKeyNotFoundException("No keys found in " + url.toString(), null);
        }

        try {
            for (Map<String, Object> values : keys) {
                jwks.add(Jwk.fromValues(values));
            }
        } catch (IllegalArgumentException e) {
            throw new SigningKeyNotFoundException("Failed to parse jwk from json", e);
        }
        return jwks;
    }

    public List<Jwk> getCachedJwks() throws JwkException {
        List<Jwk> jwks = cachedJwks.get();
        if (jwks == null) {
            synchronized (this) {
                jwks = getAll();
                cachedJwks.set(jwks);
            }
        }
        return jwks;
    }

    @Override
    public Jwk get(String keyId) throws JwkException {
        List<Jwk> jwks = getCachedJwks();
        if (keyId == null && jwks.size() == 1) {
            return jwks.get(0);
        }
        if (keyId != null) {
            for (Jwk jwk : jwks) {
                if (keyId.equals(jwk.getId())) {
                    return jwk;
                }
            }
        }

        // Key not found — refreshing JWKS from remote
        synchronized (this) {
            List<Jwk> freshJwks = getAll();
            cachedJwks.set(freshJwks);

            // Retry lookup in freshly fetched JWKS
            if (keyId == null && freshJwks.size() == 1) {
                return freshJwks.get(0);
            }

            if (keyId != null) {
                for (Jwk jwk : freshJwks) {
                    if (keyId.equals(jwk.getId())) {
                        return jwk;
                    }
                }
            }
        }


        throw new SigningKeyNotFoundException("No key found in " + url.toString() + " with kid " + keyId, null);
    }
}

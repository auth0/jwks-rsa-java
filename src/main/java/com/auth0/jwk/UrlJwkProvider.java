package com.auth0.jwk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Jwk provider that loads them from a {@link URL}
 */
@SuppressWarnings("WeakerAccess")
public class UrlJwkProvider implements JwkProvider {

    @VisibleForTesting
    static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

    final URL url;
    private final Integer connectTimeout;
    private final Integer readTimeout;

    private final ObjectReader reader;
    /**
     * Creates a provider that loads from the given URL
     *
     * @param url to load the jwks
     */
    public UrlJwkProvider(URL url) {
        this(url, null, null);
    }

    /**
     * Creates a provider that loads from the given URL
     *
     * @param url            to load the jwks
     * @param connectTimeout connection timeout in milliseconds (null for default)
     * @param readTimeout    read timeout in milliseconds (null for default)
     */
    public UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout) {
        checkArgument(url != null, "A non-null url is required");
        checkArgument(connectTimeout == null || connectTimeout >= 0, "Invalid connect timeout value '" + connectTimeout + "'. Must be a non-negative integer.");
        checkArgument(readTimeout == null || readTimeout >= 0, "Invalid read timeout value '" + readTimeout + "'. Must be a non-negative integer.");

        this.url = url;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        
        this.reader = new ObjectMapper().readerFor(Map.class);
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

    static URL urlForDomain(String domain) {
        checkArgument(!isNullOrEmpty(domain), "A domain is required");

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
            final URLConnection c = this.url.openConnection();
            if (connectTimeout != null) {
                c.setConnectTimeout(connectTimeout);
            }
            if (readTimeout != null) {
                c.setReadTimeout(readTimeout);
            }
            c.setRequestProperty("Accept", "application/json");
            
            try (InputStream inputStream = c.getInputStream()) {
                return reader.readValue(inputStream);
            }
        } catch (IOException e) {
            throw new NetworkException("Cannot obtain jwks from url " + url.toString(), e);
        }
    }

    public List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = Lists.newArrayList();
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

    @Override
    public Jwk get(String keyId) throws JwkException {
        final List<Jwk> jwks = getAll();
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
        throw new SigningKeyNotFoundException("No key found in " + url.toString() + " with kid " + keyId, null);
    }
}

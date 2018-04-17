package com.auth0.jwk;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Jwk provider that loads them from a {@link URL}
 */
@SuppressWarnings("WeakerAccess")
public class UrlJwkProvider implements JwkProvider {

    static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

    final URL url;

    /**
     * Creates a provider that loads from the given URL
     * @param url to load the jwks
     */
    public UrlJwkProvider(URL url) {
        checkArgument(url != null, "A non-null url is required");
        this.url = url;
    }

    /**
     * Creates a provider that loads from the given domain's well-known directory.
     * <br>If the protocol (http or https) is not provided then https is used by default
     * (some.domain -> "https://" + some.domain + {@value #WELL_KNOWN_JWKS_PATH}).
     * <br><br> Use {@link #UrlJwkProvider(URL)} if you need to pass a full URL.
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
            final URL url = new URL(domain);
            return new URL(url, WELL_KNOWN_JWKS_PATH);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
    }

    private Map<String, Object> getJwks() throws SigningKeyNotFoundException {
        try {
            final InputStream inputStream = this.url.openStream();
            final JsonFactory factory = new JsonFactory();
            final JsonParser parser = factory.createParser(inputStream);
            final TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
            return new ObjectMapper().reader().readValue(parser, typeReference);
        } catch (IOException e) {
            throw new SigningKeyNotFoundException("Cannot obtain jwks from url " + url.toString(), e);
        }
    }

    private List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> keys = (List<Map<String, Object>>) getJwks().get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new SigningKeyNotFoundException("No keys found in " + url.toString(), null);
        }

        try {
            for (Map<String, Object> values: keys) {
                jwks.add(Jwk.fromValues(values));
            }
        } catch(IllegalArgumentException e) {
            throw new SigningKeyNotFoundException("Failed to parse jwk from json", e);
        }
        return jwks;
    }

    @Override
    public Jwk get(String keyId) throws JwkException {
        final List<Jwk> jwks = getAll();
        for (Jwk jwk: jwks) {
            if (keyId.equals(jwk.getId())) {
                return jwk;
            }
        }
        throw new SigningKeyNotFoundException("No key found in " + url.toString() + " with kid " + keyId, null);
    }
}

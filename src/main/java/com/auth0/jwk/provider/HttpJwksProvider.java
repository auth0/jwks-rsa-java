package com.auth0.jwk.provider;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpJwksProvider implements JwkProvider {
    private final URL url;

    public HttpJwksProvider(String domain) {
        if (Strings.isNullOrEmpty(domain)) {
            throw new IllegalArgumentException("A domain is required");
        }

        try {
            final URL url = new URL(domain);
            this.url = new URL(url, "/.well-known/jwks.json");
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
            throw new SigningKeyNotFoundException("Cannot build jwks from url " + url.toString(), e);
        }
    }

    @Override
    public List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> keys = (List<Map<String, Object>>) getJwks().get("keys");
        for (Map<String, Object> values: keys) {
            jwks.add(Jwk.fromValues(values));
        }
        return jwks;
    }

    @Override
    public Jwk get(String keyId) throws SigningKeyNotFoundException {
        final List<Jwk> jwks = getAll();
        for (Jwk jwk: jwks) {
            if (keyId.equals(jwk.getId())) {
                return jwk;
            }
        }
        return null;
    }
}

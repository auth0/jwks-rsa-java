package com.auth0;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JwksProvider {
    private final JwksReader jwksReader;

    public JwksProvider(JwksReader jwksReader) {
        this.jwksReader = jwksReader;
    }

    public List<Jwks> getAllJwks() throws JwksException {
        return parse(this.jwksReader.getJwks());
    }

    public Jwks getJwks(String id) throws JwksException {
        for (Jwks jwks: getAllJwks()) {
            if (jwks.getId().equals(id)) {
                return jwks;
            }
        }

        throw new SigningKeyNotFoundException("Can't get specified signing key");
    }

    private List<Jwks> parse(InputStream input) throws JwksException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Jwks> jwksList = new ArrayList<>();

            final JsonNode keys = mapper.readTree(input).get("keys");
            final Iterator<JsonNode> it = keys.iterator();

            while (it.hasNext()) {
                JsonNode node = it.next();

                Jwks jwks = new Jwks(node.findValue("alg").asText(),
                        node.findValue("kid").asText(),
                        node.findValue("n").asText(),
                        node.findValue("e").asText()
                );

                jwksList.add(jwks);
            }

            return jwksList;
        } catch (IOException e) {
            throw new JwksException("Invalid JWKS Json", e);
        }
    }

}

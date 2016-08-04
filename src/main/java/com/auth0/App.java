package com.auth0;

public class App {
    public static void main(String[] args) throws JwksException {
        JwksReader reader = new FileBasedJwksReader("https://fortune.au.auth0.com/.well-known/jwks.json", "jwks.json");
        System.out.println(new JwksProvider(reader).getAllJwks().get(0).getPublicKey());
    }
}

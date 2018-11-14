package com.auth0.jwk;

import java.net.URL;

import static com.auth0.jwk.UrlJwkProvider.urlForDomain;

public class UrlJwkProviderBuilder extends JwkProviderBuilder {

    /**
     * Creates a new Builder with the given URL where to load the jwks from.
     *
     * @param url to load the jwks
     * @throws IllegalStateException if url is null
     * @return the builder
     */
    public static UrlJwkProviderBuilder from(URL url) {
        return new UrlJwkProviderBuilder(url);
    }

    /**
     * Creates a new Builder with a domain where to look for the jwks.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * <br><br> Use {@link #UrlJwkProviderBuilder(URL)} if you need to pass a full URL.
     * @param domain where jwks is published
     * @throws IllegalStateException if domain is null
     * @return the builder
     * @see UrlJwkProvider#UrlJwkProvider(String)
     */
    public static UrlJwkProviderBuilder from(String domain) {
        return new UrlJwkProviderBuilder(domain);
    }

    private UrlJwkProviderBuilder(URL url) {
        super(urlJwkProvider(url));
    }

    private UrlJwkProviderBuilder(String domain) {
        this(buildJwksUrl(domain));
    }

    static URL buildJwksUrl(String domain) {
        if (domain == null) {
            throw new IllegalStateException("Cannot build provider without domain");
        }
        return urlForDomain(domain);
    }

    static UrlJwkProvider urlJwkProvider(URL url) {
        if (url == null) {
            throw new IllegalStateException("Cannot build provider without url to jwks");
        }
        return new UrlJwkProvider(url);
    }
}

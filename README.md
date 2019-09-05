# jwks-rsa

[![CircleCI](https://circleci.com/gh/auth0/jwks-rsa-java.svg?style=svg)](https://circleci.com/gh/auth0/jwks-rsa-java)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0/jwks-rsa.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%20com.auth0%20a%3Ajwks-rsa)

## Install

### Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.8.3</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.auth0:jwks-rsa:0.9.0'
```

## Usage

The JSON Web Tokens you get from the Authorization Server include a [key id](https://tools.ietf.org/html/rfc7515#section-4.1.4) header parameter ("kid"), used to uniquely identify the Key used to sign the token.

i.e.: Given the following JWT:

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ
```

Decode it using any JWT library or tool like [jwt.io](https://jwt.io/?value=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ) and extract the `kid` parameter from the Header claims.

```json
{
  "typ": "JWT",
  "alg": "RS256",
  "kid": "RkI5MjI5OUY5ODc1N0Q4QzM0OUYzNkVGMTJDOUEzQkFCOTU3NjE2Rg"
}
```

Match the `kid` against the keys provided by your JWKS endpoint, then validate its signature.

### UrlJwksProvider

`UrlJwksProvider` fetches a list of JWKs from an URL.

```java
URL url = new URL("https://samples.auth0.com/");
JwksProvider jwksProvider = new UrlJwksProvider(url);
List<Jwk> jwks = jwksProvider.getJwks(); // throws JwtException when not found or can't get one
```

Get individual keys via `JwkProvider` using `DefaultJwkProvider`:

```java
JwkProvider provider = new DefaultJwkProvider(jwksProvider);
Jwk jwk = provider.getJwk("{kid of the signing key}");
```

If the JWK path is `/.well-known/jwks.json`, pass a `String` to the constructor:

```java
UrlJwksProvider provider = new UrlJwksProvider("https://samples.auth0.com/");
```

### CachedJwksProvider

`CachedJwksProvider` caches a `JwksProvider`.

> By default it stores the keys for 10 hours but these values can be changed

```java
JwksProvider http = new UrlJwksProvider("https://samples.auth0.com/");
JwksProvider jwksProvider = new CachedJwksProvider(http);
```

The implementation blocks so that a single thread is responsible for updating the cache at a time. For preemptive cache updates, configure the '`PreemptiveCachedJwksProvider`.

### RateLimitJwksProvider

`RateLimitJwskProvider` wraps a provider to limit the number of times it is invoked.

> By default the rate is limited to 10 per minute, but these values can be changed

```java
URL url = new URL("https://samples.auth0.com/");
JwksProvider http = new UrlJwksProvider(url);
Bucket bucket = new Bucket(10, 1, TimeUnit.MINUTES);
JwksProvider provider = new RateLimitJwksProvider(http, bucket);
```

### RetryingJwksProvider

`RetryingJwksProvider` wraps a provider to retry once when keys are reported as unavailable; this is usually triggered by an IOException.

This is a workaround for transient network problems.

### ShadowCachedJwksProvider

`ShadowCachedJwksProvider` wraps a provider to transparently cache keys. If the wrapped provider is unavailable, the shadow provider returns its cached instance untill the cache expires.

This is a workaround for temporary network problems / outage.

### JwkProviderBuilder

To create a provider for domain `https://samples.auth0.com` with cache and rate limit:

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

and specifying cache and rate limit attributes

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .cached(24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free Auth0 Account

1. Go to [Auth0](https://auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## Author

[Auth0](https://auth0.com)

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more info.

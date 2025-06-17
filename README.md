![A Java library for obtaining JSON Web Keys from a JWKS (JSON Web Key Set) endpoint.](https://cdn.auth0.com/website/sdks/banners/jwks-rsa-java-banner.png)

![Build Status](https://img.shields.io/github/checks-status/auth0/jwks-rsa-java/master)
[![Coverage Status](https://codecov.io/gh/auth0/jwks-rsa-java/branch/master/graph/badge.svg?style=flat-square)](https://codecov.io/github/auth0/jwks-rsa-java)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](https://doge.mit-license.org/)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0/jwks-rsa.svg?style=flat-square)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%20com.auth0%20a%3Ajwks-rsa)
[![javadoc](https://javadoc.io/badge2/com.auth0/jwks-rsa-java/javadoc.svg)](https://javadoc.io/doc/com.auth0/jwks-rsa)

> **Note**
> As part of our ongoing commitment to best security practices, we have rotated the signing keys used to sign previous releases of this SDK. As a result, new patch builds have been released using the new signing key. Please upgrade at your earliest convenience.
> 
> While this change won't affect most developers, if you have implemented a dependency signature validation step in your build process, you may notice a warning that past releases can't be verified. This is expected, and a result of the key rotation process. Updating to the latest version will resolve this for you.

:books: [Documentation](#documentation) - :rocket: [Getting Started](#getting-started) - :computer: [API Reference](#api-reference) :speech_balloon: [Feedback](#feedback)

## Documentation
- [Examples](./EXAMPLES.md) - code samples for common jwks-rsa-java scenarios.
- [Docs site](https://www.auth0.com/docs) - explore our docs site and learn more about Auth0.

## Getting Started

### Requirements

Java 8 or above.

### Installation

Add the dependency via Maven:

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>jwks-rsa</artifactId>
  <version>0.23.0</version>
</dependency>
```

or Gradle:

```gradle
implementation 'com.auth0:jwks-rsa:0.23.0'
```

### Usage

The JSON Web Tokens you obtain from an authorization server include a [key id](https://tools.ietf.org/html/rfc7515#section-4.1.4) header parameter ("kid"), used to uniquely identify the Key used to sign the token.

Given the following JWT:

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ
```

Decode it using a JWT library or tool like [jwt.io](https://jwt.io/?value=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ) and extract the `kid` parameter from the Header claims.

```json
{
  "typ": "JWT",
  "alg": "RS256",
  "kid": "RkI5MjI5OUY5ODc1N0Q4QzM0OUYzNkVGMTJDOUEzQkFCOTU3NjE2Rg"
}
```

The `kid` value can then be used to obtain the JWK using a `JwkProvider`.

Create a `JWKProvider` using the domain from which to fetch the JWK. The provider will use the domain to build the URL `https:{your-domain}/.well-known/jwks.json`: 

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .build();
```

A `Jwk` can be obtained using the `get(String keyId)` method:

```java
Jwk jwk = provider.get("{kid of the signing key}"); // throws Exception when not found or can't get one
```

The provider can be configured to cache JWKs to avoid unnecessary network requests, as well as only fetch the JWKs within a defined rate limit:

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
        // up to 10 JWKs will be cached for up to 24 hours
        .cached(10, 24, TimeUnit.HOURS)
        // up to 10 JWKs can be retrieved within one minute
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build();
```

See the [examples](./EXAMPLES.md) for additional configurations.

## API Reference

- [jwks-rsa-java JavaDocs](https://javadoc.io/doc/com.auth0/jwks-rsa/latest/)

## Feedback

### Contributing

We appreciate feedback and contribution to this repo! Before you get started, please see the following:

- [Auth0's general contribution guidelines](https://github.com/auth0/open-source-template/blob/master/GENERAL-CONTRIBUTING.md)
- [Auth0's code of conduct guidelines]((https://github.com/auth0/open-source-template/blob/master/CODE-OF-CONDUCT.md))

### Raise an issue
To provide feedback or report a bug, [please raise an issue on our issue tracker](https://github.com/auth0/jwks-rsa-java/issues).

### Vulnerability Reporting
Please do not report security vulnerabilities on the public Github issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

---

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="https://cdn.auth0.com/website/sdks/logos/auth0_light_mode.png"   width="150">
    <source media="(prefers-color-scheme: dark)" srcset="https://cdn.auth0.com/website/sdks/logos/auth0_dark_mode.png" width="150">
    <img alt="Auth0 Logo" src="./auth0_light_mode.png" width="150">
  </picture>
</p>
<p align="center">Auth0 is an easy to implement, adaptable authentication and authorization platform. To learn more checkout <a href="https://auth0.com/why-auth0">Why Auth0?</a></p>
<p align="center">
This project is licensed under the MIT license. See the <a href="./LICENSE"> LICENSE</a> file for more info.</p>

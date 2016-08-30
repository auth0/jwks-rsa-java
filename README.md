# jwks-rsa for Java

[![Build Status](https://travis-ci.org/auth0/jwks-rsa-java.svg?branch=master)](https://travis-ci.org/auth0/jwks-rsa-java)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0.android/lock.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%20com.auth0%20a%3Ajwks-rsa)

## Install

### Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```gradle
compile 'com.auth0:jwks-rsa:0.1.0'
```

## Usage

### No Cache

`UrlJwkProvider` fetches the jwk from `/.well-known/jwks.json` of the supplied domain issuer and returns a `Jwk` if the `kid` matches one of the registered keys.

```java
JwkProvider provider = new UrlJwkProvider("https://samples.auth0.com/");
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

or using the `JwkProviderBuilder`

```java
JwkProvider provider = new JwkProviderBuilder()
    .forDomain("https://samples.auth0.com/")
    .cached(false)
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

### Cached

`GuavaCachedJwkProvider` cache the jwk in a LRU in memory cache, if the jwk is not found in the cache it will ask another provider for it and store it's result in the cache.

> By default it stores 5 keys for 10 hours but these values can be changed

```java
JwkProvider http = new UrlJwkProvider("https://samples.auth0.com/");
JwkProvider provider = new GuavaCachedJwkProvider(http);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one

```

or using the `JwkProviderBuilder` falling back to `UrlJwkProvider` when jwk is not cached

```java
JwkProvider provider = new JwkProviderBuilder()
    .forDomain("https://samples.auth0.com/")
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

and specifying cache attributes

```java
JwkProvider provider = new JwkProviderBuilder()
    .forDomain("https://samples.auth0.com/")
    .cached(10, 24, TimeUnit.HOURS)
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
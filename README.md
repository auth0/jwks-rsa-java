# jwks-rsa-java

## Install

### Maven

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

### No Cache

`UrlJwkProvider` fetches the jwk from `/.well-known/jwks.json` of the supplied domain issuer and returns a `Jwk` if the `kid` matches one of the registered keys.

```java
JwkProvider provider = new HttpJwkProvider("https://samples.auth0.com/");
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

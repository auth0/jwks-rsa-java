# Examples using jwks-rsa-java

- [Provider configuration](#provider-configuration)
- [Error handling](#error-handling)

## Provider configuration

This library contains several `JwkProvider` implementations, which is used to obtain a JWK.

The `JwkProviderBuilder` is the preferred way to create a `JwkProvider`. Configurations can be combined to suit your needs.

### Cache retrieved JWK

To create a provider for domain `https://samples.auth0.com` that will cache a JWK using an LRU in-memory cache:

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
        // cache up to 10 JWKs for up to 24 hours
        .cached(10, 24, TimeUnit.HOURS)
        .build(); 
```

### Configure rate limits

`RateLimitJwkProvider` will limit the amounts of different signing keys to get in a given time frame.

> By default the rate is limited to 10 different keys per minute but these values can be changed.

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
        // up to 10 JWKs can be retrieved within one minute
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build();
```

### Configure network timeout settings

The connect and read network timeouts can be configured using the builder:

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
        // Connect timeout of 1 second, read timeout of 2 seconds (values are in milliseconds)
        .timeouts(1000, 2000)
        .build();
```

See the [JwkProviderBuilder JavaDocs](https://javadoc.io/doc/com.auth0/jwks-rsa/latest/com/auth0/jwk/JwkProviderBuilder.html) for all available configurations.

## Error handling

There are certain scenarios in which this library can fail. Read below to understand what to expect and how to handle the errors.

### Missing JSON Web Key
This error may arise when the hosted JSON Web Key set (JWKS) file doesn't represent a valid set of keys, or is empty.
They are raised as a `SigningKeyNotFoundException`. The cause should to be inspected in order to understand the specific failure reason.

#### Network error
There's a special case for Network errors. These errors represent timeouts, invalid URLs, or a faulty internet connection.
They may occur when fetching the keys from the given URL. They are raised as a `NetworkException` instance.

If you need to detect this scenario, make sure to check it before the catch of `SigningKeyNotFoundException`.

```java
try {
    // ...
} catch (NetworkException e) {
    // Network error
} catch (SigningKeyNotFoundException e) {
    // Key is invalid or not found
}
```

### Unsupported JSON Web Key
When the received key is not of a supported type, or the attribute values representing it are wrong, an `InvalidPublicKeyException` will be raised.
The following key types are supported:
- RSA
- Elliptic Curve
    - P-256
    - P-384
    - P-521

### Rate limits
When using a rate-limited provider, a `RateLimitReachedException` error will be raised when the limit is breached.
The exception can help determine how long to wait until the next call is available.

```java
try {
    // ...
} catch (RateLimitReachedException e) {
    long waitTime = e.getAvailableIn()
    // wait until available
}
```

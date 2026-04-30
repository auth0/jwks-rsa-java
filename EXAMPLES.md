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

### Configure a custom HTTP client

The `httpClient()` builder method lets you replace the default `java.net.URLConnection`-based HTTP transport with any HTTP library. This solves four common requirements: custom TLS, authenticated proxies, Cache-Control header access, and HTTP/2.

> When `httpClient()` is set, `proxied()`, `timeouts()`, and `headers()` are ignored — the custom client has full control over the HTTP layer.

#### Custom TLS 

Force TLS 1.3 for JWKS calls without affecting the rest of your JVM:

```java
SSLContext tls13 = SSLContext.getInstance("TLSv1.3");
tls13.init(null, null, null);

JwksHttpClient tlsClient = url -> {
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setSSLSocketFactory(tls13.getSocketFactory());
    conn.setRequestProperty("Accept", "application/json");
    try (InputStream in = conn.getInputStream()) {
        String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        return new JwksHttpResponse(body, conn.getHeaderFields());
    }
};

JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .httpClient(tlsClient)
    .build();
```

> **Note:** TLS 1.3 requires Java 11+ or a provider like [Conscrypt](https://github.com/google/conscrypt) on Java 8.

#### Authenticated Proxy 

Use OkHttp to authenticate with a corporate proxy that requires credentials:

```java
OkHttpClient okHttp = new OkHttpClient.Builder()
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.corp.com", 8080)))
    .proxyAuthenticator((route, response) ->
        response.request().newBuilder()
            .header("Proxy-Authorization", Credentials.basic("user", "pass"))
            .build())
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .build();

JwksHttpClient proxyClient = url -> {
    Request request = new Request.Builder().url(url).build();
    try (Response response = okHttp.newCall(request).execute()) {
        return new JwksHttpResponse(
            response.body().string(),
            response.headers().toMultimap()
        );
    }
};

JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .httpClient(proxyClient)
    .build();
```

#### Cache-Control Headers 

Response headers (including `Cache-Control`) are now accessible via `JwksHttpResponse`:

```java
JwksHttpClient headerAwareClient = url -> {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Accept", "application/json");
    try (InputStream in = conn.getInputStream()) {
        String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        JwksHttpResponse response = new JwksHttpResponse(body, conn.getHeaderFields());

        // Headers are now available for inspection
        String cacheControl = response.getHeaderValue("Cache-Control");
        // e.g., "max-age=3600"

        return response;
    }
};

JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .httpClient(headerAwareClient)
    .build();
```

#### HTTP/2 Support

**Using Java 11+ HttpClient:**

```java
java.net.http.HttpClient http2Client = java.net.http.HttpClient.newBuilder()
    .version(java.net.http.HttpClient.Version.HTTP_2)
    .connectTimeout(Duration.ofSeconds(5))
    .build();

JwksHttpClient h2Client = url -> {
    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(url.toURI())
        .header("Accept", "application/json")
        .GET()
        .build();
    java.net.http.HttpResponse<String> response = http2Client.send(
        request, java.net.http.HttpResponse.BodyHandlers.ofString());
    return new JwksHttpResponse(response.body(), response.headers().map());
};

JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .httpClient(h2Client)
    .build();
```

**Using OkHttp (Java 8 compatible):**

```java
OkHttpClient okHttp = new OkHttpClient.Builder()
    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .build();

JwksHttpClient okClient = url -> {
    Request request = new Request.Builder().url(url).build();
    try (Response response = okHttp.newCall(request).execute()) {
        return new JwksHttpResponse(
            response.body().string(),
            response.headers().toMultimap()
        );
    }
};

JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .httpClient(okClient)
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

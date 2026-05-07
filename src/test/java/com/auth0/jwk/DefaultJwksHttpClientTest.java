package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class DefaultJwksHttpClientTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldFetchAndReturnBodyAndHeaders() throws Exception {
        String json = "{\"keys\":[]}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", Collections.singletonList("application/json"));
        when(connection.getHeaderFields()).thenReturn(responseHeaders);

        URL url = createMockUrl(connection);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, null, null);

        JwksHttpResponse response = client.fetch(url);

        assertThat(response.getBody(), is(json));
        assertThat(response.getHeaders(), is(responseHeaders));
    }

    @Test
    public void shouldSetTimeouts() throws Exception {
        String json = "{}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(connection.getHeaderFields()).thenReturn(Collections.<String, List<String>>emptyMap());

        URL url = createMockUrl(connection);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(5000, 10000, null, null);

        client.fetch(url);

        verify(connection).setConnectTimeout(5000);
        verify(connection).setReadTimeout(10000);
    }

    @Test
    public void shouldNotSetTimeoutsWhenNull() throws Exception {
        String json = "{}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(connection.getHeaderFields()).thenReturn(Collections.<String, List<String>>emptyMap());

        URL url = createMockUrl(connection);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, null, null);

        client.fetch(url);

        verify(connection, never()).setConnectTimeout(anyInt());
        verify(connection, never()).setReadTimeout(anyInt());
    }

    @Test
    public void shouldSetDefaultAcceptHeader() throws Exception {
        String json = "{}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(connection.getHeaderFields()).thenReturn(Collections.<String, List<String>>emptyMap());

        URL url = createMockUrl(connection);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, null, null);

        client.fetch(url);

        verify(connection).setRequestProperty("Accept", "application/json");
    }

    @Test
    public void shouldSetCustomHeaders() throws Exception {
        String json = "{}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(connection.getHeaderFields()).thenReturn(Collections.<String, List<String>>emptyMap());

        URL url = createMockUrl(connection);
        Map<String, String> customHeaders = new LinkedHashMap<>();
        customHeaders.put("Authorization", "Bearer token");
        customHeaders.put("X-Custom", "value");
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, null, customHeaders);

        client.fetch(url);

        verify(connection).setRequestProperty("Authorization", "Bearer token");
        verify(connection).setRequestProperty("X-Custom", "value");
    }

    @Test
    public void shouldThrowIOExceptionOnNetworkError() throws Exception {
        expectedException.expect(IOException.class);

        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenThrow(new IOException("Connection refused"));

        URL url = createMockUrl(connection);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, null, null);

        client.fetch(url);
    }

    @Test
    public void shouldUseProxyWhenProvided() throws Exception {
        String json = "{}";
        URLConnection connection = mock(URLConnection.class);
        when(connection.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(connection.getHeaderFields()).thenReturn(Collections.<String, List<String>>emptyMap());

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
        URL url = createMockUrl(connection, proxy);
        DefaultJwksHttpClient client = new DefaultJwksHttpClient(null, null, proxy, null);

        JwksHttpResponse response = client.fetch(url);
        assertThat(response.getBody(), is(json));
    }

    private URL createMockUrl(final URLConnection connection) throws Exception {
        return createMockUrl(connection, null);
    }

    private URL createMockUrl(final URLConnection connection, final Proxy expectedProxy) throws Exception {
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return connection;
            }

            @Override
            protected URLConnection openConnection(URL u, Proxy p) throws IOException {
                return connection;
            }
        };
        return new URL("http", "localhost", 80, "/.well-known/jwks.json", handler);
    }
}

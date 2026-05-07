package com.auth0.jwk;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwksHttpResponseTest {

    @Test
    public void shouldReturnBody() {
        JwksHttpResponse response = new JwksHttpResponse("{\"keys\":[]}", Collections.<String, List<String>>emptyMap());
        assertThat(response.getBody(), is("{\"keys\":[]}"));
    }

    @Test
    public void shouldReturnHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cache-Control", Collections.singletonList("max-age=600"));
        JwksHttpResponse response = new JwksHttpResponse("{}", headers);
        assertThat(response.getHeaders(), is(headers));
    }

    @Test
    public void shouldReturnEmptyHeadersWhenNull() {
        JwksHttpResponse response = new JwksHttpResponse("{}", null);
        assertThat(response.getHeaders(), is(notNullValue()));
        assertThat(response.getHeaders().isEmpty(), is(true));
    }

    @Test
    public void shouldReturnEmptyHeadersWithBodyOnlyConstructor() {
        JwksHttpResponse response = new JwksHttpResponse("{}");
        assertThat(response.getHeaders(), is(notNullValue()));
        assertThat(response.getHeaders().isEmpty(), is(true));
    }

    @Test
    public void shouldGetHeaderValueCaseInsensitive() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cache-Control", Collections.singletonList("max-age=600"));
        JwksHttpResponse response = new JwksHttpResponse("{}", headers);

        assertThat(response.getHeaderValue("Cache-Control"), is("max-age=600"));
        assertThat(response.getHeaderValue("cache-control"), is("max-age=600"));
        assertThat(response.getHeaderValue("CACHE-CONTROL"), is("max-age=600"));
    }

    @Test
    public void shouldReturnNullForMissingHeader() {
        JwksHttpResponse response = new JwksHttpResponse("{}", Collections.<String, List<String>>emptyMap());
        assertThat(response.getHeaderValue("X-Missing"), is(nullValue()));
    }

    @Test
    public void shouldReturnFirstValueWhenMultipleValues() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Multi", Arrays.asList("first", "second"));
        JwksHttpResponse response = new JwksHttpResponse("{}", headers);

        assertThat(response.getHeaderValue("X-Multi"), is("first"));
    }

    @Test
    public void shouldReturnNullForEmptyValuesList() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Empty", Collections.<String>emptyList());
        JwksHttpResponse response = new JwksHttpResponse("{}", headers);

        assertThat(response.getHeaderValue("X-Empty"), is(nullValue()));
    }

    @Test
    public void shouldHandleNullHeaderKey() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        headers.put("Content-Type", Collections.singletonList("application/json"));
        JwksHttpResponse response = new JwksHttpResponse("{}", headers);

        assertThat(response.getHeaderValue("Content-Type"), is("application/json"));
        assertThat(response.getHeaderValue("Missing"), is(nullValue()));
    }
}

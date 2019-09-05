package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJwkProviderTest {

    private static final String KID = "KID";

    @Mock
    private JwksProvider jwksProvider;

    @Mock
    private Jwk jwk;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DefaultJwkProvider jwkProvider;

    @Before
    public void setUp() throws Exception {
        jwkProvider = new DefaultJwkProvider(jwksProvider);

        when(jwk.getId()).thenReturn(KID);
    }

    @Test
    public void shouldSucceedToGetJwtForKnownKeyId() throws JwkException {
        when(jwksProvider.getJwks()).thenReturn(Arrays.asList(jwk));
        assertThat(jwkProvider.getJwk(KID), equalTo(jwk));
    }

    @Test
    public void shouldFailToGetJwtForUnknownKeyId() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        when(jwksProvider.getJwks()).thenReturn(Arrays.asList(jwk));
        jwkProvider.getJwk("unknown");
    }

    @Test
    public void shouldSucceedToGetJwtForKnownNullKeyId() throws Exception {
        Jwk jwk = mock(Jwk.class);

        when(jwksProvider.getJwks()).thenReturn(Arrays.asList(jwk));

        when(jwk.getId()).thenReturn(null);

        assertThat(jwkProvider.getJwk(null), equalTo(jwk));
    }

    @Test
    public void shouldFailToGetJwtForUnknownNullKeyId() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        when(jwksProvider.getJwks()).thenReturn(Arrays.asList(jwk));
        jwkProvider.getJwk(null);
    }

}

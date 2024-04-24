package com.backbase.ct.bbfuel.util.security;

import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.core.properties.JsonWebTokenProperties;
import com.backbase.buildingblocks.jwt.core.properties.Signature;
import com.backbase.buildingblocks.jwt.core.properties.TokenKey;
import com.backbase.buildingblocks.jwt.core.properties.TokenKeyType;
import com.backbase.buildingblocks.jwt.core.token.JsonWebTokenClaimsSet;
import com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

public final class JwtFactory {

    private static JsonWebTokenProperties jwtProperties = new JsonWebTokenProperties() {

        @Override
        public String getType() {
            return JsonWebTokenTypeFactory.SIGNED_TOKEN_TYPE;
        }
    };

    private JwtFactory() {
    }

    public static String generateJwt(String secretKey) throws JsonWebTokenException {
        Signature signature = new Signature();
        TokenKey tokenKey = new TokenKey();
        signature.setKey(tokenKey);
        tokenKey.setType(TokenKeyType.VALUE);
        tokenKey.setValue(secretKey);
        // Stops a kid being added
        tokenKey.setId("");
        jwtProperties.setSignature(signature);

        JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> producer =
            JsonWebTokenTypeFactory.getProducer(jwtProperties);

        Map<String, Object> claims = new HashMap<>();

        claims.put("scope", new String[]{"api:service"});
        claims.put("sub", "bb-client");
        claims.put("rol", singletonList("MTLS"));
        claims.put("iat", Instant.now().minus(5, ChronoUnit.SECONDS).toEpochMilli());
        claims.put("exp", Instant.now().plus(30, ChronoUnit.MINUTES).toEpochMilli());

        JsonWebTokenClaimsSet claimsSet = new JsonWebTokenClaimsSet() {

            @Override
            public Map<String, Object> getClaims() {
                return claims;
            }

            @Override
            public Optional<Object> getClaim(String claimName) {
                return Optional.ofNullable(claims.get(claimName));
            }
        };

        return producer.createToken(claimsSet);
    }
}
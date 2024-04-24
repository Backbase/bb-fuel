package com.backbase.ct.bbfuel.util.security;

import com.backbase.buildingblocks.jwt.core.JsonWebTokenProducerType;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.jwt.core.properties.Encryption;
import com.backbase.buildingblocks.jwt.core.properties.JsonWebTokenProperties;
import com.backbase.buildingblocks.jwt.core.properties.Signature;
import com.backbase.buildingblocks.jwt.core.properties.TokenKey;
import com.backbase.buildingblocks.jwt.core.properties.TokenKeyType;
import com.backbase.buildingblocks.jwt.core.token.JsonWebTokenClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.RestAssured;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory.SIGNED_TOKEN_TYPE;
import static com.backbase.buildingblocks.jwt.core.type.JsonWebTokenTypeFactory.getProducer;
import static com.backbase.ct.bbfuel.util.security.data.SecurityData.JWT_EXTERNAL_ENC_SECRETKEY;
import static com.backbase.ct.bbfuel.util.security.data.SecurityData.JWT_EXTERNAL_SECRETKEY;
import static com.backbase.ct.bbfuel.util.security.data.SecurityData.JWT_INTERNAL_SECRETKEY;

@Service
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(String.valueOf(JwtUtils.class));

    public String generateInternalToken(final Claims.ClaimsBuilder claimsBuilder) {
        try {
            return createAuthorisationToken(claimsBuilder.build().toMap());
        } catch (JsonWebTokenException e) {
            logger.error("Generate internal token failure!", e);
        }
        return "";
    }

    private String createAuthorisationToken(Map<String, Object> claims) throws JsonWebTokenException {
        String sigSecretKey;
        String extSigSecretKey;
        String extEncSigSecretKey;

        sigSecretKey = fetchSecret(JWT_INTERNAL_SECRETKEY);
        extSigSecretKey = fetchSecret(JWT_EXTERNAL_SECRETKEY);
        extEncSigSecretKey = fetchSecret(JWT_EXTERNAL_ENC_SECRETKEY);

        return createAuthorisationTokenAndEncode(claims, SIGNED_TOKEN_TYPE, sigSecretKey, extSigSecretKey, extEncSigSecretKey);
    }

    private String createAuthorisationTokenAndEncode(Map<String, Object> claims, String tokenType, String sigSecretKey,
                                                     String extSigSecretKey, String extEncSecretKey) throws JsonWebTokenException {
        String token = "";

        System.setProperty("SIG_SECRET_KEY", sigSecretKey);
        System.setProperty("EXTERNAL_SIG_SECRET_KEY", extSigSecretKey);
        System.setProperty("EXTERNAL_ENC_SECRET_KEY", extEncSecretKey);

        try {
            JsonWebTokenProperties properties = createTokenProperties(tokenType);

            @SuppressWarnings("unchecked")
            JsonWebTokenProducerType<JsonWebTokenClaimsSet, String> producer = getProducer(properties);

            JsonWebTokenClaimsSet claimsSet = new TestJsonWebTokenClaimsSet(claims);
            token = producer.createToken(claimsSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JsonWebTokenException(ex);
        }
        return token;
    }

    private JsonWebTokenProperties createTokenProperties(String tokenType) {
        JsonWebTokenProperties properties = new TestJsonWebTokenProperties(tokenType);

        Signature signature = new Signature();
        TokenKey tokenKey = new TokenKey();
        tokenKey.setType(TokenKeyType.ENV);
        tokenKey.setValue(SIGNED_TOKEN_TYPE.equals(tokenType) ? "SIG_SECRET_KEY"
            : "EXTERNAL_SIG_SECRET_KEY");

        signature.setKey(tokenKey);
        properties.setSignature(signature);

        Encryption encryption = new Encryption();
        TokenKey externalTokenKey = new TokenKey();
        externalTokenKey.setType(TokenKeyType.ENV);
        externalTokenKey.setValue("EXTERNAL_ENC_SECRET_KEY");

        encryption.setKey(externalTokenKey);
        // TODO encryption.setMethod(EncryptionMethod.A256CBC_HS512);
        properties.setEncryption(encryption);
        return properties;
    }

    public Optional<String> getRealmFromIssuerClaim(String jwt) {
        TestJsonWebTokenClaimsSet jwtClaims = createClaimsSetFromToken(jwt);
        if (jwtClaims == null) {
            logger.warn("Could not parse token to get claims.");
            return Optional.empty();
        }

        Optional<Object> optionalIssuer = jwtClaims.getClaim("iss");
        if (!optionalIssuer.isPresent()) {
            logger.warn("Could not retrieve issuer claim from jwt");
            return Optional.empty();
        }
        String issuer = (String) optionalIssuer.get();

        // pattern matches everything after the last "/" in a string
        final Pattern p = Pattern.compile("(?<=realms\\/)[\\S]+$");
        Matcher matcher = p.matcher(issuer);
        if (!matcher.find()) {
            logger.warn("Could not find realm from the tokens issuer claim");
            return Optional.empty();
        }

        String realm = matcher.group();

        return Optional.of(realm);
    }

    private TestJsonWebTokenClaimsSet createClaimsSetFromToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            return new TestJsonWebTokenClaimsSet(signedJwt.getJWTClaimsSet().getClaims());
        } catch (ParseException e) {
            logger.warn("Token failed verification: {}", token, e);
        }
        return null;
    }

    @SneakyThrows
    private String fetchSecret(String keyType) {

        String baseUrl = "https://jwt-ep-72bq6.eph.rndbb.azure.backbaseservices.com/";
        String tokenUrl = baseUrl + keyType;

        return RestAssured.given()
            .log().all()
            .get(tokenUrl)
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();
    }
}

class TestJsonWebTokenClaimsSet implements JsonWebTokenClaimsSet {
    private final Map<String, Object> claims;

    TestJsonWebTokenClaimsSet(Map<String, Object> claims) {
        this.claims = claims;
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    @Override
    public Optional<Object> getClaim(String claimName) {
        return Optional.ofNullable(claims.get(claimName));
    }
}

class TestJsonWebTokenProperties extends JsonWebTokenProperties {

    private final String type;

    TestJsonWebTokenProperties(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}

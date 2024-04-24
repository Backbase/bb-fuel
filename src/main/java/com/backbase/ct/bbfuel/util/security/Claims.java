package com.backbase.ct.bbfuel.util.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Builder
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
public class Claims {

    @Builder.Default
    private final Object naf = System.currentTimeMillis() / 1000 + 3600 * 24;
    @Builder.Default
    private final Object cnexp = true;
    @Builder.Default
    private final Object grp = Collections.singletonList("user(USER)");
    @Builder.Default
    private final Object anloc = true;
    @Builder.Default
    private final Object anexp = true;
    @Builder.Default
    private final Object enbl = true;
    @Builder.Default
    private final Object exp = 2533644487L;
    @Builder.Default
    private final Object iat = 2533640487L;
    @Builder.Default
    private final Object rol = Arrays.asList("ROLE_group_user(USER)", "ROLE_USER");
    @Builder.Default
    private final Object jti = "37cea40b-b1d9-486c-9522-834654f4b986";

    private final Object sub;  //subscriber the username/user external id
    private final Object said; // service agreement id
    private final Object leid; // legal entity id
    private final Object inuid; // internal user id
    private final Object tid;  // tenant id
    private final Object scope; // scope for scoped access

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        ObjectMapper mapper = new ObjectMapper();
        return (Map<String, Object>) mapper.convertValue(this, Map.class);
    }
}

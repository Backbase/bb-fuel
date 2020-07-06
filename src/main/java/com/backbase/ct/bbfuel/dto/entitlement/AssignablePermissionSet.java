package com.backbase.ct.bbfuel.dto.entitlement;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignablePermissionSet {

    private Long id;
    private String name;
    private String description;
    private String type;
    private List<Permission> permissions;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Permission {

        private String functionId;
        private String functionName;
        private String resourceName;
        private List<String> privileges;
    }
}

package com.backbase.ct.bbfuel.dto.entitlement;

import static org.springframework.beans.BeanUtils.copyProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
public class JobProfile extends DbsEntity {

    public static final String PROFILE_ROLE_ADMIN = "admin";

    private String jobProfileName;
    private String approvalLevel;
    private List<String> roles;
    private Boolean isRetail;
    private List<Permission> permissions;
    private String type;

    public JobProfile() {
        super();
    }

    public JobProfile(JobProfile source) {
        copyProperties(source, this);
        if (source.getRoles() != null) {
            this.setRoles(new ArrayList<>(source.getRoles()));
        }
        if (source.getPermissions() != null) {
            this.setPermissions(Arrays.asList(
                source.getPermissions().toArray(new Permission[0]).clone()));
        }
    }
}

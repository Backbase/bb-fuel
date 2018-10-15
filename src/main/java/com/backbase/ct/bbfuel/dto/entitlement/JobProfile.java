package com.backbase.ct.bbfuel.dto.entitlement;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.beanutils.BeanUtils;

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

    public JobProfile() {
        super();
    }

    public JobProfile(JobProfile source) throws InvocationTargetException, IllegalAccessException {
        copyProperties(this, source);
        if (source.getRoles() != null) {
            this.setRoles(new ArrayList<>(source.getRoles()));
        }
        if (source.getPermissions() != null) {
            this.setPermissions(Arrays.asList(
                source.getPermissions().toArray(new Permission[0]).clone()));
        }
    }
}

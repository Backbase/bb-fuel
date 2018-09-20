package com.backbase.ct.dataloader.dto.entitlement;

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
    private String jobProfileName;
    private String approvalLevel;
    private List<Permission> permissions;

    public JobProfile() {
        super();
    }

    public JobProfile(JobProfile source) {
        this.setJobProfileName(source.getJobProfileName());
        this.setApprovalLevel(source.getApprovalLevel());
        if (permissions != null) {
            this.setPermissions(Arrays.asList(
                source.getPermissions().toArray(new Permission[0]).clone()));
        }
    }
}

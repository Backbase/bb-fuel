package com.backbase.ct.dataloader.dto.entitlement;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobProfile {
    private String jobProfileName;
    private String approvalLevel;
    private List<Permission> permissions;

}

package com.backbase.ct.dataloader.dto;

import static com.google.common.base.Predicates.alwaysTrue;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalEntityWithUsers {
    private String legalEntityExternalId;
    private String parentLegalEntityExternalId;
    private String legalEntityName;
    private String legalEntityType;
    private Category category;

    private @Singular List<User> users;

    private List<String> filterUserExternalIds(Predicate<User> userFilter) {
        if (users != null) {
            Set<String> ids = users.stream()
                .filter(userFilter)
                .map(User::getExternalId)
                .collect(Collectors.toSet());
            return new ArrayList<>(ids);
        }
        return Collections.emptyList();
    }

    public List<String> filterUserExternalIdsOnRole(String role) {
        return filterUserExternalIds(user -> role.equals(user.getRole()));
    }

    public List<String> getAdminUserExternalIds() {
        return filterUserExternalIdsOnRole(JobProfile.PROFILE_ROLE_ADMIN);
    }

    public List<String> getUserExternalIds() {
        return filterUserExternalIds(alwaysTrue());
    }
}

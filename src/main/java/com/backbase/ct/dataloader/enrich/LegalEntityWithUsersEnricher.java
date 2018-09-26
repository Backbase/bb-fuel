package com.backbase.ct.dataloader.enrich;

import static com.backbase.ct.dataloader.util.CommonHelpers.splitDelimitedWordToSingleCapatilizedWords;

import com.backbase.ct.dataloader.dto.Category;
import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.User;
import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.github.javafaker.Faker;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LegalEntityWithUsersEnricher {

    private static final Faker FAKER = new Faker();

    /**
     * Build it up with first and last name because faker fullname method sometimes adds prefix or suffix.
     */
    private static String buildFakerFullName() {
        return FAKER.name().firstName() + " " + FAKER.name().lastName();
    }

    /**
     * Create the root Legal Entity and admin user with given externalUserId.
     */
    public static LegalEntityWithUsers createRootLegalEntityWithAdmin(String externalUserId) {
        return LegalEntityWithUsers.builder()
            .branch(Category.ROOT)
            .user(createAdminUser(externalUserId)).build();
    }

    /**
     * Create user with admin role, fake fullName and given externalId.
     */
    public static User createAdminUser(String externalId) {
        return User.builder()
            .externalId(externalId)
            .role(JobProfile.PROFILE_ROLE_ADMIN)
            .fullName(buildFakerFullName()).build();
    }

    private static boolean isRetailUser(LegalEntityWithUsers legalEntityWithUsers) {
        return legalEntityWithUsers.getUsers().size() == 1
            && legalEntityWithUsers.getLegalEntityName() == null;
    }

    /**
     * Give names to LE and its users if not set. Give users the admin role if not set.
     */
    public void enrich(List<LegalEntityWithUsers> legalEntityWithUsers) {
        legalEntityWithUsers.forEach(le -> {
            enrichLegalEntity(le);
            enrichUsers(le.getUsers());
        });
    }

    private void enrichLegalEntity(LegalEntityWithUsers legalEntity) {
        if (legalEntity.getCategory() == null) {
            legalEntity.setCategory(isRetailUser(legalEntity)
            ? Category.RETAIL : Category.BUSINESS);
        }
        legalEntity.setLegalEntityName(
            legalEntity.getCategory().isRetail()
                ? buildFakerFullName()
                : legalEntity.getLegalEntityName()
        );
    }

    private void enrichUsers(List<User> users) {
        users.forEach(this::enrichUser);
    }

    /**
     * Assign the admin role when not explicitly set. When fullName is not set do the following:
     * If externalId contains one or more separators (._) convert it by capitalizing each word and split with a space.
     * Otherwise fake up the first and last name.
     */
    private void enrichUser(User user) {
        if (StringUtils.isEmpty(user.getFullName())) {
            if (user.getExternalId().matches(".*[_.].*")) {
                user.setFullName(splitDelimitedWordToSingleCapatilizedWords(user.getExternalId(), "_."));
            } else {
                user.setFullName(buildFakerFullName());
            }
        }
        if (StringUtils.isEmpty(user.getRole())) {
            user.setRole(JobProfile.PROFILE_ROLE_ADMIN);
        }
    }
}

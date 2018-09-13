package com.backbase.ct.dataloader.enrich;

import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.dto.User;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LegalEntityWithUsersEnricher {

    private Faker faker = new Faker();

    private static boolean isRetailUser(LegalEntityWithUsers legalEntityWithUsers) {
        return legalEntityWithUsers.getUsers().size() == 1
            && legalEntityWithUsers.getLegalEntityName() == null;
    }

    public void enrich(List<LegalEntityWithUsers> legalEntityWithUsers) {
        legalEntityWithUsers.forEach( le -> {
            enrichLegalEntity(le);
            enrichUsers(le.getUsers());
        });
    }

    private void enrichLegalEntity(LegalEntityWithUsers legalEntity) {
        legalEntity.setLegalEntityName(
            isRetailUser(legalEntity)
                ? faker.name().firstName() + " " + faker.name().lastName()
                : legalEntity.getLegalEntityName()
        );
    }

    /**
     * Split a string, capitalize each word and divide them by a space.
     */
    private static String convertLogonNameToFullName(String value, String separatorChars) {
        String[] strings = StringUtils.split(value.toLowerCase(), separatorChars);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings, " ");
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
                user.setFullName(convertLogonNameToFullName(user.getExternalId(), "_."));
            } else {
                user.setFullName(faker.name().firstName() + " " + faker.name().lastName());
            }
        }
        if (StringUtils.isEmpty(user.getRole())) {
            user.setRole("admin");
        }
    }
}

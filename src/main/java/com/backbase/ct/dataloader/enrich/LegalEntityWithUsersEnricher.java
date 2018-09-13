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
        return legalEntityWithUsers.getUserExternalIds().size() == 1
            && legalEntityWithUsers.getLegalEntityName() == null;
    }

    public void enrich(List<LegalEntityWithUsers> legalEntityWithUsers) {
        legalEntityWithUsers.forEach( le -> {
            enrichLegalEntity(le);
            enrichUsers(le);
        });
    }

    private void enrichLegalEntity(LegalEntityWithUsers legalEntity) {
        legalEntity.setLegalEntityName(
            isRetailUser(legalEntity)
                ? faker.name().firstName() + " " + faker.name().lastName()
                : legalEntity.getLegalEntityName()
        );
    }

    private void enrichUsers(LegalEntityWithUsers legalEntity) {
        createAdminUsersFromSimpleIds(legalEntity);
        enrichUsers(legalEntity.getUsers());
    }

    private List<User> createAdminUsersFromSimpleIds(LegalEntityWithUsers legalEntity) {
        List<User> users = legalEntity.getUsers();
        List<String> ids = legalEntity.getUserExternalIds();
        if (ids != null) {
            List<User> simpleUsers = ids.stream()
                .map(id -> User.builder().externalId(id).build())
                .collect(Collectors.toList());
            if (users == null) {
                users = new ArrayList<>();
            }
            users.addAll(simpleUsers);
            legalEntity.setUsers(users);
        }
        return users;
    }

    /**
     * Split a string, capitalize each word and divide them by a space.
     */
    public static String convertLogonNameToFullName(String value, String separatorChars) {
        String[] strings = StringUtils.split(value.toLowerCase(), separatorChars);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings, " ");
    }

    private void enrichUsers(List<User> users) {
        users.forEach(user -> enrichUser(user));
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

package com.backbase.ct.bbfuel.service.factory;

import static com.backbase.ct.bbfuel.service.factory.TestData.EXTERNAL_LEGAL_ENTITY_ID;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import java.util.ArrayList;
import java.util.List;

public class LegalEntityWithUsersFactory {

    public static LegalEntityWithUsers createLegalEntityWithUsers(String userExternalId) {
        LegalEntityWithUsers le = new LegalEntityWithUsers();
        le.setLegalEntityExternalId(EXTERNAL_LEGAL_ENTITY_ID);
        le.setUsers(singletonList(User.builder().externalId(userExternalId).build()));

        return createLegalEntityWithUsers(singletonList(userExternalId));
    }

    public static LegalEntityWithUsers createLegalEntityWithUsers(List<String> userExternalIds) {
        LegalEntityWithUsers le = new LegalEntityWithUsers();
        List<User> users = new ArrayList<>();
        le.setUsers(users);
        userExternalIds.forEach(externalId -> users.add(User.builder().externalId(externalId).build()));

        return le;
    }
}

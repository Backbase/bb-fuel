package com.backbase.testing.dataloader;

import com.backbase.testing.dataloader.setup.BankSetup;
import com.backbase.testing.dataloader.setup.UsersSetup;
import com.backbase.testing.dataloader.utils.ParserUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.backbase.testing.dataloader.data.CommonConstants.USERS_JSON;

public class Runner {

    public static void main(String[] args) throws IOException {

        BankSetup bankSetup = new BankSetup();
        UsersSetup usersSetup = new UsersSetup();

        List<HashMap<String, List<String>>> userLists = ParserUtil.convertJsonToObject(USERS_JSON, new TypeReference<List<HashMap<String, List<String>>>>() {});

        bankSetup.setupBankWithEntitlementsAdminAndProducts();

        for (Map<String, List<String>> userList : userLists) {
            List<String> externalUserIds = userList.get("users");

            usersSetup.setupUsersWithAllFunctionDataGroupsAndPrivilegesUnderNewLegalEntity(externalUserIds);
        }
    }
}

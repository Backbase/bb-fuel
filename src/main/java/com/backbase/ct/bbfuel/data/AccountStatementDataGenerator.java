package com.backbase.ct.bbfuel.data;

import com.backbase.ct.bbfuel.dto.accountStatement.Categories;
import com.backbase.ct.bbfuel.dto.accountStatement.EstatementPostRequestBody;
import com.backbase.ct.bbfuel.dto.accountStatement.ValidEstatementDocuments;
import org.apache.commons.lang.time.DateUtils;
import java.util.*;
import java.util.stream.IntStream;

import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;

public class AccountStatementDataGenerator {

    public static List<EstatementPostRequestBody> generateAccountStatementsRequests(int quantity, String externalUserId, String arrangementInternalId, String accountName, String accountNumber) {
        List<EstatementPostRequestBody> requestBodies = new ArrayList<>();
        IntStream.range(0, quantity).forEach(randomNumber -> {
            requestBodies.add(
                    new EstatementPostRequestBody()
                            .setAccountId(arrangementInternalId)
                            .setUserId(externalUserId)
                            .setAccountName(accountName)
                            .setAccountNumber(accountNumber)
                            .setDate(DateUtils.addDays(new Date(), (randomNumber * -1)))
                            .setDescription(String.format("%s statement ", accountName))
                            .setCategory(getRandomFromEnumValues(Categories.values()))
                            .setDocuments(Collections.singletonList(getRandomFromEnumValues(ValidEstatementDocuments.values()).geteStatetmentDocument())));
                });
        return requestBodies;
    }
}

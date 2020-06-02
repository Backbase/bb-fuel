package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MAX;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_MESSAGE_TOPICS_MIN;
import static java.util.Collections.singleton;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.messagecenter.MessagesPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.data.MessagesDataGenerator;
import com.backbase.ct.bbfuel.service.LegalEntityService;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagesConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private final LoginRestClient loginRestClient;
    private final MessagesPresentationRestClient messagesPresentationRestClient;
    private final LegalEntityService legalEntityService;
    private final UserPresentationRestClient userPresentationRestClient;

    public void ingestTopics() {

        int howManyTopics = CommonHelpers.generateRandomNumberInRange(
            globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MIN),globalProperties.getInt(PROPERTY_MESSAGE_TOPICS_MAX));

        loginRestClient.loginBankAdmin();

        String bankAdmin = legalEntityService.getRootAdmin();
        String bankAdminInternalId = userPresentationRestClient.getUserByExternalId(bankAdmin).getId();

        IntStream.range(0, howManyTopics).forEach(number -> {
            String topicId = messagesPresentationRestClient.postTopic(MessagesDataGenerator
                .generateTopicPostRequestBody(singleton(bankAdminInternalId)))
                .then()
                .statusCode(SC_OK)
                .extract()
                .path("id");

            log.info("Topic ingested with id [{}] for subscriber [{}]", topicId, bankAdmin);
        });
    }
}

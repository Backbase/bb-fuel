package com.backbase.ct.bbfuel.configurator;

import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.notification.NotificationsPresentationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.NotificationsDataGenerator;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.notification.service.api.v2.model.NotificationsPostRequestBody;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final NotificationsPresentationRestClient notificationsPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;

    /**
     * Create global notifications. Requires also either ingest.approvals.for.notifications=true or disabled approval
     * flow on notifications service.
     *
     * @param externalUserId - external user ID of notifications manager with create and approve permissions
     */
    public void ingestNotifications(String externalUserId) {
        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            NotificationsPostRequestBody notification = NotificationsDataGenerator
                .generateNotificationsPostRequestBodyForGlobalTargetGroup();
            notificationsPresentationRestClient.createNotification(notification)
                .then()
                .statusCode(SC_CREATED);

            log.info("Notification ingested with title [{}] and target group [{}]", notification.getTitle(),
                notification.getTargetGroup());
        });
    }
}

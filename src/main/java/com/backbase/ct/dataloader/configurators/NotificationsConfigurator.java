package com.backbase.ct.dataloader.configurators;

import com.backbase.ct.dataloader.clients.notifications.NotificationsPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.NotificationsDataGenerator;
import com.backbase.ct.dataloader.utils.CommonHelpers;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static org.apache.http.HttpStatus.SC_CREATED;

public class NotificationsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private NotificationsPresentationRestClient notificationsPresentationRestClient = new NotificationsPresentationRestClient();

    public void ingestNotifications() {
        int randomAmount = CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MIN), globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            NotificationsPostRequestBody notification = NotificationsDataGenerator.generateNotificationsPostRequestBodyForGlobalTargetGroup();
            notificationsPresentationRestClient.createNotification(notification)
                    .then()
                    .statusCode(SC_CREATED);

            LOGGER.info("Notification ingested with title [{}] and target group [{}]", notification.getTitle(), notification.getTargetGroup());
        });
    }
}
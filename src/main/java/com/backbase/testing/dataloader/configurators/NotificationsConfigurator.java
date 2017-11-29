package com.backbase.testing.dataloader.configurators;

import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.testing.dataloader.clients.notifications.NotificationsPresentationRestClient;
import com.backbase.testing.dataloader.data.NotificationsDataGenerator;
import com.backbase.testing.dataloader.utils.CommonHelpers;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_NOTIFICATIONS_MAX;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_NOTIFICATIONS_MIN;
import static org.apache.http.HttpStatus.SC_CREATED;

public class NotificationsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private NotificationsPresentationRestClient notificationsPresentationRestClient = new NotificationsPresentationRestClient();
    private NotificationsDataGenerator notificationsDataGenerator = new NotificationsDataGenerator();

    public void ingestNotifications() {
        for (int i = 0; i < CommonHelpers.generateRandomNumberInRange(globalProperties.getInt(PROPERTY_NOTIFICATIONS_MIN), globalProperties.getInt(PROPERTY_NOTIFICATIONS_MAX)); i++) {
            NotificationsPostRequestBody notification = notificationsDataGenerator.generateNotificationsPostRequestBodyForGlobalTargetGroup();
            notificationsPresentationRestClient.createNotification(notification)
                    .then()
                    .statusCode(SC_CREATED);

            LOGGER.info(String.format("Notification ingested with title [%s] and target group [%s]", notification.getTitle(), notification.getTargetGroup()));
        }
    }
}
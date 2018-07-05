package com.backbase.ct.dataloader.configurator;

import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.dataloader.client.notification.NotificationsPresentationRestClient;
import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.data.NotificationsDataGenerator;
import com.backbase.ct.dataloader.util.CommonHelpers;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final NotificationsPresentationRestClient notificationsPresentationRestClient;

    public void ingestNotifications() {
        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_NOTIFICATIONS_MAX));
        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            NotificationsPostRequestBody notification = NotificationsDataGenerator
                .generateNotificationsPostRequestBodyForGlobalTargetGroup();
            notificationsPresentationRestClient.createNotification(notification)
                .then()
                .statusCode(SC_CREATED);

            LOGGER.info("Notification ingested with title [{}] and target group [{}]", notification.getTitle(),
                notification.getTargetGroup());
        });
    }
}

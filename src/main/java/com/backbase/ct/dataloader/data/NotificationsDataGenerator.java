package com.backbase.ct.dataloader.data;

import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.SeverityLevel;
import com.github.javafaker.Faker;
import java.util.Random;

public class NotificationsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();

    public static NotificationsPostRequestBody generateNotificationsPostRequestBodyForGlobalTargetGroup() {
        return new NotificationsPostRequestBody()
            .withLevel(SeverityLevel.values()[random.nextInt(SeverityLevel.values().length)])
            .withMessage(faker.lorem().paragraph() + " {{link}}")
            .withOrigin(faker.lorem().characters(10))
            .withTargetGroup(NotificationsPostRequestBody.TargetGroup.GLOBAL)
            .withTitle(faker.lorem().sentence().replace(".", ""))
            .withLink("http://" + faker.internet().url());
    }
}

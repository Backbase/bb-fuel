package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;

import com.backbase.dbs.notification.service.api.v2.model.NotificationsPostRequestBody;
import com.backbase.dbs.notification.service.api.v2.model.SeverityLevel;
import com.backbase.dbs.notification.service.api.v2.model.TargetGroup;
import com.github.javafaker.Faker;
import java.util.Random;

public class NotificationsDataGenerator {

    private static Faker faker = new Faker();
    private static Random random = new Random();

    public static NotificationsPostRequestBody generateNotificationsPostRequestBodyForGlobalTargetGroup() {
        return new NotificationsPostRequestBody()
            .withLevel(getRandomFromEnumValues(SeverityLevel.values()))
            .withMessage(faker.lorem().paragraph() + " {{link}}")
            .withOrigin(faker.lorem().characters(10))
            .withTargetGroup(TargetGroup.GLOBAL)
            .withTitle(faker.lorem().sentence().replace(".", ""))
            .withLink("http://" + faker.internet().url());
    }
}

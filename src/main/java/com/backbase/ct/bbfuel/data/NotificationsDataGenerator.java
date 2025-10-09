package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;

import com.backbase.dbs.notifications.client.api.v3.model.NotificationsPostRequestBody;
import com.backbase.dbs.notifications.client.api.v3.model.SeverityLevel;
import com.backbase.dbs.notifications.client.api.v3.model.TargetGroup;
import com.github.javafaker.Faker;

public class NotificationsDataGenerator {

    private static Faker faker = new Faker();

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

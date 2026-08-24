package com.ryefry.data.dynamic;

import com.ryefry.enums.RoomType;
import com.ryefry.model.Booking;
import net.datafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

import static com.ryefry.config.ConfigurationManager.configuration;

public final class BookingDataFactory {

    private static final Faker faker = new Faker(new Locale.Builder().setLanguageTag(configuration().faker()).build());
    private static final Logger logger = LogManager.getLogger(BookingDataFactory.class);

    private BookingDataFactory() {
    }

    public static Booking createBookingData() {
        var booking = new Booking(
                faker.internet().emailAddress(),
                returnRandomCountry(),
                faker.credentials().password(),
                returnDailyBudget(),
                faker.bool().bool(),
                faker.options().option(RoomType.class),
                faker.lorem().paragraph());

        logger.info(
                "Booking data generated: email={}, country={}, dailyBudget={}, newsletter={}, roomType={}",
                booking.email(),
                booking.country(),
                booking.dailyBudget(),
                booking.newsletter(),
                booking.roomType());

        return booking;
    }

    private static String returnRandomCountry() {
        return faker.options().option("Belgium", "Brazil", "Netherlands");
    }

    private static String returnDailyBudget() {
        return faker.options().option("$100", "$100 - $499", "$499 - $999", "$999+");
    }
}

package edu.eflerrr.scrapper.configuration;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeConstants {

    public final static OffsetDateTime MIN_DATE_TIME =
        OffsetDateTime.now()
            .withOffsetSameInstant(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MICROS);

}

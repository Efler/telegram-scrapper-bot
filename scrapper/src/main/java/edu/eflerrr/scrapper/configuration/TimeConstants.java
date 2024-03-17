package edu.eflerrr.scrapper.configuration;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeConstants {

    public final static OffsetDateTime MIN_DATE_TIME =
        OffsetDateTime.parse("0001-01-01T00:00:00Z");

}

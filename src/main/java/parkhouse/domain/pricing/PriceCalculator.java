package parkhouse.domain.pricing;

import java.time.Duration;
import java.time.LocalDateTime;

public class PriceCalculator {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int FULL_PRICE_BPS = 10_000;

    public int calculateCents(Tariff tariff, LocalDateTime entry, LocalDateTime until) {

        long minutesParked = Duration.between(entry, until).toMinutes();

        if (minutesParked <= tariff.graceMinutes()) {
            return 0;
        }

        //rounds up to the next started hour
        long billableHours = (minutesParked + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR;
        int totalCents = 0;

        for (long hour = 0; hour < billableHours; hour++) {
            var blockStart = entry.plusHours(hour);

            int rateCents = tariff.rateAtCents(blockStart);
            int discountBps = tariff.discountBpsFor((int) (hour + 1));

            //adding half the divisor rounds to the nearest cent without floating point
            totalCents += (rateCents * (FULL_PRICE_BPS - discountBps) + FULL_PRICE_BPS / 2) / FULL_PRICE_BPS;
        }

        return totalCents;
    }
}
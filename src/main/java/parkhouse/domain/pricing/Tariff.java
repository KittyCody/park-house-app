package parkhouse.domain.pricing;

import java.time.LocalDateTime;
import java.util.List;

public record Tariff(int pricePerHourCents,
                     int nightRateCents,
                     int nightStartHour,
                     int nightEndHour,
                     int graceMinutes,
                     List<TariffTier> tiers) {

    public static Tariff createDefault() {
        return new Tariff(300, 150, 20, 8, 10,
                List.of(new TariffTier(1, 0),
                        new TariffTier(3, 1500),
                        new TariffTier(8, 3000)));
    }

    public int rateAtCents(LocalDateTime moment) {
        return isNight(moment.getHour()) ? nightRateCents : pricePerHourCents;
    }

    public boolean isNight(int hour) {
        if (nightStartHour < nightEndHour) {
            return hour >= nightStartHour && hour < nightEndHour;
        }

        return hour >= nightStartHour || hour < nightEndHour;   // window wraps past midnight
    }

    public int discountBpsFor(int cumulativeHour) {
        int discountBps = 0;
        int matchedFromHour = 0;

        for (var tier : tiers) {
            if (tier.fromHour() <= cumulativeHour && tier.fromHour() >= matchedFromHour) {
                matchedFromHour = tier.fromHour();
                discountBps = tier.discountBps();
            }
        }

        return discountBps;
    }
}
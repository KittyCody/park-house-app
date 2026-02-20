package parkhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.Floor;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.error.InvalidOperationalHours;
import parkhouse.domain.error.NotEnoughSpaces;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TicketServiceIntegrationTest.FixedClockConfig.class)
class TicketServiceIntegrationTest {

    @Autowired
    TicketService ticketService;

    @Autowired
    FloorRepository floors;

    @Autowired
    TicketRepository tickets;

    @Autowired
    ParkingSettingsRepository parkingSettings;

    @Autowired
    Clock clock;

    @BeforeEach
    void cleanDb() {
        tickets.deleteAll();
        floors.deleteAll();
        parkingSettings.deleteAll();
    }

    @Test
    void createEntry_persistsTicket_inRealDatabase() {
        var floor = floors.save(new Floor(200));
        parkingSettings.save(new ParkingSettings(0, 23, 3));

        UUID gateId = UUID.randomUUID();

        var ticket = ticketService.createEntry(gateId, floor.getId());

        var fromDb = tickets.findById(ticket.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getEntryGateId()).isEqualTo(gateId);
        assertThat(fromDb.get().getFloorId()).isEqualTo(floor.getId());
    }

    @Test
    void createEntry_throwsInvalidOperationalHours_whenClosed() {
        floors.save(new Floor(200));
        parkingSettings.save(new ParkingSettings(8, 22, 3));

        UUID gateId = UUID.randomUUID();
        int floorId = 1;

        assertThatThrownBy(() -> ticketService.createEntry(gateId, floorId))
                .isInstanceOf(InvalidOperationalHours.class);
    }

    @Test
    void createEntry_throwsNotEnoughSpaces_whenNoCapacityLeft() {
        var floor = floors.save(new Floor(1));
        parkingSettings.save(new ParkingSettings(0, 23, 3));

        UUID gateId1 = UUID.randomUUID();
        UUID gateId2 = UUID.randomUUID();

        int floorId = floor.getId();

        ticketService.createEntry(gateId1, floorId);

        assertThat(ticketService.getAvailableSpaces()).isZero();

        assertThatThrownBy(() -> ticketService.createEntry(gateId2, floorId))
                .isInstanceOf(NotEnoughSpaces.class);
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        public Clock testClock() {
            LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 3, 0);
            ZoneId zone = ZoneOffset.UTC;
            return Clock.fixed(fixedDateTime.toInstant(zone.getRules().getOffset(fixedDateTime)), zone);
        }
    }
}

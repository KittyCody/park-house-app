package parkhouse.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.Floor;
import parkhouse.domain.ParkingSettings;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ParkingJourneyTest.FixedClockConfig.class)
class ParkingJourneyTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    FloorRepository floors;
    @Autowired
    TicketRepository tickets;
    @Autowired
    ParkingSettingsRepository parkingSettings;

    private int floorId;

    @BeforeEach
    void setup() {
        tickets.deleteAll();
        floors.deleteAll();
        parkingSettings.deleteAll();

        floorId = floors.save(new Floor(50)).getId();
        parkingSettings.save(new ParkingSettings(0, 23, 3));
    }

    @Test
    void full_journey_entry_payment_exit() throws Exception {
        UUID entryGateId = UUID.randomUUID();
        UUID exitGateId = UUID.randomUUID();

        // 1. Entry machine creates a ticket
        String entryBody = mvc.perform(post("/api/v1/tickets/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"floorId": %d}
                                """.formatted(floorId))
                        .with(jwt()
                                .jwt(j -> j.subject(entryGateId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTRY_GATE_MACHINE"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID ticketId = UUID.fromString(json.readTree(entryBody).get("id").textValue());

        // 2. Exit machine checks status → payment required
        mvc.perform(get("/api/v1/tickets/{id}/status", ticketId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXIT_GATE_MACHINE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_REQUIRED"));

        // 3. Exit machine processes payment
        mvc.perform(post("/api/v1/tickets/{id}/pay", ticketId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXIT_GATE_MACHINE"))))
                .andExpect(status().isNoContent());

        // 4. Exit machine opens barrier
        mvc.perform(post("/api/v1/tickets/{id}/exit", ticketId)
                        .with(jwt()
                                .jwt(j -> j.subject(exitGateId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_EXIT_GATE_MACHINE"))))
                .andExpect(status().isNoContent());

        // 5. Status is now PAID
        mvc.perform(get("/api/v1/tickets/{id}/status", ticketId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXIT_GATE_MACHINE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVALID"));
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        public Clock testClock() {
            LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0);
            ZoneId zone = ZoneOffset.UTC;
            return Clock.fixed(fixedTime.toInstant(zone.getRules().getOffset(fixedTime)), zone);
        }
    }
}
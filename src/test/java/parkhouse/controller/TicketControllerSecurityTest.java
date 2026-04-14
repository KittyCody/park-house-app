package parkhouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import parkhouse.service.TicketService;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@ActiveProfiles("test")
@Import(TicketControllerSecurityTest.MethodSecurityConfig.class)
class TicketControllerSecurityTest {

    private static final UUID TICKET_ID = UUID.randomUUID();
    @Autowired
    MockMvc mvc;

    @MockitoBean
    TicketService tickets;

    @Test
    void entryMachine_cannotGetPrice() throws Exception {
        mvc.perform(get("/api/v1/tickets/{id}/price", TICKET_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENTRY_GATE_MACHINE"))))
                .andExpect(status().isForbidden());
    }

    // --- ENTRY_GATE_MACHINE cannot call EXIT routes ---

    @Test
    void entryMachine_cannotPay() throws Exception {
        mvc.perform(post("/api/v1/tickets/{id}/pay", TICKET_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENTRY_GATE_MACHINE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void entryMachine_cannotExit() throws Exception {
        mvc.perform(post("/api/v1/tickets/{id}/exit", TICKET_ID)
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTRY_GATE_MACHINE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void entryMachine_cannotGetStatus() throws Exception {
        mvc.perform(get("/api/v1/tickets/{id}/status", TICKET_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENTRY_GATE_MACHINE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void exitMachine_cannotCreateEntry() throws Exception {
        mvc.perform(post("/api/v1/tickets/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"floorId": 1}
                                """)
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_EXIT_GATE_MACHINE"))))
                .andExpect(status().isForbidden());
    }

    // --- EXIT_GATE_MACHINE cannot call ENTRY routes ---

    @Test
    void admin_cannotCreateEntry() throws Exception {
        mvc.perform(post("/api/v1/tickets/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"floorId": 1}
                                """)
                        .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    // --- ADMIN cannot call machine routes ---

    @Test
    void admin_cannotGetPrice() throws Exception {
        mvc.perform(get("/api/v1/tickets/{id}/price", TICKET_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_cannotCreateEntry() throws Exception {
        mvc.perform(post("/api/v1/tickets/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"floorId": 1}
                                """))
                .andExpect(status().is4xxClientError());
    }

    // --- No authentication : 4xx error ---

    @Test
    void unauthenticated_cannotGetPrice() throws Exception {
        mvc.perform(get("/api/v1/tickets/{id}/price", TICKET_ID))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }
}
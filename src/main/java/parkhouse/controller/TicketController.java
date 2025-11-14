package parkhouse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import parkhouse.dto.TicketResponse;
import parkhouse.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService tickets;

    public TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @PostMapping("/entries")
    @PreAuthorize("hasRole('ENTRY_GATE_MACHINE')")
    public ResponseEntity<TicketResponse> createEntry(@AuthenticationPrincipal Jwt token,
                                                      UriComponentsBuilder uri) {
        var gateId = UUID.fromString(token.getSubject());

        var created = tickets.createEntry(gateId);
        var location = uri.path("/api/v1/tickets/{id}").buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(TicketResponse.of(created));
    }
}

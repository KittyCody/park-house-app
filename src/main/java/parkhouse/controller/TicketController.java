package parkhouse.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import parkhouse.dto.TicketResponse;
import parkhouse.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService tickets;

    public TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @PostMapping("/entries")
    @PreAuthorize("hasRole('ENTRY_GATE_MACHINE')")
    public ResponseEntity<TicketResponse> createEntry(@AuthenticationPrincipal Jwt token,
                                                      UriComponentsBuilder uri) {

        var gateId = UUID.fromString(token.getSubject());

        log.info("Ticket entry request from gate {}", gateId);

        var created = tickets.createEntry(gateId);
        var location = uri.path("/api/v1/tickets/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        log.info("Ticket created successfully: id={}, gate={}", created.getId(), gateId);

        return ResponseEntity.created(location).body(TicketResponse.of(created));
    }

    @GetMapping("/{id}/price")
    @PreAuthorize("hasRole('EXIT_GATE_MACHINE')")
    public ResponseEntity<Integer> getPrice(@PathVariable UUID id) {

        log.info("Price calculation request for ticket {}", id);

        int price = tickets.calculatePrice(id);

        return ResponseEntity.ok(price);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('EXIT_GATE_MACHINE')")
    public ResponseEntity<Void> pay(@PathVariable UUID id) {

        log.info("Payment requested for ticket {}", id);

        tickets.pay(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/exit")
    @PreAuthorize("hasRole('EXIT_GATE_MACHINE')")
    public ResponseEntity<Void> exit(@AuthenticationPrincipal Jwt token,
                                     @PathVariable UUID id) {

        var gateId = UUID.fromString(token.getSubject());

        log.info("Exit requested for ticket {} at gate {}", id, gateId);

        tickets.exit(id, gateId);

        return ResponseEntity.noContent().build();
    }


}

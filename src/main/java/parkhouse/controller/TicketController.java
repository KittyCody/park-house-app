package parkhouse.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import parkhouse.dto.EntryRequest;
import parkhouse.dto.TicketResponse;
import parkhouse.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService tickets;

    public TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @PostMapping("/entries")
    public ResponseEntity<TicketResponse> createEntry(@RequestBody @Valid EntryRequest body,
        UriComponentsBuilder uri) {

        var created = tickets.createEntry(body.entryGateId());
        var location = uri.path("/api/tickets/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(TicketResponse.of(created));
    }
}

package parkhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Ticket;
import parkhouse.domain.error.InvalidOperationalHours;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    TicketRepository tickets;
    @Mock
    FloorRepository floors;
    @Mock
    ParkingSettingsRepository parkingSettings;

    Clock clock;
    TicketService ticketService;

    @BeforeEach
    public void setup() {
        clock = Clock.fixed(LocalDateTime.of(2025, 1, 1, 10, 0)
                .toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        ticketService = new TicketService(tickets, floors, parkingSettings, clock);

        when(parkingSettings.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(new ParkingSettings(8, 22)));
        lenient().when(floors.sumCapacity()).thenReturn(Optional.of(100L));
        lenient().when(tickets.countAllByTimeOfExitIsNull()).thenReturn(10);
    }

    @Test
    void createEntry_succeeds_when_open_and_capacity_available() {
        UUID gateId = UUID.randomUUID();

        //make repository save return the same Ticket instance it received
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        when(tickets.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket saved = ticketService.createEntry(gateId);

        verify(tickets).save(captor.capture());
        Ticket toSave = captor.getValue();

        assertThat(saved).isSameAs(toSave);
        assertThat(saved.getEntryGateId()).isEqualTo(gateId);
        assertThat(saved.getTimeOfEntry()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));

        verify(floors).sumCapacity();
        verify(tickets).countAllByTimeOfExitIsNull();
        verify(parkingSettings).findTopByOrderByIdDesc();
    }

    @Test
    void createEntry_throws_when_outside_operational_hours() {
        Clock nightClock = Clock.fixed(LocalDateTime.of(2025, 1, 1, 23, 0)
                .toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        TicketService closedService =
                new TicketService(tickets, floors, parkingSettings, nightClock);

        when(parkingSettings.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(new ParkingSettings(8, 22)));

        assertThatThrownBy(() -> closedService.createEntry(UUID.randomUUID()))
                .isInstanceOf(InvalidOperationalHours.class);

        verify(tickets, never()).save(any());
    }
}

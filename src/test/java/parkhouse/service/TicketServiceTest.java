package parkhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.Floor;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Ticket;
import parkhouse.domain.error.InvalidOperationalHours;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
                .thenReturn(Optional.of(new ParkingSettings(8, 22, 3)));
        lenient().when(floors.sumCapacity()).thenReturn(Optional.of(100L));
        lenient().when(tickets.countAllByTimeOfExitIsNull()).thenReturn(10);
    }

    @Test
    void createEntry_succeeds_when_open_and_capacity_available() {
        UUID gateId = UUID.randomUUID();
        int floorId = 1;

        Floor floor = mock(Floor.class);
        when(floor.getId()).thenReturn(floorId);
        when(floors.findById(floorId)).thenReturn(Optional.of(floor));

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        when(tickets.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket saved = ticketService.createEntry(gateId, floorId);

        verify(tickets).save(captor.capture());
        Ticket toSave = captor.getValue();

        assertThat(saved).isSameAs(toSave);
        assertThat(saved.getEntryGateId()).isEqualTo(gateId);
        assertThat(saved.getTimeOfEntry()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));

        assertThat(saved.getFloorId()).isEqualTo(floorId);
        
        verify(floors).findById(floorId);
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
                .thenReturn(Optional.of(new ParkingSettings(8, 22, 3)));

        assertThatThrownBy(() -> closedService.createEntry(UUID.randomUUID(), 1))
                .isInstanceOf(InvalidOperationalHours.class);

        verify(tickets, never()).save(any());
        verify(floors, never()).findById(anyInt());
    }
}

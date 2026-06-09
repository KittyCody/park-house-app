package parkhouse.dto;

import parkhouse.domain.Floor;

public record FloorResponse(int id, int capacity, int parked, int available) {

    public static FloorResponse of(Floor floor, int parked) {
        int available = Math.max(0, floor.getCapacity() - parked);
        return new FloorResponse(floor.getId(), floor.getCapacity(), parked, available);
    }
}
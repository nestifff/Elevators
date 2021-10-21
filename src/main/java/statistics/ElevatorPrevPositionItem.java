package statistics;

import building.passenger.Passenger;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ElevatorPrevPositionItem {

    private int floor = 1;
    private Set<Passenger> passengers = new HashSet<>();

}

package statistics;

import building.passenger.Passenger;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ElevatorPrevPositionItemTest {

    @Test
    public void createSetGet_allRight() {
        ElevatorPrevPositionItem item = new ElevatorPrevPositionItem();
        Passenger passenger = mock(Passenger.class);

        item.setPassengers(Set.of(passenger));
        item.setFloor(3);

        assertTrue(item.getPassengers().contains(passenger));
        assertEquals(item.getFloor(), 3);
    }

}
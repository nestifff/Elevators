package building.floor;

import building.passenger.Passenger;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FloorTest {

    @Test
    public void create_allRight() {
        Floor floor = new Floor(2, new LinkedBlockingDeque<>());
        assertEquals(floor.getNumber(), 2);
        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void passengerGenerated_allRight() {
        Floor floor = new Floor(2, new LinkedBlockingDeque<>());
        Passenger passenger = mock(Passenger.class);

        floor.passengerGenerated(passenger);

        assertTrue(floor.getPassengers().contains(passenger));
        verify(passenger).getClickDesiredButton();
    }

    @Test
    public void passengerGenerated_twoPassengers() {
        Floor floor = new Floor(2, new LinkedBlockingDeque<>());
        Passenger passenger1 = mock(Passenger.class);
        Passenger passenger2 = mock(Passenger.class);

        floor.passengerGenerated(passenger1);
        floor.passengerGenerated(passenger2);

        assertTrue(floor.getPassengers().contains(passenger1));
        assertTrue(floor.getPassengers().contains(passenger2));
        assertEquals(floor.getPassengers().size(), 2);
        verify(passenger1).getClickDesiredButton();
        verify(passenger2).getClickDesiredButton();
    }

    @Test
    public void passengerGenerated_passengerNull() {
        Floor floor = new Floor(2, new LinkedBlockingDeque<>());
        assertThrows(NullPointerException.class, () -> floor.passengerGenerated(null));
    }

    @Test
    public void elevatorArrived_turnOffButtonsInvoked() {
        Floor floor = mock(Floor.class, CALLS_REAL_METHODS);
        Elevator elevator = mock(Elevator.class);
        assertThrows(NullPointerException.class, () -> floor.elevatorArrived(elevator));
        verify(floor).turnOffButtons();
    }


    @Test
    public void elevatorArrived_floorPassengersEmpty() {
        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(false);

        floor.elevatorArrived(elevator);

        verify(elevator, never()).doesPassengerFit(any());
        verify(elevator, never()).putPassenger(any());
    }

    @Test
    public void elevatorArrived_elevatorPassengersEmpty() {
        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(false);

        floor.elevatorArrived(elevator);

        verify(elevator).getFloorToPassengersDrop();
        verify(elevatorsPassengers).containsKey(1);
        verify(elevatorsPassengers, never()).get(1);
        verify(elevatorsPassengers, never()).remove(1);
    }

    @Test
    public void elevatorArrived_elevatorHasPassengersToDrop() {

        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Passenger elevatorsPassenger = mock(Passenger.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);
        Set<Passenger> elevatorsPassengersToDrop = Set.of(elevatorsPassenger);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(true);
        when(elevatorsPassengers.get(1)).thenReturn(elevatorsPassengersToDrop);

        floor.elevatorArrived(elevator);

        verify(elevator, times(3)).getFloorToPassengersDrop();
        verify(elevatorsPassengers).containsKey(1);
        verify(elevatorsPassengers).get(1);
        verify(elevator).dropPassenger(elevatorsPassenger);
        verify(elevatorsPassengers).remove(1);
    }

    @Test
    public void elevatorArrived_floorHasPassengersToPut() {

        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);
        Passenger floorPassenger = mock(Passenger.class);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(false);
        when(elevator.doesPassengerFit(floorPassenger)).thenReturn(true);
        when(floorPassenger.checkWillSitOnElevator(elevator)).thenReturn(true);

        floor.passengerGenerated(floorPassenger);
        floor.elevatorArrived(elevator);

        verify(elevator).doesPassengerFit(floorPassenger);
        verify(floorPassenger).checkWillSitOnElevator(elevator);
        verify(elevator).putPassenger(floorPassenger);
        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void elevatorArrived_floorHasPassengersToPutAndThenRemains_clickButtons() {

        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);
        Passenger floorPassenger1 = mock(Passenger.class);
        Passenger floorPassenger2 = mock(Passenger.class);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(false);
        when(elevator.doesPassengerFit(floorPassenger1)).thenReturn(true);
        when(elevator.doesPassengerFit(floorPassenger2)).thenReturn(false);
        when(floorPassenger1.checkWillSitOnElevator(elevator)).thenReturn(true);

        floor.passengerGenerated(floorPassenger1);
        floor.passengerGenerated(floorPassenger2);
        floor.elevatorArrived(elevator);

        verify(elevator).doesPassengerFit(floorPassenger1);
        verify(elevator).doesPassengerFit(floorPassenger2);
        verify(floorPassenger1).checkWillSitOnElevator(elevator);
        verify(floorPassenger2, never()).checkWillSitOnElevator(any());
        verify(elevator).putPassenger(floorPassenger1);
        verify(elevator, never()).putPassenger(floorPassenger2);

        verify(floorPassenger1).getClickDesiredButton();
        verify(floorPassenger2, times(2)).getClickDesiredButton();

        assertEquals(floor.getPassengers().size(), 1);
        assertTrue(floor.getPassengers().contains(floorPassenger2));
    }

    @Test
    public void elevatorArrived_floorPassengersEmptyToPutThenRemains_clickButtons() {

        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);
        Passenger floorPassenger1 = mock(Passenger.class);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(false);
        when(elevator.doesPassengerFit(floorPassenger1)).thenReturn(false);

        floor.passengerGenerated(floorPassenger1);
        floor.elevatorArrived(elevator);

        verify(elevator).doesPassengerFit(floorPassenger1);
        verify(floorPassenger1, never()).checkWillSitOnElevator(any());
        verify(elevator, never()).putPassenger(floorPassenger1);
        verify(floorPassenger1, times(2)).getClickDesiredButton();

        assertEquals(floor.getPassengers().size(), 1);
        assertTrue(floor.getPassengers().contains(floorPassenger1));
    }

    @Test
    public void elevatorArrived_floorHasPassengersToPut_elevatorHasPassengersToDrop() {

        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        Elevator elevator = mock(Elevator.class);
        Passenger elevatorsPassenger = mock(Passenger.class);
        Passenger floorPassenger = mock(Passenger.class);
        Map<Integer, Set<Passenger>> elevatorsPassengers = mock(Map.class);
        Set<Passenger> elevatorsPassengersToDrop = Set.of(elevatorsPassenger);

        when(elevator.getFloorToPassengersDrop()).thenReturn(elevatorsPassengers);
        when(elevatorsPassengers.containsKey(1)).thenReturn(true);
        when(elevatorsPassengers.get(1)).thenReturn(elevatorsPassengersToDrop);
        when(elevator.doesPassengerFit(floorPassenger)).thenReturn(true);
        when(floorPassenger.checkWillSitOnElevator(elevator)).thenReturn(true);

        floor.passengerGenerated(floorPassenger);
        floor.elevatorArrived(elevator);

        verify(elevator, times(3)).getFloorToPassengersDrop();
        verify(elevatorsPassengers).containsKey(1);
        verify(elevatorsPassengers).get(1);
        verify(elevator).dropPassenger(elevatorsPassenger);
        verify(elevatorsPassengers).remove(1);

        verify(elevator).doesPassengerFit(floorPassenger);
        verify(elevator).putPassenger(floorPassenger);

        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void elevatorArrived_null() {
        Floor floor = new Floor(1, new LinkedBlockingDeque<>());
        assertThrows(NullPointerException.class, () -> floor.elevatorArrived(null));
    }

    @SneakyThrows
    @Test
    public void sendInQueueToStop_allRight() {
        BlockingQueue<ButtonOnClickData> queue = mock(BlockingQueue.class);
        Floor floor = new Floor(1, queue);
        ButtonOnClickData buttonOnClickData = mock(ButtonOnClickData.class);

        floor.sendInQueueToStop(buttonOnClickData);

        verify(queue).put(buttonOnClickData);
    }

    @Test
    public void equalsHashCode_true() {
        Floor floor1 = new Floor(1, new LinkedBlockingDeque<>());
        Floor floor2 = new Floor(1, new LinkedBlockingDeque<>());

        assertNotEquals(floor2, floor1);
        assertNotSame(floor1.hashCode(), floor2.hashCode());
    }

    @Test
    public void equalsHashCode_same() {
        Floor floor1 = new Floor(1, new LinkedBlockingDeque<>());

        assertEquals(floor1, floor1);
        assertEquals(floor1.hashCode(), floor1.hashCode());
    }

    @Test
    public void equalsHashCode_false() {
        Floor floor1 = new Floor(1, new LinkedBlockingDeque<>());
        Floor floor2 = new Floor(2, new LinkedBlockingDeque<>());

        assertNotEquals(floor2, floor1);
        assertNotSame(floor1.hashCode(), floor2.hashCode());
    }

    @Test
    public void equalsHashCode_falseSecondNotFloor() {
        Floor floor1 = new Floor(1, new LinkedBlockingDeque<>());
        Object obj = new Object();

        assertNotEquals(obj, floor1);
        assertNotSame(floor1.hashCode(), obj.hashCode());
    }

    @Test
    public void equals_falseSecondNull() {
        Floor floor1 = new Floor(1, new LinkedBlockingDeque<>());

        assertNotEquals(null, floor1);
    }


}
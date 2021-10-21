package elevators.elevator;

import building.floor.Floor;
import building.passenger.Passenger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static constants.GlobalApplicationConstants.*;
import static elevators.elevator.ElevatorCurrentState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static utils.ElevatorsCreator.anyElevator;

class ElevatorTest {

    @Test
    public void create_allRight() {

        Floor floor = mock(Floor.class);
        when(floor.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(mock(Floor.class));
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);

        assertSame(elevator.getCurrentState(), EMPTY_STAND);
        assertTrue(elevator.getButtonPressedDown().isEmpty());
        assertTrue(elevator.getButtonPressedUp().isEmpty());
        assertEquals(elevator.getNextDestination(), 0);
        assertEquals(elevator.getElevatorId(), 1);
        assertTrue(elevator.getCalls().isEmpty());
        assertTrue(elevator.isWithoutPassengers());
        assertEquals(elevator.getCurrFloorNum(), 1);
        assertEquals(LIFTING_CAPACITY, elevator.getRemainingCapacity());
        assertEquals(SPEED_ONE_FLOOR, elevator.getSpeedOneFloor());
        assertEquals(TIME_OPEN_DOORS + TIME_CLOSE_DOORS, elevator.getTimeToStopOnFloor());
        assertEquals(elevator.getFloorToPassengersDrop().size(), 0);
    }

    @Test
    public void addCall_allRight() {
        Elevator elevator = anyElevator();
        elevator.addCall(1);

        assertTrue(elevator.getCalls().contains(1));
        assertEquals(elevator.getCalls().size(), 1);
    }

    @Test
    public void removeCall_allRight() {
        Elevator elevator = anyElevator();
        elevator.addCall(1);
        elevator.removeCall(1);

        assertFalse(elevator.getCalls().contains(1));
        assertEquals(elevator.getCalls().size(), 0);
    }

    @Test
    public void removeCall_wasNotContained() {
        Elevator elevator = anyElevator();
        elevator.removeCall(1);

        assertFalse(elevator.getCalls().contains(1));
        assertEquals(elevator.getCalls().size(), 0);
    }

    @Test
    public void setCurrentState_allRight() {
        Elevator elevator = anyElevator();
        elevator.setCurrentState(GOING_UP);
        assertSame(elevator.getCurrentState(), GOING_UP);
    }

    @SneakyThrows
    @Test
    public void arrivedOnFloor_allRight() {

        Floor floor = mock(Floor.class);
        when(floor.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);

        elevator.arrivedOnFloor(1);

        verify(floor).elevatorArrived(elevator);
        verify(queue).put(elevator);
    }

    @Test
    public void arrivedOnFloor_floorNotExist() {

        Floor floor = mock(Floor.class);
        when(floor.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);

        assertThrows(NullPointerException.class, () -> elevator.arrivedOnFloor(10));
    }

    @Test
    public void doesPassengerFit_true() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY);
        assertTrue(elevator.doesPassengerFit(passenger));
    }

    @Test
    public void doesPassengerFit_false() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY + 1);
        assertFalse(elevator.doesPassengerFit(passenger));
    }

    @Test
    public void putPassenger_passengerClickUp() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY - 100);
        when(passenger.getStartFloor()).thenReturn(1);
        when(passenger.getRequiredFloor()).thenReturn(2);

        elevator.putPassenger(passenger);

        assertEquals(elevator.getRemainingCapacity(), 100);
        assertEquals(elevator.getFloorToPassengersDrop().size(), 1);
        assertTrue(elevator.getFloorToPassengersDrop().get(2).contains(passenger));
        verify(passenger, times(3)).getRequiredFloor();
        assertTrue(elevator.getButtonPressedUp().contains(2));
        assertEquals(elevator.getButtonPressedDown().size(), 0);
    }

    @Test
    public void putPassenger_passengerClickDown() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY - 100);
        when(passenger.getStartFloor()).thenReturn(2);
        when(passenger.getRequiredFloor()).thenReturn(1);

        elevator.putPassenger(passenger);

        assertEquals(elevator.getRemainingCapacity(), 100);
        assertEquals(elevator.getFloorToPassengersDrop().size(), 1);
        assertTrue(elevator.getFloorToPassengersDrop().get(1).contains(passenger));
        verify(passenger, times(3)).getRequiredFloor();
        assertTrue(elevator.getButtonPressedDown().contains(1));
        assertEquals(elevator.getButtonPressedUp().size(), 0);
    }

    @Test
    public void putPassenger_notFit() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY + 100);

        assertThrows(IllegalArgumentException.class, () -> elevator.putPassenger(passenger));
    }

    @Test
    public void dropPassenger_afterPut() {
        Elevator elevator = anyElevator();
        Passenger passenger = mock(Passenger.class);
        when(passenger.getWeight()).thenReturn(LIFTING_CAPACITY - 100);
        when(passenger.getStartFloor()).thenReturn(2);
        when(passenger.getRequiredFloor()).thenReturn(1);

        elevator.putPassenger(passenger);
        elevator.dropPassenger(passenger);

        assertEquals(elevator.getRemainingCapacity(), LIFTING_CAPACITY);
    }

    @SneakyThrows
    @Test
    public void run_allRight() {
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        when(floor1.getNumber()).thenReturn(1);
        when(floor2.getNumber()).thenReturn(2);
        List<Floor> floors = List.of(floor1, floor2);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);

        elevator.start();
        elevator.setCurrentState(GOING_UP);
        elevator.setNextDestination(2);

        Thread.sleep(SPEED_ONE_FLOOR + 100);

        assertEquals(elevator.getCurrFloorNum(), 2);

        Thread.sleep(elevator.getTimeToStopOnFloor() + 100);

        verify(floor2).elevatorArrived(elevator);
        verify(queue).put(elevator);

        elevator.setCurrentState(GOING_DOWN);
        elevator.setNextDestination(1);

        Thread.sleep(SPEED_ONE_FLOOR);

        elevator.setNextDestination(NEXT_DESTINATION_TO_STOP_ELEVATORS);
        elevator.join();

        verify(queue).put(elevatorFlorToStop);
    }

    @Test
    public void equalsHashCode_false() {
        Floor floor1 = mock(Floor.class);
        when(floor1.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor1);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator1 = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);
        Elevator elevator2 = new Elevator(2, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);

        assertNotEquals(elevator2, elevator1);
        assertFalse(elevator1.hashCode() == elevator2.hashCode());
    }

    @Test
    public void equalsHashCode_true() {
        Floor floor1 = mock(Floor.class);
        when(floor1.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor1);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        Elevator elevator1 = new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);
        Elevator elevator2 = elevator1;

        assertEquals(elevator2, elevator1);
        assertEquals(elevator2.hashCode(), elevator1.hashCode());
    }

}
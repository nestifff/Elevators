package elevators.controller.commonController;

import building.floor.Floor;
import building.passenger.Passenger;
import elevators.elevator.Elevator;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static elevators.elevator.ElevatorCurrentState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElevatorsControllerUtilsTest {

    private static final ElevatorsControllerUtils utils = new ElevatorsControllerUtils();

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_emptyStand_needUp() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_STAND);
        assertTrue(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_UP, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_emptyStand_needDown() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_STAND);
        assertTrue(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_DOWN, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_emptyGo_needDown() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_DOWN, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_emptyGo_needUp() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_UP, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goUp_needUpUp() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        assertTrue(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_UP, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goUp_needUpDown() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_DOWN, 10));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goUp_needDown() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_UP, 2));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goDown_needDownDown() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getCurrFloorNum()).thenReturn(10);
        assertTrue(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_DOWN, 8));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goDown_needDownUp() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getCurrFloorNum()).thenReturn(10);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_UP, 8));
    }

    @Test
    public void checkDirectionAndCurrFloorSuitToTakeCall_goDown_needUp() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getCurrFloorNum()).thenReturn(5);
        assertFalse(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator, GOING_DOWN, 8));
    }

    @Test
    public void isFloorQueueEmpty_true() {

        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        when(floor1.getNumber()).thenReturn(1);
        when(floor2.getNumber()).thenReturn(2);
        when(floor2.getPassengers()).thenReturn(new LinkedList<>());

        boolean isEmpty = utils.isFloorQueueEmpty(floors, 2);

        assertTrue(isEmpty);
        verify(floor1).getNumber();
        verify(floor2).getNumber();
        verify(floor1, never()).getPassengers();
        verify(floor2).getPassengers();
    }

    @Test
    public void isFloorQueueEmpty_false() {

        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        when(floor1.getNumber()).thenReturn(1);
        when(floor2.getNumber()).thenReturn(2);
        when(floor2.getPassengers()).thenReturn(new LinkedList<>(List.of(mock(Passenger.class))));

        boolean isEmpty = utils.isFloorQueueEmpty(floors, 2);

        assertFalse(isEmpty);
        verify(floor1).getNumber();
        verify(floor2).getNumber();
        verify(floor1, never()).getPassengers();
        verify(floor2).getPassengers();
    }

    @Test
    public void isFloorQueueEmpty_notExist() {
        Floor floor1 = mock(Floor.class);
        List<Floor> floors = List.of(floor1);
        when(floor1.getNumber()).thenReturn(1);
        assertThrows(NullPointerException.class, () -> utils.isFloorQueueEmpty(floors, 2));
    }

    @Test
    public void getTimeToFloor_emptyGoToCall() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        assertThrows(IllegalArgumentException.class, () -> utils.getTimeToFloor(elevator, 2));
    }

    @Test
    public void getTimeToFloor_emptyStand_alreadyHere() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_STAND);
        when(elevator.getCurrFloorNum()).thenReturn(2);

        double time = utils.getTimeToFloor(elevator, 2);
        assertEquals(time, 0);
    }

    @Test
    public void getTimeToFloor_emptyStand() {
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_STAND);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        when(elevator.getSpeedOneFloor()).thenReturn((long) 200);

        double time = utils.getTimeToFloor(elevator, 2);
        assertEquals(time, 400);
    }

    @Test
    public void getTimeToFloor_goUp_hasStopsBetween() {
        Elevator elevator = mock(Elevator.class);

        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getCurrFloorNum()).thenReturn(2);
        when(elevator.getSpeedOneFloor()).thenReturn((long) 200);
        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(1, 4, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>());
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>());
        when(elevator.getTimeToStopOnFloor()).thenReturn((long) 1000);

        double time = utils.getTimeToFloor(elevator, 10);

        assertEquals(time, 3600);
    }

    @Test
    public void getTimeToFloor_goDown_hasStopsBetweenAndButtonsUpDown() {
        Elevator elevator = mock(Elevator.class);

        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getCurrFloorNum()).thenReturn(10);
        when(elevator.getSpeedOneFloor()).thenReturn((long) 200);
        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(15, 12, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(1, 4, 8)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(16, 20, 21, 22)));
        when(elevator.getTimeToStopOnFloor()).thenReturn((long) 1000);

        double time = utils.getTimeToFloor(elevator, 2);

        assertEquals(time, 3600);
    }

    @Test
    public void getTimeToFloor_goUp_alreadyHere() {
        Elevator elevator = mock(Elevator.class);

        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getCurrFloorNum()).thenReturn(2);
        when(elevator.getSpeedOneFloor()).thenReturn((long) 200);
        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(1, 4, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>());
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>());
        when(elevator.getTimeToStopOnFloor()).thenReturn((long) 1000);

        long time = utils.getTimeToFloor(elevator, 2);
        assertEquals(time, 0);
    }


}
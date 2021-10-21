package elevators.controller.commonController;

import elevators.elevator.Elevator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static elevators.elevator.ElevatorCurrentState.*;
import static org.mockito.Mockito.*;

class UpdaterElevatorStateAndNextDestinationTest {

    private static final UpdaterElevatorStateAndNextDestination updater =
            new UpdaterElevatorStateAndNextDestination();

    @Test
    public void update_newOldDestinationsMatches() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(1);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getNextDestination()).thenReturn(0);

        when(elevator.getCalls()).thenReturn(new TreeSet<>());
        when(elevator.isWithoutPassengers()).thenReturn(true);

        updater.update(elevator);

        verify(elevator, never()).setNextDestination(0);
        verify(elevator, never()).setCurrentState(EMPTY_STAND);
    }

    @Test
    public void update_empty() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(1);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>());
        when(elevator.isWithoutPassengers()).thenReturn(true);

        updater.update(elevator);

        verify(elevator).setNextDestination(0);
        verify(elevator).setCurrentState(EMPTY_STAND);
    }

    @Test
    public void update_empty_callsNotEmpty() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(1);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(2, 3)));
        when(elevator.isWithoutPassengers()).thenReturn(true);

        updater.update(elevator);

        verify(elevator).setNextDestination(2);
        verify(elevator).setCurrentState(EMPTY_GO_TO_CALL);
    }

    @Test
    public void update_haveCallsOrButtonsOnlyDown1() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(8);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(4, 6)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(6);
        verify(elevator).setCurrentState(GOING_DOWN);
    }

    @Test
    public void update_haveCallsOrButtonsOnlyDown2() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(8);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(2, 5)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>());
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(5);
        verify(elevator).setCurrentState(GOING_DOWN);
    }

    @Test
    public void update_haveCallsAndButtonsOnlyDown() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(8);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(2, 5)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(1, 2, 4)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(5);
        verify(elevator).setCurrentState(GOING_DOWN);
    }

    @Test
    public void update_haveCallsOrButtonsOnlyUp1() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(8);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getNextDestination()).thenReturn(5);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(11, 12)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(11);
        verify(elevator).setCurrentState(GOING_UP);
    }

    @Test
    public void update_haveCallsOrButtonsOnlyUp2() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(3);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        when(elevator.getNextDestination()).thenReturn(1);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(6, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>());
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(6);
        verify(elevator).setCurrentState(GOING_UP);
    }

    @Test
    public void update_haveCallsAndButtonsOnlyUp() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getNextDestination()).thenReturn(2);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(6, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of()));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(8, 9, 10)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(6);
        verify(elevator).setCurrentState(GOING_UP);
    }

    @Test
    public void update_goUp_haveCallsAndButtonsUpDown() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(4);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        when(elevator.getNextDestination()).thenReturn(10);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(1, 2, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(3, 1)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(8, 9, 10)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(8);
        verify(elevator).setCurrentState(GOING_UP);
    }

    @Test
    public void update_goDown_haveCallsAndButtonsUpDown() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(6);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        when(elevator.getNextDestination()).thenReturn(1);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(7)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(3, 1)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(8, 9, 10)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(3);
        verify(elevator).setCurrentState(GOING_DOWN);
    }

    @Test
    public void update_emptyGo_haveCallsAndButtonsUpDown_willGoUp() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(6);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        when(elevator.getNextDestination()).thenReturn(1);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(1, 2, 7)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(3, 1)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(7, 8, 9, 10)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(7);
        verify(elevator).setCurrentState(GOING_UP);
    }

    @Test
    public void update_emptyGo_haveCallsAndButtonsUpDown_willGoDown() {

        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrFloorNum()).thenReturn(6);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        when(elevator.getNextDestination()).thenReturn(1);

        when(elevator.getCalls()).thenReturn(new TreeSet<>(Set.of(1, 5, 8)));
        when(elevator.getButtonPressedDown()).thenReturn(new TreeSet<>(Set.of(4, 1)));
        when(elevator.getButtonPressedUp()).thenReturn(new TreeSet<>(Set.of(9, 10)));
        when(elevator.isWithoutPassengers()).thenReturn(false);

        updater.update(elevator);

        verify(elevator).setNextDestination(5);
        verify(elevator).setCurrentState(GOING_DOWN);
    }

}
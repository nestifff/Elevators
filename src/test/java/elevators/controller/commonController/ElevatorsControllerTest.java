package elevators.controller.commonController;

import building.floor.Floor;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static constants.GlobalApplicationConstants.*;
import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonDirection.UP;
import static elevators.elevator.ElevatorCurrentState.GOING_DOWN;
import static elevators.elevator.ElevatorCurrentState.GOING_UP;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ElevatorsControllerTest {

    private static ElevatorsController controller;
    private static ElevatorsControllerUtils utils;
    private static UpdaterElevatorStateAndNextDestination updater;

    @BeforeEach
    public void init() {
        utils = mock(ElevatorsControllerUtils.class);
        updater = mock(UpdaterElevatorStateAndNextDestination.class);
        controller = new ElevatorsController(utils, updater);
    }

    @Test
    public void setElevators_alreadySet() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators1 = List.of(elevator1, elevator2);
        List<Elevator> elevators2 = List.of(elevator2);

        controller.setElevators(elevators1);

        assertThrows(IllegalArgumentException.class, () -> controller.setElevators(elevators2));
    }

    @Test
    public void setFloors_alreadySet() {
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors1 = List.of(floor1, floor2);
        List<Floor> floors2 = List.of(floor1);

        controller.setFloors(floors1);

        assertThrows(IllegalArgumentException.class, () -> controller.setFloors(floors2));
    }

    @Test
    public void stopAllElevators_allRight() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);

        controller.setElevators(elevators);
        controller.stopAllElevators(NEXT_DESTINATION_TO_STOP_ELEVATORS);

        verify(elevator1).setNextDestination(NEXT_DESTINATION_TO_STOP_ELEVATORS);
        verify(elevator2).setNextDestination(NEXT_DESTINATION_TO_STOP_ELEVATORS);
    }

    @Test
    public void floorButtonOnClickProcess_floorQueueEmpty() {
        Elevator elevator1 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1);
        Floor floor1 = mock(Floor.class);
        List<Floor> floors = List.of(floor1);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(true);
        controller.floorButtonOnClickProcess(1, UP);

        verify(elevator1, never()).addCall(anyInt());
        verify(updater, never()).update(any(Elevator.class));
        verify(utils, never()).getTimeToFloor(any(Elevator.class), anyInt());
        verify(utils).isFloorQueueEmpty(floors, 1);
    }

    @Test
    public void floorButtonOnClickProcess_twoFitChooseByTimeToFloor() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_UP, 1)).thenReturn(true);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator2, GOING_UP, 1)).thenReturn(true);
        when(elevator1.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        when(elevator2.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        when(utils.getTimeToFloor(elevator1, 1)).thenReturn((long) 100);
        when(utils.getTimeToFloor(elevator2, 1)).thenReturn((long) 1000);

        controller.floorButtonOnClickProcess(1, UP);

        verify(utils).isFloorQueueEmpty(floors, 1);
        verify(utils, times(2))
                .checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
        verify(utils, times(2)).getTimeToFloor(any(Elevator.class), anyInt());
        verify(elevator1).getRemainingCapacity();
        verify(elevator2).getRemainingCapacity();
        verify(elevator1).addCall(1);
        verify(elevator2, never()).addCall(anyInt());
        verify(updater).update(elevator1);
    }

    @Test
    public void floorButtonOnClickProcess_oneNotFitRemainingCapacity() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_DOWN, 1)).thenReturn(true);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator2, GOING_DOWN, 1)).thenReturn(true);
        when(elevator1.getRemainingCapacity()).thenReturn(1);
        when(elevator2.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);

        controller.floorButtonOnClickProcess(1, DOWN);

        verify(utils, times(2))
                .checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
        verify(elevator1).getRemainingCapacity();
        verify(elevator2).getRemainingCapacity();
        verify(utils, never()).getTimeToFloor(any(Elevator.class), anyInt());
        verify(elevator2).addCall(1);
        verify(elevator1, never()).addCall(anyInt());
        verify(updater).update(elevator2);
    }

    @Test
    public void floorButtonOnClickProcess_oneNotFitDirections() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_DOWN, 1)).thenReturn(true);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator2, GOING_DOWN, 1)).thenReturn(false);
        when(elevator1.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        controller.floorButtonOnClickProcess(1, DOWN);

        verify(utils, times(2))
                .checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
        verify(elevator1).getRemainingCapacity();
        verify(elevator2, never()).getRemainingCapacity();
        verify(utils, never()).getTimeToFloor(any(Elevator.class), anyInt());
        verify(elevator1).addCall(1);
        verify(elevator2, never()).addCall(anyInt());
        verify(updater).update(elevator1);
    }

    @Test
    public void floorButtonOnClickProcess_bothSuitAndHaveSameTimeToFloor() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_UP, 1)).thenReturn(true);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator2, GOING_UP, 1)).thenReturn(true);
        when(elevator1.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        when(elevator2.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        when(utils.getTimeToFloor(elevator1, 1)).thenReturn((long) 1000);
        when(utils.getTimeToFloor(elevator2, 1)).thenReturn((long) 1000);

        controller.floorButtonOnClickProcess(1, UP);

        verify(utils).isFloorQueueEmpty(floors, 1);
        verify(utils, times(2))
                .checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
        verify(utils, times(2)).getTimeToFloor(any(Elevator.class), anyInt());
        verify(elevator1).getRemainingCapacity();
        verify(elevator2).getRemainingCapacity();
        verify(elevator1).addCall(1);
        verify(elevator2, never()).addCall(anyInt());
        verify(updater).update(elevator1);
    }

    @SneakyThrows
    @Test
    public void floorButtonOnClickProcess_nothingSuit() {
        Elevator elevator1 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_DOWN, 1)).thenReturn(false);
        controller.floorButtonOnClickProcess(1, DOWN);

        verify(utils).checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
        verify(elevator1, never()).getRemainingCapacity();
        verify(elevator1, never()).addCall(1);
        verify(updater, never()).update(elevator1);

        Thread.sleep(TIME_RETRY_FIND_ELEVATOR_FOR_CALL * 2);

        verify(utils, times(2))
                .checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
    }

    @Test
    public void elevatorArrivedOnFloor_asItsCall() {
        Elevator elevator1 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_UP, 1)).thenReturn(true);
        when(elevator1.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        controller.floorButtonOnClickProcess(1, UP);
        controller.elevatorArrivedOnFloor(elevator1, 1);

        verify(elevator1).addCall(1);
        verify(elevator1).removeCall(1);
        verify(updater, times(2)).update(elevator1);
    }

    @Test
    public void elevatorArrivedOnFloor_abortAnotherCall() {
        Elevator elevator1 = mock(Elevator.class);
        Elevator elevator2 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1, elevator2);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_UP, 1)).thenReturn(true);
        when(elevator1.getRemainingCapacity()).thenReturn(MAX_PASSENGER_WEIGHT);
        controller.floorButtonOnClickProcess(1, UP);
        when(elevator1.getNextDestination()).thenReturn(1);
        controller.elevatorArrivedOnFloor(elevator2, 1);

        verify(elevator1).addCall(1);
        verify(elevator1).removeCall(1);
        verify(updater, times(2)).update(elevator1);
        verify(updater).update(elevator2);
    }

    @Test
    public void elevatorArrivedOnFloor_wasNotCall() {
        Elevator elevator1 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        controller.elevatorArrivedOnFloor(elevator1, 1);

        verify(elevator1, never()).addCall(1);
        verify(elevator1, never()).removeCall(1);
        verify(updater).update(elevator1);
    }

    @SneakyThrows
    @Test
    public void stopAllDelayedElevatorsChoices_allRight() {
        Elevator elevator1 = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator1);
        Floor floor1 = mock(Floor.class);
        Floor floor2 = mock(Floor.class);
        List<Floor> floors = List.of(floor1, floor2);

        controller.setElevators(elevators);
        controller.setFloors(floors);

        when(utils.isFloorQueueEmpty(floors, 1)).thenReturn(false);
        when(utils.checkDirectionAndCurrFloorSuitToTakeCall(elevator1, GOING_DOWN, 1)).thenReturn(false);
        controller.floorButtonOnClickProcess(1, DOWN);
        controller.stopAllDelayedElevatorsChoices();

        Thread.sleep(TIME_RETRY_FIND_ELEVATOR_FOR_CALL * 2);

        verify(utils).checkDirectionAndCurrFloorSuitToTakeCall(any(), any(), anyInt());
    }

}
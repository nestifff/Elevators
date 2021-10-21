package statistics;

import building.floor.Floor;
import building.passenger.Passenger;
import elevators.controller.commonController.ElevatorsController;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElevatorsStatisticsTest {

    @Test
    public void elevatorArrivedOnFloor_firstIteration() {

        ElevatorsController controller = mock(ElevatorsController.class);
        Elevator elevator = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator);
        Floor floor1 = mock(Floor.class);
        when(floor1.getNumber()).thenReturn(1);
        Floor floor2 = mock(Floor.class);
        when(floor2.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor1, floor2);
        Passenger passenger1 = mock(Passenger.class);
        Passenger passenger2 = mock(Passenger.class);

        ElevatorsStatistics statistics = new ElevatorsStatistics(controller, elevators, floors, 1);

        when(elevator.getFloorToPassengersDrop()).thenReturn(Map.of(1, Set.of(passenger1, passenger2)));
        statistics.elevatorArrivedOnFloor(elevator, 1);

        verify(controller).elevatorArrivedOnFloor(elevator, 1);
        assertEquals(statistics.getInterimStatisticsString(), "\nElevator 0: ElevatorStatisticsItem(numOfPeopleTransported=0, numOfFloorsPassed=0, numOfStopsOnFloors=1)\n");
    }

    @Test
    public void elevatorArrivedOnFloor_secondIteration() {

        ElevatorsController controller = mock(ElevatorsController.class);
        Elevator elevator = mock(Elevator.class);
        List<Elevator> elevators = List.of(elevator);
        Floor floor1 = mock(Floor.class);
        when(floor1.getNumber()).thenReturn(1);
        Floor floor2 = mock(Floor.class);
        when(floor2.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(floor1, floor2);
        Passenger passenger1 = mock(Passenger.class);
        Passenger passenger2 = mock(Passenger.class);

        ElevatorsStatistics statistics = new ElevatorsStatistics(controller, elevators, floors, 1);

        when(elevator.getFloorToPassengersDrop()).thenReturn(Map.of(1, Set.of(passenger1, passenger2)));
        statistics.elevatorArrivedOnFloor(elevator, 1);
        when(elevator.getFloorToPassengersDrop()).thenReturn(Map.of(1, Set.of(passenger1)));
        statistics.elevatorArrivedOnFloor(elevator, 2);

        verify(controller, times(2)).elevatorArrivedOnFloor(any(), anyInt());
        assertEquals(statistics.getInterimStatisticsString(), "\nElevator 0: ElevatorStatisticsItem(numOfPeopleTransported=1, numOfFloorsPassed=1, numOfStopsOnFloors=2)\n");
    }

    @SneakyThrows
    @Test
    public void runFinish_allRight() {
        ElevatorsController controller = mock(ElevatorsController.class);
        List<Elevator> elevators = List.of();
        List<Floor> floors = List.of();

        ElevatorsStatistics statistics = new ElevatorsStatistics(controller, elevators, floors, 1);
        statistics.start();
        TimeUnit.SECONDS.sleep(1);
        statistics.finish();
        statistics.join();

        verify(controller, never()).elevatorArrivedOnFloor(any(), anyInt());
    }

    @SneakyThrows
    @Test
    public void writeToFile_allRight() {
        ElevatorsController controller = mock(ElevatorsController.class);
        List<Elevator> elevators = List.of();
        Floor floor1 = mock(Floor.class);
        List<Floor> floors = List.of(floor1);
        FileWriter fileWriter = mock(FileWriter.class);

        ElevatorsStatistics statistics = new ElevatorsStatistics(controller, elevators, floors, 1);

        statistics.writeToFile(fileWriter);

        verify(fileWriter, times(4)).write(anyString());
        verify(fileWriter).close();
        verify(floor1).getPassengers();
    }
}
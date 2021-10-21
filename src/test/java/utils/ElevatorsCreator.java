package utils;

import building.floor.Floor;
import elevators.elevator.Elevator;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static constants.GlobalApplicationConstants.NEXT_DESTINATION_TO_STOP_ELEVATORS;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElevatorsCreator {

    public static Elevator anyElevator() {

        Floor floor = mock(Floor.class);
        when(floor.getNumber()).thenReturn(1);
        List<Floor> floors = List.of(mock(Floor.class));
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        Elevator elevatorFlorToStop = mock(Elevator.class);

        return new Elevator(1, floors, queue, elevatorFlorToStop, NEXT_DESTINATION_TO_STOP_ELEVATORS);
    }
}

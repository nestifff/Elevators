package elevators.controller.elevatorArrivedFloor;

import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import statistics.ElevatorsStatistics;

import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.*;

class ThreadProcessorElevatorArrivedFloorTest {

    @SneakyThrows
    @Test
    public void run_allRight() {

        Elevator flagToStop = mock(Elevator.class);
        Elevator someElevator = mock(Elevator.class);
        BlockingQueue<Elevator> queue = mock(BlockingQueue.class);
        ElevatorsStatistics statistics = mock(ElevatorsStatistics.class);

        ThreadProcessorElevatorArrivedFloor processor =
                new ThreadProcessorElevatorArrivedFloor(flagToStop, queue, statistics);

        when(queue.take()).thenReturn(someElevator).thenReturn(flagToStop);
        when(someElevator.getCurrFloorNum()).thenReturn(2);

        processor.run();

        verify(queue, times(2)).take();
        verify(statistics).elevatorArrivedOnFloor(someElevator, 2);
    }

}
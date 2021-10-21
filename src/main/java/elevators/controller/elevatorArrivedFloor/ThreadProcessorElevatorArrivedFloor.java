package elevators.controller.elevatorArrivedFloor;

import elevators.controller.ElevatorArrivedFloorListener;
import elevators.elevator.Elevator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class ThreadProcessorElevatorArrivedFloor extends Thread {

    private final Elevator flagToStop;

    private final BlockingQueue<Elevator> queue;
    private final ElevatorArrivedFloorListener elevatorArrivedFloorListener;

    @SneakyThrows
    @Override
    public void run() {

        while (true) {
            Elevator elevator = queue.take();
            if (elevator.equals(flagToStop)) {
                while(queue.size() > 0) {
                    queue.take();
                }
                break;
            }
            int floorNum = elevator.getCurrFloorNum();
            elevatorArrivedFloorListener.elevatorArrivedOnFloor(elevator, floorNum);
        }
        log.info("Finished");
    }

}

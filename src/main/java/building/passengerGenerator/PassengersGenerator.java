package building.passengerGenerator;

import building.floor.FirstFloor;
import building.floor.Floor;
import building.passenger.Passenger;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static constants.GlobalApplicationConstants.*;

@Slf4j
public class PassengersGenerator extends Thread {

    private boolean isNeedWork = true;
    private final Object needStopMonitor = new Object();
    private final ButtonOnClickData flagToStop;

    private final Floor floor;
    private final List<Integer> availableFloorNums = new ArrayList<>();

    public PassengersGenerator(
            Floor floor,
            ButtonOnClickData flagToStop
    ) {
        super("PassengersSpawn-" + floor.getNumber());

        IntStream.range(1, COUNT_OF_FLOORS + 1)
                .filter(it -> it != floor.getNumber())
                .forEach(availableFloorNums::add);

        this.floor = floor;
        this.flagToStop = flagToStop;
    }

    @SneakyThrows
    @Override
    public void run() {

        while (isNeedWork) {
            Passenger passenger = getRandomPassenger();
            floor.passengerGenerated(passenger);

            synchronized (needStopMonitor) {
                if (floor instanceof FirstFloor) {
                    needStopMonitor.wait(INTERVAL_TO_GENERATE_PASSENGERS_FIRST_FLOOR * 1000);
                } else {
                    needStopMonitor.wait(INTERVAL_TO_GENERATE_PASSENGERS * 1000);
                }
            }
        }
        floor.sendInQueueToStop(flagToStop);
        log.info("Finished");
    }

    public Passenger getRandomPassenger() {

        int randomForFloorNum = RandomNumberCreator.getRandom(0, availableFloorNums.size() - 1);
        int requiredFloorNum = availableFloorNums.get(randomForFloorNum);
        int randomWeight = RandomNumberCreator.getRandom(MIN_PASSENGER_WEIGHT, MAX_PASSENGER_WEIGHT);
        int randomForBool = RandomNumberCreator.getRandom(0, 1);

        return new Passenger(randomWeight, floor.getNumber(),
                requiredFloorNum, randomForBool != 0);
    }

    public void finish() {
        synchronized (needStopMonitor) {
            isNeedWork = false;
            needStopMonitor.notifyAll();
        }
    }
}

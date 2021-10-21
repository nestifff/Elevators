package statistics;

import building.floor.Floor;
import building.passenger.Passenger;
import com.google.common.collect.Sets;
import elevators.controller.ElevatorArrivedFloorListener;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static constants.GlobalApplicationConstants.INTERVAL_TO_PRINT_STATISTICS;

@Slf4j
public class ElevatorsStatistics extends Thread implements ElevatorArrivedFloorListener {

    private boolean isNeedWork = true;
    private final Object needStopMonitor = new Object();

    // ElevatorsController
    private final ElevatorArrivedFloorListener whatToWrap;

    private final Map<Elevator, ElevatorStatisticsItem> statistics = new LinkedHashMap<>();
    private final Map<Elevator, ElevatorPrevPositionItem> elevatorsPrevPositions = new LinkedHashMap<>();

    private final List<Floor> floors = new ArrayList<>();

    private final int intervalSecondsToPrintStatistics;

    public ElevatorsStatistics(ElevatorArrivedFloorListener whatToWrap,
                               List<Elevator> elevators, List<Floor> floors,
                               int intervalSecondsToPrintStatistics) {

        this.floors.addAll(floors);
        this.whatToWrap = whatToWrap;
        this.intervalSecondsToPrintStatistics = intervalSecondsToPrintStatistics;
        elevators.forEach(it -> {
            statistics.put(it, new ElevatorStatisticsItem());
            elevatorsPrevPositions.put(it, new ElevatorPrevPositionItem());
        });
    }

    @Override
    public void elevatorArrivedOnFloor(Elevator elevator, int floorNum) {

        ElevatorPrevPositionItem prevPositionItem = elevatorsPrevPositions.get(elevator);
        ElevatorStatisticsItem statisticsItem = statistics.get(elevator);

        Set<Passenger> currentPassengers = getPassengersFromElevator(elevator);

        updateStatisticsItem(floorNum, prevPositionItem, statisticsItem, currentPassengers);
        updatePrevElevatorPosition(floorNum, prevPositionItem, currentPassengers);

        whatToWrap.elevatorArrivedOnFloor(elevator, floorNum);
    }

    @SneakyThrows
    @Override
    public void run() {
        TimeUnit.SECONDS.sleep(intervalSecondsToPrintStatistics);

        while (isNeedWork) {
            log.info(getInterimStatisticsString());
            synchronized (needStopMonitor) {
                needStopMonitor.wait(intervalSecondsToPrintStatistics * 1000);
            }
        }
        log.info("Finished");
    }

    private void updatePrevElevatorPosition(int floorNum, ElevatorPrevPositionItem prevPositionItem, Set<Passenger> currentPassengers) {
        prevPositionItem.setFloor(floorNum);
        prevPositionItem.setPassengers(currentPassengers);
    }

    private void updateStatisticsItem(int floorNum, ElevatorPrevPositionItem prevPositionItem,
                                      ElevatorStatisticsItem statisticsItem, Set<Passenger> currentPassengers) {

        statisticsItem.increaseFloorsPassed(Math.abs(prevPositionItem.getFloor() - floorNum));
        statisticsItem.increaseStopsOnFloors(1);

        statisticsItem.increasePeopleTransported(
                Sets.difference(prevPositionItem.getPassengers(),
                        currentPassengers).size()
        );
    }

    private Set<Passenger> getPassengersFromElevator(Elevator elevator) {
        return elevator.getFloorToPassengersDrop().entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());
    }

    public void finish() {
        synchronized (needStopMonitor) {
            isNeedWork = false;
            needStopMonitor.notify();
        }
    }

    @SneakyThrows
    public void writeToFile(FileWriter output) {

        output.write(getInterimStatisticsString());
        output.write("\n");
        output.write("Remaining passengers on floors: \n");
        floors.forEach(it -> {
            try {
                output.write("Floor " + it.getNumber() + ": " + it.getPassengers() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        output.close();
    }

    public String getInterimStatisticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        statistics.forEach((key, value) ->
                sb.append("Elevator ").append(key.getElevatorId())
                        .append(": ").append(value.toString()).append("\n"));
        return sb.toString();
    }
}

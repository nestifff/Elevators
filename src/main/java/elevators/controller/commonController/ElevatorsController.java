package elevators.controller.commonController;

import building.floor.Floor;
import elevators.button.ButtonDirection;
import elevators.controller.ElevatorArrivedFloorListener;
import elevators.elevator.Elevator;
import elevators.elevator.ElevatorCurrentState;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static constants.GlobalApplicationConstants.MAX_PASSENGER_WEIGHT;
import static constants.GlobalApplicationConstants.TIME_RETRY_FIND_ELEVATOR_FOR_CALL;
import static elevators.elevator.ElevatorCurrentState.getEquivalent;

@Slf4j
public class ElevatorsController implements ElevatorArrivedFloorListener {

    private final List<Elevator> elevators = new ArrayList<>();
    private final List<Floor> floors = new ArrayList<>();
    private final Map<Integer, Elevator> floorNumToCalledElevator = new HashMap<>();

    private final ElevatorsControllerUtils utils;
    private final UpdaterElevatorStateAndNextDestination updaterElevatorState;

    private final List<Timer> scheduledTimers = new ArrayList<>();

    public ElevatorsController(ElevatorsControllerUtils utils,
                               UpdaterElevatorStateAndNextDestination updaterElevatorState) {

        this.updaterElevatorState = updaterElevatorState;
        this.utils = utils;
    }

    public synchronized void floorButtonOnClickProcess(int requiredFloorNum, ButtonDirection buttonDirection) {

        if (utils.isFloorQueueEmpty(floors, requiredFloorNum)) {
            return;
        }

        ElevatorCurrentState suitableDirection = getEquivalent(buttonDirection);

        Optional<Elevator> suitableBestElevator =
                findSuitableBest(requiredFloorNum, suitableDirection);

        suitableBestElevator.ifPresentOrElse(it -> assignCallToElevator(requiredFloorNum, it),
                () -> delayElevatorChoice(requiredFloorNum, buttonDirection));
    }

    @Override
    public synchronized void elevatorArrivedOnFloor(Elevator elevator, int floorNum) {

        if (floorNumToCalledElevator.containsKey(floorNum)) {

            if (floorNumToCalledElevator.get(floorNum).equals(elevator)) {
                floorNumToCalledElevator.remove(floorNum);

            } else {
                abortCall(floorNum);
            }
        }
        updaterElevatorState.update(elevator);
    }

    public void stopAllDelayedElevatorsChoices() {
        scheduledTimers.forEach(timer -> {
            timer.cancel();
            timer.purge();
        });
    }

    public void stopAllElevators(int flagToStop) {
        elevators.forEach(it -> it.setNextDestination(flagToStop));
    }

    private void delayElevatorChoice(int requiredFloorNum, ButtonDirection buttonDirection) {
        ElevatorsController controller = this;
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        controller.floorButtonOnClickProcess(requiredFloorNum, buttonDirection);
                    }
                },
                TIME_RETRY_FIND_ELEVATOR_FOR_CALL
        );
        scheduledTimers.add(timer);
    }

    private void abortCall(int floorNum) {
        Elevator calledElevator = floorNumToCalledElevator.get(floorNum);
        calledElevator.removeCall(floorNum);
        log.info("Aborted: " + floorNum + ", elevator: " + calledElevator.toString());

        if (calledElevator.getNextDestination() == floorNum) {
            updaterElevatorState.update(calledElevator);
        }
    }

    private void assignCallToElevator(int requiredFloorNum, Elevator suitableBest) {

        log.info("\nCall on " + requiredFloorNum + " floor " + suitableBest);

        floorNumToCalledElevator.put(requiredFloorNum, suitableBest);
        suitableBest.addCall(requiredFloorNum);

        updaterElevatorState.update(suitableBest);
    }

    private Optional<Elevator> findSuitableBest(int requiredFloorNum,
                                                ElevatorCurrentState suitableDirection) {

        Comparator<Elevator> comparatorTimeToFloor =
                Comparator.comparingDouble(it -> utils.getTimeToFloor(it, requiredFloorNum));

        return elevators.stream()
                .filter(it -> utils.checkDirectionAndCurrFloorSuitToTakeCall(it,
                        suitableDirection, requiredFloorNum))
                .filter(it -> it.getRemainingCapacity() >= MAX_PASSENGER_WEIGHT)
                .min(comparatorTimeToFloor);
    }

    public void setElevators(List<Elevator> elevators) {
        checkArgument(this.elevators.isEmpty());
        this.elevators.addAll(elevators);
    }

    public void setFloors(List<Floor> floors) {
        checkArgument(this.floors.isEmpty());
        this.floors.addAll(floors);
    }
}

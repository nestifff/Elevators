package elevators.controller.commonController;

import building.floor.Floor;
import elevators.elevator.Elevator;
import elevators.elevator.ElevatorCurrentState;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static elevators.elevator.ElevatorCurrentState.*;

public class ElevatorsControllerUtils {

    public boolean checkDirectionAndCurrFloorSuitToTakeCall(
            Elevator elevator,
            ElevatorCurrentState suitableDirection,
            int floorNum
    ) {

        if (elevator.getCurrentState() == suitableDirection) {

            return (elevator.getCurrentState() == GOING_UP && elevator.getCurrFloorNum() < floorNum) ||
                    (elevator.getCurrentState() == GOING_DOWN && elevator.getCurrFloorNum() > floorNum);

        } else {
            return elevator.getCurrentState() == EMPTY_STAND;
        }
    }

    public boolean isFloorQueueEmpty(List<Floor> floors, int requiredFloorNum) {

        Floor requiredFloor = floors.stream()
                .filter(it -> it.getNumber() == requiredFloorNum)
                .findFirst()
                .orElse(null);

        checkNotNull(requiredFloor);
        return requiredFloor.getPassengers().isEmpty();
    }


    public long getTimeToFloor(Elevator elevator, int floorNumber) {

        checkArgument(elevator.getCurrentState() != EMPTY_GO_TO_CALL);

        int currFloorNum = elevator.getCurrFloorNum();
        ElevatorCurrentState currentState = elevator.getCurrentState();

        long drivingWithoutStopsTime = Math.abs(currFloorNum - floorNumber) * elevator.getSpeedOneFloor();
        if (currentState == EMPTY_STAND) {
            return drivingWithoutStopsTime;
        }

        Set<Integer> stopsBeforeArrivingToRequired =
                getStopsBetweenCurrFloorAndRequired(elevator, floorNumber, elevator.getCalls());

        if (currentState == GOING_DOWN) {
            stopsBeforeArrivingToRequired.addAll(getStopsBetweenCurrFloorAndRequired(
                    elevator, floorNumber, elevator.getButtonPressedDown()
            ));
        } else if (currentState == GOING_UP) {
            stopsBeforeArrivingToRequired.addAll(getStopsBetweenCurrFloorAndRequired(
                    elevator, floorNumber, elevator.getButtonPressedUp()
            ));
        }

        return drivingWithoutStopsTime +
                (stopsBeforeArrivingToRequired.size() * elevator.getTimeToStopOnFloor());
    }

    private boolean checkFloorBetweenCurrAndRequired(
            ElevatorCurrentState currentState,
            int floorNum,
            int requiredFloorNum,
            int currFloorNum
    ) {

        return ((currentState == GOING_UP) && (currFloorNum < floorNum && floorNum < requiredFloorNum)) ||
                ((currentState == GOING_DOWN) && (currFloorNum > floorNum && floorNum > requiredFloorNum));

    }

    private Set<Integer> getStopsBetweenCurrFloorAndRequired(
            Elevator elevator,
            int requiredFloorNum,
            Set<Integer> fromWhereGet
    ) {

        return fromWhereGet.stream()
                .filter(it -> checkFloorBetweenCurrAndRequired(elevator.getCurrentState(), it,
                        requiredFloorNum, elevator.getCurrFloorNum()))
                .map(Integer::new)
                .collect(Collectors.toSet());
    }
}

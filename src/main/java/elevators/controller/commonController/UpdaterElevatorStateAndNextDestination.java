package elevators.controller.commonController;

import com.google.common.base.Preconditions;
import elevators.elevator.Elevator;
import elevators.elevator.ElevatorCurrentState;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;
import static constants.GlobalApplicationConstants.COUNT_OF_FLOORS;
import static elevators.elevator.ElevatorCurrentState.*;

@Slf4j
public class UpdaterElevatorStateAndNextDestination {

    public static final int DEFAULT_DOWN_VALUE = 0;
    public static final int DEFAULT_UP_VALUE = COUNT_OF_FLOORS + 1;

    // не обязательно обновит nextDestination (вызов в floorButtonOnClick)
    public void update(Elevator elevator) {

        int currFloorNum = elevator.getCurrFloorNum();
        ElevatorCurrentState currentState = elevator.getCurrentState();

        if (elevator.isWithoutPassengers() && elevator.getCalls().isEmpty()) {
            setStateAndNextDestination(elevator, 0, EMPTY_STAND);
            return;
        }
        if (elevator.isWithoutPassengers()) {
            setStateAndNextDestination(elevator, elevator.getCalls().first(), EMPTY_GO_TO_CALL);
            return;
        }

        int closestDown = Math.max(getLast(elevator.getCalls().headSet(currFloorNum, true), DEFAULT_DOWN_VALUE),
                getLast(elevator.getButtonPressedDown(), DEFAULT_DOWN_VALUE));
        int closestUp = Math.min(getFirst(elevator.getCalls().tailSet(currFloorNum, true), DEFAULT_UP_VALUE),
                getFirst(elevator.getButtonPressedUp(), DEFAULT_UP_VALUE));

        Preconditions.checkArgument((closestDown != DEFAULT_DOWN_VALUE || closestUp != DEFAULT_UP_VALUE));

        if (closestDown == DEFAULT_DOWN_VALUE || closestUp == DEFAULT_UP_VALUE) {
            if (closestDown == DEFAULT_DOWN_VALUE) {
                setStateAndNextDestination(elevator, closestUp, GOING_UP);
            } else {
                setStateAndNextDestination(elevator, closestDown, GOING_DOWN);
            }

        } else { // both exist

            if (currentState == GOING_DOWN) {
                setStateAndNextDestination(elevator, closestDown, GOING_DOWN);

            } else if (currentState == GOING_UP) {
                setStateAndNextDestination(elevator, closestUp, GOING_UP);

            } else if (currentState == EMPTY_GO_TO_CALL) {
                findAndSetClosetsToCurrentFloor(elevator, currFloorNum, closestUp, closestDown);
            }
        }
    }

    private void setStateAndNextDestination(Elevator elevator, int nextDestination, ElevatorCurrentState newState) {
        if (nextDestination != elevator.getNextDestination()) {
            elevator.setCurrentState(newState);
            elevator.setNextDestination(nextDestination);
        }
    }

    private void findAndSetClosetsToCurrentFloor(Elevator elevator, int current,  int closestUp, int closestDown) {

        Preconditions.checkArgument(closestDown != DEFAULT_DOWN_VALUE);
        Preconditions.checkArgument(closestUp != DEFAULT_UP_VALUE);

        if (Math.abs(current - closestDown) < Math.abs(current - closestUp)) {
            setStateAndNextDestination(elevator, closestDown, GOING_DOWN);
        } else {
            setStateAndNextDestination(elevator, closestUp, GOING_UP);
        }
    }
}

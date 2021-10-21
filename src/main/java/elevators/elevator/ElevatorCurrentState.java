package elevators.elevator;

import elevators.button.ButtonDirection;

public enum ElevatorCurrentState {
    GOING_UP, GOING_DOWN, EMPTY_STAND, EMPTY_GO_TO_CALL;

    public static ElevatorCurrentState getEquivalent(ButtonDirection buttonDirection) {
        return buttonDirection == ButtonDirection.UP ? GOING_UP : GOING_DOWN;
    }
}

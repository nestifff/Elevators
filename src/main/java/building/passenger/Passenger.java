package building.passenger;

import elevators.button.Button;
import elevators.elevator.Elevator;
import elevators.elevator.ElevatorCurrentState;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonDirection.UP;
import static elevators.elevator.ElevatorCurrentState.GOING_DOWN;
import static elevators.elevator.ElevatorCurrentState.GOING_UP;

public class Passenger {

    private final UUID id;

    // else sit only on floor with suitable direction
    private final boolean willSitOnAnyElevator;
    private final int weight;
    private final int startFloor;
    private final int requiredFloor;

    public Passenger(int weight, int startFloor, int finalFloor, boolean willSitOnAnyFloor) {

        checkArgument(weight > 0);
        checkArgument(startFloor != finalFloor);

        this.weight = weight;
        this.startFloor = startFloor;
        this.requiredFloor = finalFloor;
        this.willSitOnAnyElevator = willSitOnAnyFloor;
        this.id = UUID.randomUUID();
    }

    public Optional<Button> getClickDesiredButton(Button... buttons) {

        checkArgument(buttons.length <= 2);
        checkArgument(Arrays.stream(buttons).anyMatch(it -> it.getDirection() == UP) ||
                Arrays.stream(buttons).anyMatch(it -> it.getDirection() == DOWN));

        if (startFloor < requiredFloor) {
            Optional<Button> upButton = Arrays.stream(buttons)
                    .filter(it -> it.getDirection() == UP)
                    .findFirst();

            if (upButton.isPresent()) {
                upButton.get().turnOn();
                return upButton;
            }

        } else {
            Optional<Button> downButton = Arrays.stream(buttons)
                    .filter(it -> it.getDirection() == DOWN)
                    .findFirst();

            if (downButton.isPresent()) {
                downButton.get().turnOn();
                return downButton;
            }
        }
        return Optional.empty();
    }

    public boolean checkWillSitOnElevator(Elevator elevator) {
        if (willSitOnAnyElevator) {
            return true;
        }
        ElevatorCurrentState currentState = elevator.getCurrentState();
        if (currentState == GOING_UP || currentState == GOING_DOWN) {

            return (currentState == GOING_UP && startFloor < requiredFloor) ||
                    (currentState == GOING_DOWN && startFloor > requiredFloor);
        }
        return true;
    }

    public int getWeight() {
        return weight;
    }

    public int getRequiredFloor() {
        return requiredFloor;
    }

    public int getStartFloor() {
        return startFloor;
    }

    public boolean isWillSitOnAnyElevator() {
        return willSitOnAnyElevator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return willSitOnAnyElevator == passenger.willSitOnAnyElevator &&
                weight == passenger.weight &&
                startFloor == passenger.startFloor &&
                requiredFloor == passenger.requiredFloor &&
                Objects.equals(id, passenger.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, willSitOnAnyElevator, weight, startFloor, requiredFloor);
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "w=" + weight +
                ", start=" + startFloor +
                ", finish=" + requiredFloor +
                ", willSitOnAnyFloor=" + willSitOnAnyElevator +
                '}';
    }

}

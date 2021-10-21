package building.floor;

import building.Building;
import building.passenger.Passenger;
import elevators.button.Button;
import elevators.button.ButtonDirection;
import elevators.button.ButtonStatus;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonDirection.UP;

@Slf4j
public class MiddleFloor extends Floor {

    private static final Map<Building, Set<MiddleFloor>> alreadyCreated = new HashMap<>();

    public static MiddleFloor create(Building building, int floorNum, BlockingQueue<ButtonOnClickData> queue) {

        if (alreadyCreated.containsKey(building)) {

            Optional<MiddleFloor> optionalMiddleFloor = alreadyCreated.get(building).stream()
                    .filter(it -> it.getNumber() == floorNum)
                    .findFirst();

            if (optionalMiddleFloor.isPresent()) {
                return optionalMiddleFloor.get();
            }
        }

        MiddleFloor middleFloor = new MiddleFloor(floorNum, queue);
        alreadyCreated.computeIfAbsent(building,
                middleFloors -> new HashSet<>()).add(middleFloor);
        return middleFloor;
    }

    private final Button upButton = new Button(UP);
    private final Button downButton = new Button(DOWN);

    private MiddleFloor(int number, BlockingQueue<ButtonOnClickData> queue) {
        super(number, queue);
    }

    @SneakyThrows
    @Override
    public void passengerClickButton(Passenger passenger) {

        Map<ButtonDirection, ButtonStatus> oldStates =
                Map.of(UP, upButton.getStatus(), DOWN, downButton.getStatus());

        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton, downButton);

        if (clickedButton.isPresent() &&
                oldStates.get(clickedButton.get().getDirection()) != clickedButton.get().getStatus()) {
            buttonsDataQueue.put(new ButtonOnClickData(number, clickedButton.get().getDirection()));
        }
    }

    @Override
    public void turnOffButtons() {
        upButton.turnOff();
        downButton.turnOff();
    }
}

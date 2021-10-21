package building.floor;

import building.Building;
import building.passenger.Passenger;
import elevators.button.Button;
import elevators.button.ButtonStatus;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import static constants.GlobalApplicationConstants.COUNT_OF_FLOORS;
import static elevators.button.ButtonDirection.DOWN;

public class LastFloor extends Floor {

    private static Map<Building, LastFloor> alreadyCreated = new HashMap<>();

    public static LastFloor create(Building building, BlockingQueue<ButtonOnClickData> queue) {
        if (alreadyCreated.containsKey(building)) {
            return alreadyCreated.get(building);
        }
        LastFloor lastFloor = new LastFloor(queue);
        alreadyCreated.put(building, lastFloor);
        return lastFloor;
    }

    private final Button downButton = new Button(DOWN);

    private LastFloor(BlockingQueue<ButtonOnClickData> queue) {
        super(COUNT_OF_FLOORS, queue);
    }

    @SneakyThrows
    public void passengerClickButton(Passenger passenger) {

        ButtonStatus oldStatus = downButton.getStatus();
        Optional<Button> clickedButton = passenger.getClickDesiredButton(downButton);

        if (clickedButton.isPresent() && oldStatus != clickedButton.get().getStatus()) {
            buttonsDataQueue.put(new ButtonOnClickData(number, clickedButton.get().getDirection()));
        }
    }

    @Override
    public void turnOffButtons() {
        downButton.turnOff();
    }
}

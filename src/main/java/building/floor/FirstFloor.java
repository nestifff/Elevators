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

import static elevators.button.ButtonDirection.UP;

public class FirstFloor extends Floor {

    private static Map<Building, FirstFloor> alreadyCreated = new HashMap<>();

    public static FirstFloor create(Building building, BlockingQueue<ButtonOnClickData> queue) {
        if (alreadyCreated.containsKey(building)) {
            return alreadyCreated.get(building);
        }
        FirstFloor firstFloor = new FirstFloor(queue);
        alreadyCreated.put(building, firstFloor);
        return firstFloor;
    }

    private final Button upButton = new Button(UP);

    private FirstFloor(BlockingQueue<ButtonOnClickData> queue) {
        super(1, queue);
    }

    @SneakyThrows
    public void passengerClickButton(Passenger passenger) {

        ButtonStatus oldStatus = upButton.getStatus();
        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton);

        if (clickedButton.isPresent() && oldStatus != clickedButton.get().getStatus()) {
            buttonsDataQueue.put(new ButtonOnClickData(number, clickedButton.get().getDirection()));
        }
    }

    @Override
    public void turnOffButtons() {
        upButton.turnOff();
    }

}

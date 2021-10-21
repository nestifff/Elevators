import building.Building;
import elevators.controller.commonController.ElevatorsController;
import elevators.controller.commonController.ElevatorsControllerUtils;
import elevators.controller.commonController.UpdaterElevatorStateAndNextDestination;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {

        Building building = Building.create(new ElevatorsController(
                new ElevatorsControllerUtils(),
                new UpdaterElevatorStateAndNextDestination()
        ));
        building.startAll();
        TimeUnit.SECONDS.sleep(5);
        building.finishAll();
    }
}

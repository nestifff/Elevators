package elevators.controller.floorButtonOnClick;

import elevators.controller.commonController.ElevatorsController;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;


@Slf4j
@RequiredArgsConstructor
public class FloorButtonOnClickProcessor extends Thread {

    private final ButtonOnClickData flagToStop;
    private final int flagToStopElevators;

    private final BlockingQueue<ButtonOnClickData> queue;
    private final ElevatorsController controller;

    @SneakyThrows
    @Override
    public void run() {

        while (true) {
            ButtonOnClickData buttonData = queue.take();
            if (buttonData.equals(flagToStop)) {
                while(queue.size() > 0) {
                    queue.take();
                }
                break;
            }
            controller.floorButtonOnClickProcess(buttonData.getFloorNum(), buttonData.getDirection());
        }
        controller.stopAllDelayedElevatorsChoices();
        controller.stopAllElevators(flagToStopElevators);
        log.info("Finished");
    }

}

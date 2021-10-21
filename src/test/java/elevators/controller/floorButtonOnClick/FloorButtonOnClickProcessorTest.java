package elevators.controller.floorButtonOnClick;

import elevators.button.Button;
import elevators.controller.commonController.ElevatorsController;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;

import static constants.GlobalApplicationConstants.NEXT_DESTINATION_TO_STOP_ELEVATORS;
import static elevators.button.ButtonDirection.UP;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FloorButtonOnClickProcessorTest {

    @SneakyThrows
    @Test
    public void run_allRight() {

        ButtonOnClickData flagToStop = mock(ButtonOnClickData.class);
        ButtonOnClickData someData = mock(ButtonOnClickData.class);
        BlockingQueue<ButtonOnClickData> queue = mock(BlockingQueue.class);
        ElevatorsController controller = mock(ElevatorsController.class);

        FloorButtonOnClickProcessor processor = new FloorButtonOnClickProcessor(flagToStop,
                NEXT_DESTINATION_TO_STOP_ELEVATORS, queue, controller);

        when(queue.take()).thenReturn(someData).thenReturn(flagToStop);
        when(someData.getDirection()).thenReturn(UP);
        when(someData.getFloorNum()).thenReturn(1);

        processor.run();

        verify(queue, times(2)).take();
        verify(controller).floorButtonOnClickProcess(1, UP);
        verify(controller).stopAllDelayedElevatorsChoices();
        verify(controller).stopAllElevators(NEXT_DESTINATION_TO_STOP_ELEVATORS);
    }

}
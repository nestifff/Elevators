package building.floor;

import building.Building;
import building.passenger.Passenger;
import elevators.button.Button;
import elevators.controller.commonController.ElevatorsController;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static constants.GlobalApplicationConstants.COUNT_OF_FLOORS;
import static elevators.button.ButtonDirection.UP;
import static elevators.button.ButtonStatus.ON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FirstFloorTest {

    private static FirstFloor firstFloor;
    private static BlockingQueue<ButtonOnClickData> queue;

    @BeforeEach
    public void init() {
        queue = mock(LinkedBlockingDeque.class);
        firstFloor = FirstFloor.create(mock(Building.class), queue);
    }

    @Test
    public void create_allRight() {
        Floor floor = FirstFloor.create(Building.create(mock(ElevatorsController.class)), new LinkedBlockingDeque<>());
        assertEquals(floor.getNumber(), 1);
        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void create_sameBuildingRepeatedlyReturnSame() {
        Building building = mock(Building.class);
        Floor floor1 = FirstFloor.create(building, new LinkedBlockingDeque<>());
        Floor floor2 = FirstFloor.create(building, new LinkedBlockingDeque<>());
        assertEquals(floor1.getNumber(), 1);
        assertSame(floor1, floor2);
    }

    @Test
    public void create_differentBuildingsRepeatedlyReturnNotSame() {
        Floor floor1 = FirstFloor.create(mock(Building.class), new LinkedBlockingDeque<>());
        Floor floor2 = FirstFloor.create(mock(Building.class), new LinkedBlockingDeque<>());
        assertEquals(floor1.getNumber(), 1);
        assertNotEquals(floor1, floor2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOffAfterOff() {
        Passenger passenger = mock(Passenger.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.empty());

        firstFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(queue, never()).put(any());
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOffAfterOn() {
        Passenger passenger = mock(Passenger.class);
        Button button = mock(Button.class);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.of(button));
        when(button.getStatus()).thenReturn(ON);
        when(button.getDirection()).thenReturn(UP);

        firstFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(button).getStatus();
        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), UP);
        assertEquals(argument.getValue().getFloorNum(), 1);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOnAfterOn_clickSecondTime() {
        Passenger passenger1 = new Passenger(100, 1, COUNT_OF_FLOORS, true);
        Passenger passenger2 = new Passenger(100, 1, COUNT_OF_FLOORS, true);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        firstFloor.passengerClickButton(passenger1);
        firstFloor.passengerClickButton(passenger2);

        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), UP);
        assertEquals(argument.getValue().getFloorNum(), 1);
    }

    @Test
    public void passengerClickButton_passengerNull() {
        assertThrows(NullPointerException.class, () -> firstFloor.passengerClickButton(null));
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOff_clickedAddToQueue() {
        Passenger passenger = new Passenger(100, 1, COUNT_OF_FLOORS, true);

        firstFloor.turnOffButtons();
        firstFloor.passengerClickButton(passenger);

        verify(queue).put(any());
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOn_clickedAddToQueue() {
        Passenger passenger1 = new Passenger(100, 1, COUNT_OF_FLOORS, true);

        firstFloor.passengerClickButton(passenger1);
        firstFloor.turnOffButtons();
        firstFloor.passengerClickButton(passenger1);

        verify(queue, times(2)).put(any());
    }

}
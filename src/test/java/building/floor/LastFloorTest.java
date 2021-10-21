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
import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonStatus.ON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LastFloorTest {

    private static LastFloor lastFloor;
    private static BlockingQueue<ButtonOnClickData> queue;

    @BeforeEach
    public void init() {
        queue = mock(LinkedBlockingDeque.class);
        lastFloor = LastFloor.create(mock(Building.class), queue);
    }

    @Test
    public void create_allRight() {
        Floor floor = LastFloor.create(Building.create(mock(ElevatorsController.class)), new LinkedBlockingDeque<>());
        assertEquals(floor.getNumber(), COUNT_OF_FLOORS);
        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void create_sameBuildingRepeatedlyReturnSame() {
        Building building = mock(Building.class);
        Floor floor1 = LastFloor.create(building, new LinkedBlockingDeque<>());
        Floor floor2 = LastFloor.create(building, new LinkedBlockingDeque<>());
        assertEquals(floor1.getNumber(), COUNT_OF_FLOORS);
        assertSame(floor1, floor2);
    }

    @Test
    public void create_differentBuildingsRepeatedlyReturnNotSame() {
        Floor floor1 = LastFloor.create(mock(Building.class), new LinkedBlockingDeque<>());
        Floor floor2 = LastFloor.create(mock(Building.class), new LinkedBlockingDeque<>());
        assertEquals(floor1.getNumber(), COUNT_OF_FLOORS);
        assertNotEquals(floor1, floor2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOffAfterOff() {
        Passenger passenger = mock(Passenger.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.empty());

        lastFloor.passengerClickButton(passenger);

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
        when(button.getDirection()).thenReturn(DOWN);

        lastFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(button).getStatus();
        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), DOWN);
        assertEquals(argument.getValue().getFloorNum(), COUNT_OF_FLOORS);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOnAfterOn_clickSecondTime() {
        Passenger passenger1 = new Passenger(100, COUNT_OF_FLOORS, 1, true);
        Passenger passenger2 = new Passenger(100, COUNT_OF_FLOORS, 1, true);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        lastFloor.passengerClickButton(passenger1);
        lastFloor.passengerClickButton(passenger2);

        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), DOWN);
        assertEquals(argument.getValue().getFloorNum(), COUNT_OF_FLOORS);
    }

    @Test
    public void passengerClickButton_passengerNull() {
        assertThrows(NullPointerException.class, () -> lastFloor.passengerClickButton(null));
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOff_clickedAddToQueue() {
        Passenger passenger = new Passenger(100, COUNT_OF_FLOORS, 1, true);

        lastFloor.turnOffButtons();
        lastFloor.passengerClickButton(passenger);

        verify(queue).put(any());
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOn_clickedAddToQueue() {
        Passenger passenger1 = new Passenger(100, COUNT_OF_FLOORS, 1, true);

        lastFloor.passengerClickButton(passenger1);
        lastFloor.turnOffButtons();
        lastFloor.passengerClickButton(passenger1);

        verify(queue, times(2)).put(any());
    }


}
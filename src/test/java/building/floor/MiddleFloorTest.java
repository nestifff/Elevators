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

import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonDirection.UP;
import static elevators.button.ButtonStatus.ON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MiddleFloorTest {

    private static MiddleFloor middleFloor;
    private static BlockingQueue<ButtonOnClickData> queue;

    @BeforeEach
    public void init() {
        queue = mock(LinkedBlockingDeque.class);
        middleFloor = MiddleFloor.create(mock(Building.class), 2, queue);
    }

    @Test
    public void create_allRight() {
        Floor floor = MiddleFloor.create(Building.create(mock(ElevatorsController.class)), 2, new LinkedBlockingDeque<>());
        assertEquals(floor.getNumber(), 2);
        assertTrue(floor.getPassengers().isEmpty());
    }

    @Test
    public void create_sameBuildingAndNumberRepeatedlyReturnSame() {
        Building building = mock(Building.class);
        Floor floor1 = MiddleFloor.create(building, 2, new LinkedBlockingDeque<>());
        Floor floor2 = MiddleFloor.create(building, 2, new LinkedBlockingDeque<>());

        assertEquals(floor1.getNumber(), 2);
        assertSame(floor1, floor2);
    }

    @Test
    public void create_sameBuildingNotSameNumber() {
        Building building = mock(Building.class);
        Floor floor1 = MiddleFloor.create(building, 2, new LinkedBlockingDeque<>());
        Floor floor2 = MiddleFloor.create(building, 3, new LinkedBlockingDeque<>());

        assertEquals(floor1.getNumber(), 2);
        assertNotEquals(floor1, floor2);
    }

    @Test
    public void create_notSameBuildingSameNumber() {
        Floor floor1 = MiddleFloor.create(mock(Building.class), 2, new LinkedBlockingDeque<>());
        Floor floor2 = MiddleFloor.create(mock(Building.class), 2, new LinkedBlockingDeque<>());

        assertEquals(floor1.getNumber(), 2);
        assertNotEquals(floor1, floor2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_beforeOffAfterOff() {
        Passenger passenger = mock(Passenger.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.empty());

        middleFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(queue, never()).put(any());
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_upBeforeOffAfterOn() {
        Passenger passenger = mock(Passenger.class);
        Button button = mock(Button.class);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.of(button));
        when(button.getStatus()).thenReturn(ON);
        when(button.getDirection()).thenReturn(UP);

        middleFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(button).getStatus();
        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), UP);
        assertEquals(argument.getValue().getFloorNum(), 2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_downBeforeOffAfterOn() {
        Passenger passenger = mock(Passenger.class);
        Button button = mock(Button.class);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        when(passenger.getClickDesiredButton(any(Button.class))).thenReturn(Optional.of(button));
        when(button.getStatus()).thenReturn(ON);
        when(button.getDirection()).thenReturn(DOWN);

        middleFloor.passengerClickButton(passenger);

        verify(passenger).getClickDesiredButton(any());
        verify(button).getStatus();
        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), DOWN);
        assertEquals(argument.getValue().getFloorNum(), 2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_upBeforeOnAfterOn_clickSecondTime() {
        Passenger passenger1 = new Passenger(100, 2, 3, true);
        Passenger passenger2 = new Passenger(100, 2, 3, true);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);

        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), UP);
        assertEquals(argument.getValue().getFloorNum(), 2);
    }


    @SneakyThrows
    @Test
    public void passengerClickButton_dowBeforeOnAfterOn_clickSecondTime() {
        Passenger passenger1 = new Passenger(100, 2, 1, true);
        Passenger passenger2 = new Passenger(100, 2, 1, true);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);

        verify(queue).put(argument.capture());
        assertSame(argument.getValue().getDirection(), DOWN);
        assertEquals(argument.getValue().getFloorNum(), 2);
    }

    @SneakyThrows
    @Test
    public void passengerClickButton_twoButtonsBeforeOffAfterOn() {
        Passenger passenger1 = new Passenger(100, 2, 1, true);
        Passenger passenger2 = new Passenger(100, 2, 3, true);
        ArgumentCaptor<ButtonOnClickData> argument = ArgumentCaptor.forClass(ButtonOnClickData.class);

        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);

        verify(queue, times(2)).put(argument.capture());
        assertSame(argument.getAllValues().get(0).getDirection(), DOWN);
        assertEquals(argument.getAllValues().get(0).getFloorNum(), 2);
        assertSame(argument.getAllValues().get(1).getDirection(), UP);
        assertEquals(argument.getAllValues().get(1).getFloorNum(), 2);
    }

    @Test
    public void passengerClickButton_passengerNull() {
        assertThrows(NullPointerException.class, () -> middleFloor.passengerClickButton(null));
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOff_clickedAddToQueue() {
        Passenger passenger1 = new Passenger(100, 2, 1, true);
        Passenger passenger2 = new Passenger(100, 2, 3, true);

        middleFloor.turnOffButtons();
        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);

        verify(queue, times(2)).put(any());
    }

    @SneakyThrows
    @Test
    public void turnOffButtons_beforeOn_clickedAddToQueue() {

        Passenger passenger1 = new Passenger(100, 2, 1, true);
        Passenger passenger2 = new Passenger(100, 2, 3, true);

        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);
        middleFloor.turnOffButtons();
        middleFloor.passengerClickButton(passenger1);
        middleFloor.passengerClickButton(passenger2);

        verify(queue, times(4)).put(any());
    }

}
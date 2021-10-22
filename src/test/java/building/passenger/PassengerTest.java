package building.passenger;

import elevators.button.Button;
import elevators.elevator.Elevator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static elevators.button.ButtonDirection.DOWN;
import static elevators.button.ButtonDirection.UP;
import static elevators.elevator.ElevatorCurrentState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PassengerTest {

    @Test
    public void create_allRight() {
        Passenger passenger = new Passenger(80, 4, 10, true);

        assertEquals(passenger.getRequiredFloor(), 10);
        assertEquals(passenger.getStartFloor(), 4);
        assertEquals(passenger.getWeight(), 80);
        assertTrue(passenger.isWillSitOnAnyElevator());
    }

    @Test
    public void create_illegalWeightNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger(-20, 1, 10, true));
    }

    @Test
    public void create_illegalWeightZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger(0, 1, 10, false));
    }

    @Test
    public void create_illegalSameStartFinalFloors() {
        assertThrows(IllegalArgumentException.class, () ->
                new Passenger(0, 10, 10, true));
    }

    @Test
    public void clickDesiredButton_emptyButtons() {
        Passenger passenger = new Passenger(80, 4, 10, true);
        assertThrows(IllegalArgumentException.class, () -> passenger.getClickDesiredButton());
    }

    @Test
    public void clickDesiredButton_buttonsMoreThenTwo() {
        Passenger passenger = new Passenger(80, 4, 10, true);
        assertThrows(IllegalArgumentException.class, () ->
                passenger.getClickDesiredButton(new Button(UP), new Button(DOWN), new Button(UP)));
    }

    @Test
    public void clickDesiredButton_oneButtonUpClicked() {
        Passenger passenger = new Passenger(80, 4, 10, true);
        Button upButton = mock(Button.class);

        when(upButton.getDirection()).thenReturn(UP);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton);

        assertSame(clickedButton.get(), upButton);
        verify(upButton).turnOn();
    }

    @Test
    public void clickDesiredButton_oneButtonUpNotClicked() {
        Passenger passenger = new Passenger(80, 4, 1, true);
        Button upButton = mock(Button.class);

        when(upButton.getDirection()).thenReturn(UP);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton);

        assertTrue(clickedButton.isEmpty());
        verify(upButton, never()).turnOn();
    }

    @Test
    public void clickDesiredButton_oneButtonDownClicked() {
        Passenger passenger = new Passenger(80, 4, 1, true);
        Button downButton = mock(Button.class);

        when(downButton.getDirection()).thenReturn(DOWN);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(downButton);

        assertSame(clickedButton.get(), downButton);
        verify(downButton).turnOn();
    }

    @Test
    public void clickDesiredButton_oneButtonDownNotClicked() {
        Passenger passenger = new Passenger(80, 4, 10, true);
        Button downButton = mock(Button.class);

        when(downButton.getDirection()).thenReturn(DOWN);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(downButton);

        assertTrue(clickedButton.isEmpty());
        verify(downButton, never()).turnOn();
    }

    @Test
    public void clickDesiredButton_twoButtonsDownClicked() {
        Passenger passenger = new Passenger(80, 10, 9, false);
        Button upButton = mock(Button.class);
        Button downButton = mock(Button.class);

        when(upButton.getDirection()).thenReturn(UP);
        when(downButton.getDirection()).thenReturn(DOWN);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton, downButton);

        assertSame(clickedButton.get(), downButton);
        verify(upButton, never()).turnOn();
        verify(downButton).turnOn();
    }

    @Test
    public void clickDesiredButton_twoButtonsUpClicked() {
        Passenger passenger = new Passenger(80, 1, 9, false);
        Button upButton = mock(Button.class);
        Button downButton = mock(Button.class);

        when(upButton.getDirection()).thenReturn(UP);
        when(downButton.getDirection()).thenReturn(DOWN);
        Optional<Button> clickedButton = passenger.getClickDesiredButton(upButton, downButton);

        assertSame(clickedButton.get(), upButton);
        verify(upButton).turnOn();
        verify(downButton, never()).turnOn();
    }

    @Test
    public void checkWillSitOnElevator_willSitOnAny1() {
        Passenger passenger = new Passenger(80, 1, 9, true);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_willSitOnAny2() {
        Passenger passenger = new Passenger(80, 1, 9, true);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_willSitOnAny3() {
        Passenger passenger = new Passenger(80, 1, 9, true);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_suitElevatorUp() {
        Passenger passenger = new Passenger(80, 1, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_notSuitElevatorUp() {
        Passenger passenger = new Passenger(80, 10, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_UP);
        assertFalse(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_suitElevatorDown() {
        Passenger passenger = new Passenger(80, 10, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_notSuitElevatorDown() {
        Passenger passenger = new Passenger(80, 1, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(GOING_DOWN);
        assertFalse(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_suitElevatorEmptyGo() {
        Passenger passenger = new Passenger(80, 10, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_GO_TO_CALL);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void checkWillSitOnElevator_suitElevatorEmptyStand() {
        Passenger passenger = new Passenger(80, 10, 9, false);
        Elevator elevator = mock(Elevator.class);
        when(elevator.getCurrentState()).thenReturn(EMPTY_STAND);
        assertTrue(passenger.checkWillSitOnElevator(elevator));
    }

    @Test
    public void equalsHashCOde_false() {
        Passenger p1 = new Passenger(80, 1, 2, false);
        Passenger p2 = new Passenger(80, 1, 2, false);
        assertNotEquals(p2, p1);
        assertFalse(p1.hashCode() == p2.hashCode());
    }

    @Test
    public void equalsHashCOde_true() {
        Passenger p1 = new Passenger(80, 1, 2, false);
        Passenger p2 = p1;
        assertEquals(p2, p1);
        assertEquals(p2.hashCode(), p1.hashCode());
    }

    @Test
    public void toString_allRight() {
        Passenger p1 = new Passenger(80, 1, 2, false);
        assertEquals(p1.toString(), "Passenger{w=80, start=1, finish=2, willSitOnAnyFloor=false}");
    }

}
package elevators.button;

import org.junit.jupiter.api.Test;

import static elevators.button.ButtonDirection.UP;
import static org.junit.jupiter.api.Assertions.*;

class ButtonTest {

    @Test
    public void create_rightState() {
        Button button = new Button(UP);
        assertFalse(button.isOn());
        assertSame(button.getDirection(), UP);
        assertSame(button.getStatus(), ButtonStatus.OFF);
    }

    @Test
    public void turnOn_test() {
        Button button = new Button(UP);
        button.turnOn();
        assertTrue(button.isOn());
    }

    @Test
    public void turnOff_afterTurnOn() {
        Button button = new Button(UP);
        button.turnOn();
        button.turnOff();
        assertFalse(button.isOn());
    }

    @Test
    public void equalsHashCode_false() {
        Button b1 = new Button(UP);
        Button b2 = new Button(UP);
        assertNotEquals(b2, b1);
        assertFalse(b1.hashCode() == b2.hashCode());
    }

    @Test
    public void equalsHashCode_true() {
        Button b1 = new Button(UP);
        Button b2 = b1;
        assertEquals(b2, b1);
        assertEquals(b2.hashCode(), b1.hashCode());
    }

}
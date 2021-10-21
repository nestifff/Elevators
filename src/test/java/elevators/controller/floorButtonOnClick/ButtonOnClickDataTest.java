package elevators.controller.floorButtonOnClick;

import elevators.button.ButtonDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ButtonOnClickDataTest {

    @Test
    public void create_allRight() {
        ButtonOnClickData data = new ButtonOnClickData(2, ButtonDirection.UP);

        assertSame(data.getDirection(), ButtonDirection.UP);
        assertSame(data.getFloorNum(), 2);
    }

}
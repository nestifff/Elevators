package elevators.controller.floorButtonOnClick;

import elevators.button.ButtonDirection;
import lombok.Data;

@Data
public class ButtonOnClickData {

    private final int floorNum;
    private final ButtonDirection direction;
}

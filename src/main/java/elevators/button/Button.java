package elevators.button;

import java.util.Objects;
import java.util.UUID;

public class Button {

    private final UUID id;
    private final ButtonDirection direction;
    private ButtonStatus status;

    public Button(ButtonDirection direction) {
        this.id = UUID.randomUUID();
        this.direction = direction;
        this.status = ButtonStatus.OFF;
    }

    public void turnOn() {
        status = ButtonStatus.ON;
    }

    public void turnOff() {
        status = ButtonStatus.OFF;
    }

    public boolean isOn() {
        return status == ButtonStatus.ON;
    }

    public ButtonDirection getDirection() {
        return direction;
    }

    public ButtonStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Button button = (Button) o;
        return Objects.equals(id, button.id) &&
                direction == button.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, direction);
    }
}

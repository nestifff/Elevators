package building.floor;

import building.passenger.Passenger;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class Floor{

    private final UUID id;
    protected final int number;
    private final Queue<Passenger> passengers = new LinkedList<>();
    protected final BlockingQueue<ButtonOnClickData> buttonsDataQueue;

    public Floor(int number, BlockingQueue<ButtonOnClickData> queue) {
        this.number = number;
        this.buttonsDataQueue = queue;
        this.id = UUID.randomUUID();
    }

    public synchronized void elevatorArrived(Elevator elevator) {
        turnOffButtons();

        dropPassengers(elevator);
        putPassengers(elevator);

        if (!passengers.isEmpty()) {
            passengersClickButton();
        }
    }

    public synchronized void passengerGenerated(Passenger newPassenger) {
        passengers.add(newPassenger);
        passengerClickButton(newPassenger);
    }

    protected void turnOffButtons() {
    }

    private synchronized void putPassengers(Elevator elevator) {
        List.copyOf(passengers).stream()
                .filter(elevator::doesPassengerFit)
                .filter(it -> it.checkWillSitOnElevator(elevator))
                .forEach(it -> {
                    elevator.putPassenger(it);
                    passengers.remove(it);
                });
    }

    private synchronized void dropPassengers(Elevator elevator) {
        if (elevator.getFloorToPassengersDrop().containsKey(number)) {
            elevator.getFloorToPassengersDrop().get(number)
                    .forEach(elevator::dropPassenger);
            elevator.getFloorToPassengersDrop().remove(number);
        }
    }

    private void passengersClickButton() {
        passengers.forEach(this::passengerClickButton);
    }

    protected void passengerClickButton(Passenger passenger) {
        passenger.getClickDesiredButton();
    }

    @SneakyThrows
    public void sendInQueueToStop(ButtonOnClickData flagToStop) {
        buttonsDataQueue.put(flagToStop);
    }

    public int getNumber() {
        return number;
    }

    public Queue<Passenger> getPassengers() {
        return new LinkedList<>(passengers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Floor floor = (Floor) o;
        return number == floor.number &&
                Objects.equals(id, floor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }
}

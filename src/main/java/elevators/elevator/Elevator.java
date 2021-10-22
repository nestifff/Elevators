package elevators.elevator;

import building.floor.Floor;
import building.passenger.Passenger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static constants.GlobalApplicationConstants.*;
import static elevators.elevator.ElevatorCurrentState.*;

@Slf4j
public class Elevator extends Thread {

    private final Elevator flagToStop;
    private final int nextDestinationToStop;

    private final int id;

    private final BlockingQueue<Elevator> elevatorsQueue;

    private final int liftingCapacity = LIFTING_CAPACITY;
    private int remainingCapacity;

    private final long speedOneFloor = SPEED_ONE_FLOOR;
    private final long timeOpenDoors = TIME_OPEN_DOORS;
    private final long timeCloseDoors = TIME_CLOSE_DOORS;

    private final Object destinationChangedMonitor = new Object();
    private volatile boolean wasDestinationChanged = false;

    private volatile ElevatorCurrentState currentState;
    private int currFloorNum = 1;

    private volatile int nextDestination;

    private final List<Floor> floors = new ArrayList<>();

    private final SortedSet<Integer> buttonPressedDown = new TreeSet<>();
    private final SortedSet<Integer> buttonPressedUp = new TreeSet<>();

    private final Map<Integer, Set<Passenger>> floorToPassengersDrop = new HashMap<>();

    private final NavigableSet<Integer> calls = new TreeSet<>();

    public Elevator(
            int number,
            List<Floor> floors,
            BlockingQueue<Elevator> queue,
            Elevator flagToStop,
            int nextDestinationToStop
    ) {
        super("Elevator-" + number);
        this.id = number;
        this.currentState = EMPTY_STAND;
        this.elevatorsQueue = queue;
        this.remainingCapacity = liftingCapacity;
        this.floors.addAll(floors);
        this.flagToStop = flagToStop;
        this.nextDestinationToStop = nextDestinationToStop;
    }

    @SneakyThrows
    @Override
    public void run() {

        while (true) {

            if (nextDestination == 0) {
                synchronized (destinationChangedMonitor) {
                    if (nextDestination == nextDestinationToStop) {
                        break;
                    }
                    destinationChangedMonitor.wait();
                }
            }
            if (nextDestination == nextDestinationToStop) {
                break;
            }

            int numOfFloorsToPass = Math.abs(currFloorNum - nextDestination);

            for (int i = 0; i < numOfFloorsToPass; ++i) {
                synchronized (destinationChangedMonitor) {
                    destinationChangedMonitor.wait(speedOneFloor);
                    if (nextDestination == nextDestinationToStop) {
                        break;
                    }
                }
                recalculateCurrentFloorNum();
                if (wasDestinationChanged) {
                    break;
                }
            }
            if (nextDestination == nextDestinationToStop) {
                break;
            }

            if (!wasDestinationChanged) {
                Thread.sleep(timeOpenDoors);
                if (nextDestination == nextDestinationToStop) {
                    break;
                }
                arrivedOnFloor(currFloorNum);
                Thread.sleep(timeCloseDoors);
                log.info("\nAfter arriving " + currFloorNum + ": " + this);
            }
            wasDestinationChanged = false;
            if (nextDestination == nextDestinationToStop) {
                break;
            }
        }
        elevatorsQueue.put(flagToStop);
        log.info("Finished");
    }

    @SneakyThrows
    public synchronized void arrivedOnFloor(int currFloorNum) {

        Floor currentFloor = floors.stream()
                .filter(it -> it.getNumber() == currFloorNum)
                .findFirst()
                .orElse(null);

        checkNotNull(currentFloor);
        log.info("\nArrived on floor " + currFloorNum + ": " + this);

        buttonPressedDown.remove(currFloorNum);
        buttonPressedUp.remove(currFloorNum);
        calls.remove(currFloorNum);

        currentFloor.elevatorArrived(this);
        elevatorsQueue.put(this);
    }

    public synchronized void dropPassenger(Passenger passenger) {
        remainingCapacity += passenger.getWeight();
    }

    public synchronized boolean doesPassengerFit(Passenger passenger) {
        return remainingCapacity >= passenger.getWeight();
    }

    public synchronized void putPassenger(Passenger passenger) {
        checkArgument(doesPassengerFit(passenger));

        remainingCapacity -= passenger.getWeight();
        floorToPassengersDrop.computeIfAbsent(passenger.getRequiredFloor(),
                floorNum -> new HashSet<>()).add(passenger);

        passengerClickElevatorButton(passenger);
    }

    private void passengerClickElevatorButton(Passenger passenger) {

        int currentFloorNum = passenger.getStartFloor();
        if (passenger.getRequiredFloor() < currentFloorNum) {
            buttonPressedDown.add(passenger.getRequiredFloor());
        } else {
            buttonPressedUp.add(passenger.getRequiredFloor());
        }
    }

    private void recalculateCurrentFloorNum() {
        if ((currentState == GOING_DOWN) || (currentState == EMPTY_GO_TO_CALL && nextDestination < currFloorNum)) {
            --currFloorNum;
        } else if ((currentState == GOING_UP) || (currentState == EMPTY_GO_TO_CALL && nextDestination > currFloorNum)) {
            ++currFloorNum;
        }
    }

    public ElevatorCurrentState getCurrentState() {
        return currentState;
    }

    public synchronized SortedSet<Integer> getButtonPressedDown() {
        return new TreeSet<>(buttonPressedDown);
    }

    public synchronized SortedSet<Integer> getButtonPressedUp() {
        return new TreeSet<>(buttonPressedUp);
    }

    public Map<Integer, Set<Passenger>> getFloorToPassengersDrop() {
        return floorToPassengersDrop;
    }

    public long getSpeedOneFloor() {
        return speedOneFloor;
    }

    public long getTimeToStopOnFloor() {
        return timeOpenDoors + timeCloseDoors;
    }

    public void setCurrentState(ElevatorCurrentState currentState) {
        this.currentState = currentState;
    }

    public void setNextDestination(int nextDestination) {
        synchronized (destinationChangedMonitor) {
            this.nextDestination = nextDestination;
            wasDestinationChanged = true;
            destinationChangedMonitor.notify();
        }
    }

    public int getNextDestination() {
        return nextDestination;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public synchronized NavigableSet<Integer> getCalls() {
        return new TreeSet<>(calls);
    }

    public synchronized void removeCall(int x) {
        calls.remove(x);
    }

    public synchronized void addCall(int x) {
        calls.add(x);
    }

    public int getCurrFloorNum() {
        return currFloorNum;
    }

    public boolean isWithoutPassengers() {
        return remainingCapacity == liftingCapacity;
    }

    public int getElevatorId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Elevator elevator = (Elevator) o;
        return Objects.equals(id, elevator.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public synchronized String toString() {
        return "Elevator{" +
                "id=" + id +
                ", currentFloor=" + currFloorNum +
                ", state=" + currentState +
                ", buttonPressedDown=" + buttonPressedDown +
                ", buttonPressedUp=" + buttonPressedUp +
                ", passengers=" + floorToPassengersDrop +
                ", remainingCapacity=" + remainingCapacity +
                ", nextDestination=" + nextDestination +
                ", calls=" + calls +
                "}\n";
    }
}

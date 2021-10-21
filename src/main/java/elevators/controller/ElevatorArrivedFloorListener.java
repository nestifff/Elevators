package elevators.controller;

import elevators.elevator.Elevator;

public interface ElevatorArrivedFloorListener {
    void elevatorArrivedOnFloor(Elevator elevator, int floorNum);
}

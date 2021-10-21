package statistics;

import lombok.Data;

@Data
public class ElevatorStatisticsItem {

    private int numOfPeopleTransported;
    private int numOfFloorsPassed;
    private int numOfStopsOnFloors;

    public void increasePeopleTransported(int inc) {
        this.numOfPeopleTransported += inc;
    }

    public void increaseFloorsPassed(int inc) {
        this.numOfFloorsPassed += inc;
    }

    public void increaseStopsOnFloors(int inc) {
        this.numOfStopsOnFloors += inc;
    }
}

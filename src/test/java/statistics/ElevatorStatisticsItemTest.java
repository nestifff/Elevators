package statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorStatisticsItemTest {

    @Test
    public void increaseAll_allRight() {
        ElevatorStatisticsItem item = new ElevatorStatisticsItem();
        item.increasePeopleTransported(5);
        item.increaseStopsOnFloors(2);
        item.increaseFloorsPassed(3);

        assertEquals(item.getNumOfPeopleTransported(), 5);
        assertEquals(item.getNumOfStopsOnFloors(), 2);
        assertEquals(item.getNumOfFloorsPassed(), 3);
    }

}
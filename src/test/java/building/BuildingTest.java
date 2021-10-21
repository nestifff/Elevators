package building;

import building.floor.FirstFloor;
import building.floor.Floor;
import building.floor.LastFloor;
import building.floor.MiddleFloor;
import elevators.controller.commonController.ElevatorsController;
import elevators.elevator.Elevator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static constants.GlobalApplicationConstants.COUNT_OF_ELEVATORS;
import static constants.GlobalApplicationConstants.COUNT_OF_FLOORS;
import static elevators.elevator.ElevatorCurrentState.EMPTY_STAND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BuildingTest {

    @Test
    public void create_allRight() {
        ElevatorsController controller = mock(ElevatorsController.class);
        final ArgumentCaptor<List<Floor>> createdFloors = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<List<Elevator>> createdElevators = ArgumentCaptor.forClass(List.class);

        Building.create(controller);

        verify(controller).setFloors(createdFloors.capture());
        verify(controller).setElevators(createdElevators.capture());
        assertEquals(COUNT_OF_FLOORS, createdFloors.getValue().size());
        assertEquals(COUNT_OF_ELEVATORS, createdElevators.getValue().size());

        assertTrue(createdElevators.getValue()
                .stream()
                .allMatch(it -> it.getCurrentState() == EMPTY_STAND)
        );
        assertTrue(createdFloors.getValue().get(0) instanceof FirstFloor);
        assertTrue(createdFloors.getValue().get(COUNT_OF_FLOORS - 1) instanceof LastFloor);
        assertTrue(createdFloors.getValue().get(1) instanceof MiddleFloor);
        assertEquals(createdFloors.getValue().get(1).getNumber(), 2);
    }

}
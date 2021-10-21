package building.passengerGenerator;

import building.floor.FirstFloor;
import building.floor.Floor;
import building.passenger.Passenger;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static constants.GlobalApplicationConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PassengersGeneratorTest {

    private static Floor floor;
    private static ButtonOnClickData buttonOnClickData;
    private static PassengersGenerator passengersGenerator;

    @BeforeEach
    public void init() {
        floor = mock(Floor.class);
        buttonOnClickData = mock(ButtonOnClickData.class);
        when(floor.getNumber()).thenReturn(2);
        passengersGenerator = new PassengersGenerator(floor, buttonOnClickData);
    }


    @Test
    public void create_allRight() {
        Floor floor1 = mock(Floor.class);
        PassengersGenerator passengersGenerator1 = new PassengersGenerator(floor1, buttonOnClickData);
        verify(floor1, times(COUNT_OF_FLOORS + 1)).getNumber();
    }

    @SneakyThrows
    @Test
    public void run_workOneTimeAfterStop() {
        passengersGenerator.start();
        TimeUnit.SECONDS.sleep(1);
        passengersGenerator.finish();
        passengersGenerator.join();

        verify(floor).passengerGenerated(any());
        verify(floor).sendInQueueToStop(buttonOnClickData);
    }

    @SneakyThrows
    @Test
    public void run_firstFloorWorkOneTimeAfterStop() {
        FirstFloor firstFloor = mock(FirstFloor.class);
        PassengersGenerator passengersGenerator1 = new PassengersGenerator(firstFloor, buttonOnClickData);

        when(firstFloor.getNumber()).thenReturn(1);
        passengersGenerator1.start();
        TimeUnit.SECONDS.sleep(1);
        passengersGenerator1.finish();
        passengersGenerator1.join();

        verify(firstFloor).passengerGenerated(any());
        verify(firstFloor).sendInQueueToStop(buttonOnClickData);
    }

    @Test
    public void getRandomPassenger_repeatedlyReturnRandom() {
        Passenger p1 = passengersGenerator.getRandomPassenger();
        Passenger p2 = passengersGenerator.getRandomPassenger();

        assertNotEquals(p2, p1);
    }

    @Test
    public void getRandomPassenger_createCorrectFields() {
        List<Passenger> passengers = new ArrayList<>();
        when(floor.getNumber()).thenReturn(2);

        IntStream.range(0, 50).forEach(it ->
                passengers.add(passengersGenerator.getRandomPassenger()));

        boolean startFloorCorrect = passengers.stream().allMatch(it -> it.getStartFloor() == 2);
        boolean requiredFloorCorrect = passengers.stream()
                .allMatch(it -> it.getRequiredFloor() != 2 && it.getRequiredFloor() > 0 &&
                        it.getRequiredFloor() <= COUNT_OF_FLOORS);

        boolean weightCorrect = passengers.stream()
                .allMatch(it -> it.getWeight() >= MIN_PASSENGER_WEIGHT &&
                        it.getWeight() <= MAX_PASSENGER_WEIGHT);

        boolean hasPassengersSitAnyElevator = passengers.stream().anyMatch(Passenger::isWillSitOnAnyElevator);
        boolean hasPassengersSitElevatorOnlySuit = passengers.stream().anyMatch(it -> !it.isWillSitOnAnyElevator());

        assertTrue(startFloorCorrect);
        assertTrue(requiredFloorCorrect);
        assertTrue(weightCorrect);
        assertTrue(hasPassengersSitAnyElevator);
        assertTrue(hasPassengersSitElevatorOnlySuit);

    }

}
package building;

import building.floor.FirstFloor;
import building.floor.Floor;
import building.floor.LastFloor;
import building.floor.MiddleFloor;
import building.passengerGenerator.PassengersGenerator;
import elevators.controller.commonController.ElevatorsController;
import elevators.controller.elevatorArrivedFloor.ThreadProcessorElevatorArrivedFloor;
import elevators.controller.floorButtonOnClick.ButtonOnClickData;
import elevators.controller.floorButtonOnClick.FloorButtonOnClickProcessor;
import elevators.elevator.Elevator;
import lombok.SneakyThrows;
import statistics.ElevatorsStatistics;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.google.common.base.Preconditions.checkArgument;
import static constants.GlobalApplicationConstants.*;
import static elevators.button.ButtonDirection.UP;

public class Building {

    private static final int flagToStopElevators = NEXT_DESTINATION_TO_STOP_ELEVATORS;
    private static final Elevator elevatorFlagToStop =
            new Elevator(-1, List.of(), new LinkedBlockingDeque<>(), null, flagToStopElevators);
    private static final ButtonOnClickData buttonFlagToStop = new ButtonOnClickData(-1, UP);

    private final List<Floor> floors;
    private final List<Elevator> elevators;
    private final List<PassengersGenerator> passengersSpawns;

    private final BlockingQueue<Elevator> elevatorsQueue;
    private final BlockingQueue<ButtonOnClickData> buttonsDataQueue;

    private ThreadProcessorElevatorArrivedFloor threadProcessorElevatorArrived;
    private ElevatorsStatistics elevatorArrivedFloorStatistic;
    private FloorButtonOnClickProcessor floorButtonOnClickProcessor;

    private final ElevatorsController elevatorsController;

    public static Building create(ElevatorsController elevatorsController) {

        Building building = new Building(elevatorsController);

        building.createFloors(COUNT_OF_FLOORS);
        building.createElevators(COUNT_OF_ELEVATORS);
        building.createPassengersSpawns(COUNT_OF_FLOORS);

        elevatorsController.setElevators(building.elevators);
        elevatorsController.setFloors(building.floors);

        building.createEventListeners();

        return building;
    }

    private Building(ElevatorsController elevatorsController) {

        this.elevatorsController = elevatorsController;

        this.elevatorsQueue = new LinkedBlockingDeque<>(COUNT_OF_ELEVATORS);
        this.buttonsDataQueue = new LinkedBlockingDeque<>(COUNT_OF_FLOORS);

        floors = new ArrayList<>();
        elevators = new ArrayList<>();
        passengersSpawns = new ArrayList<>();
    }

    private void createEventListeners() {

        checkArgument(!elevators.isEmpty());
        checkArgument(!floors.isEmpty());
        checkArgument(!passengersSpawns.isEmpty());

        elevatorArrivedFloorStatistic = new ElevatorsStatistics(elevatorsController,
                elevators, floors, INTERVAL_TO_PRINT_STATISTICS);
        threadProcessorElevatorArrived = new ThreadProcessorElevatorArrivedFloor(elevatorFlagToStop,
                elevatorsQueue, elevatorArrivedFloorStatistic);
        floorButtonOnClickProcessor = new FloorButtonOnClickProcessor(buttonFlagToStop, flagToStopElevators,
                buttonsDataQueue, elevatorsController);
    }

    public void startAll() {

        elevatorArrivedFloorStatistic.start();
        threadProcessorElevatorArrived.start();
        floorButtonOnClickProcessor.start();

        passengersSpawns.forEach(Thread::start);
        elevators.forEach(Thread::start);
    }

    @SneakyThrows
    public void finishAll() {

        elevatorArrivedFloorStatistic.finish();
        passengersSpawns.forEach(PassengersGenerator::finish);

        elevatorArrivedFloorStatistic.writeToFile(new FileWriter("statisticsElevators.txt"));
    }

    private void createFloors(int numOfFloors) {
        checkArgument(numOfFloors >= 2);
        floors.add(FirstFloor.create(this, buttonsDataQueue));
        for (int i = 2; i < numOfFloors; ++i) {
            floors.add(MiddleFloor.create(this, i, buttonsDataQueue));
        }
        floors.add(LastFloor.create(this, buttonsDataQueue));
    }

    private void createElevators(int numOfElevators) {
        checkArgument(numOfElevators > 0);
        for (int i = 1; i <= numOfElevators; ++i) {
            elevators.add(new Elevator(i, floors, elevatorsQueue, elevatorFlagToStop, flagToStopElevators));
        }
        System.out.println(elevators.toString());
    }

    private void createPassengersSpawns(int numOfFloors) {
        for (int i = 1; i <= numOfFloors; ++i) {
            passengersSpawns.add(new PassengersGenerator(floors.get(i - 1), buttonFlagToStop));
        }
    }
}

package constants;

public class GlobalApplicationConstants {

    public static int INTERVAL_TO_PRINT_STATISTICS = 15;

    public static int MAX_PASSENGER_WEIGHT = 150;
    public static int MIN_PASSENGER_WEIGHT = 30;
    public static int LIFTING_CAPACITY = 800;
    public static long INTERVAL_TO_GENERATE_PASSENGERS = 5;
    public static long INTERVAL_TO_GENERATE_PASSENGERS_FIRST_FLOOR = 6;

    public static int COUNT_OF_FLOORS = 30;
    public static int COUNT_OF_ELEVATORS = 10;

    // all in milliseconds
    public static long SPEED_ONE_FLOOR = 150;
    public static long TIME_OPEN_DOORS = 200;
    public static long TIME_CLOSE_DOORS = 200;

    public static long TIME_RETRY_FIND_ELEVATOR_FOR_CALL =
            SPEED_ONE_FLOOR * (COUNT_OF_FLOORS / 4) + TIME_OPEN_DOORS + TIME_CLOSE_DOORS;

    public static int NEXT_DESTINATION_TO_STOP_ELEVATORS = -1;

}

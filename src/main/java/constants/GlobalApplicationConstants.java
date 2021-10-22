package constants;

public class GlobalApplicationConstants {

    public static int INTERVAL_TO_PRINT_STATISTICS = 2;

    public static int MAX_PASSENGER_WEIGHT = 150;
    public static int MIN_PASSENGER_WEIGHT = 30;
    public static int LIFTING_CAPACITY = 400;
    public static long INTERVAL_TO_GENERATE_PASSENGERS = 10;
    public static long INTERVAL_TO_GENERATE_PASSENGERS_FIRST_FLOOR = 4;

    public static int COUNT_OF_FLOORS = 30;
    public static int COUNT_OF_ELEVATORS = 5;

    // all in milliseconds
    public static long SPEED_ONE_FLOOR = 150;
    public static long TIME_OPEN_DOORS = 200;
    public static long TIME_CLOSE_DOORS = 200;

    public static long TIME_RETRY_FIND_ELEVATOR_FOR_CALL =
            SPEED_ONE_FLOOR * (COUNT_OF_FLOORS / 4) + TIME_OPEN_DOORS + TIME_CLOSE_DOORS;

    public static int NEXT_DESTINATION_TO_STOP_ELEVATORS = -1;

}

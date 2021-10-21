package building.passengerGenerator;

public class RandomNumberCreator {

    public static int getRandom(int min, int max) {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }
}

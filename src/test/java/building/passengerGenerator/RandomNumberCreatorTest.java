package building.passengerGenerator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static building.passengerGenerator.RandomNumberCreator.getRandom;
import static org.junit.jupiter.api.Assertions.*;

class RandomNumberCreatorTest {

    @Test
    public void getRandom_allRight() {
        int random = getRandom(1, 5);
        assertTrue(random <= 5);
        assertTrue(random >= 1);
    }

    @Test
    public void getRandom_getAllPossibleValues() {
        List<Integer> randoms = new ArrayList<>();
        IntStream.range(0, 100).forEach(it -> randoms.add(getRandom(1, 3)));

        assertTrue(randoms.contains(1));
        assertTrue(randoms.contains(2));
        assertTrue(randoms.contains(3));
    }

}
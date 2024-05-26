package top.weixiansen574.lyreplayer2;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void xyTest(){
        Clicker.Coordinate coordinate = new Clicker.Coordinate(10, 10);
        Clicker.Coordinate coordinate1 = new Clicker.Coordinate(10, 10);
        assertEquals(coordinate.hashCode(), coordinate1.hashCode());
    }
}
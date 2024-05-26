package top.weixiansen574.lyreplayer2;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("top.weixiansen574.lyreplayer2", appContext.getPackageName());
    }

    public void bitTest(){
        int index = 1;   // 00000001
        int action = 5;  // 00000101

        // 将 index 左移 8 位，然后与 action 进行按位或操作
        int result = (index << 8) | action;

        // 打印结果，验证正确性
        System.out.printf("Result: %016d\n", result); // 使用16位二进制格式打印结果

        // 如果需要从 result 中提取 index 和 action
        int extractedIndex = (result >> 8) & 0xFF;
        int extractedAction = result & 0xFF;

        // 打印提取的值，验证正确性
        System.out.printf("Extracted Index: %08d\n", extractedIndex);
        System.out.printf("Extracted Action: %08d\n", extractedAction);
    }
}
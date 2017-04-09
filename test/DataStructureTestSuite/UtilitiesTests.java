package DataStructureTestSuite;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import LockFreeDataStructures.MDList;

public class UtilitiesTests
{
    private static final int DIMENSIONS = 3;
    private static final int BASE = 4;
    // private static MDList<Integer> testList;

    @BeforeClass
    public static void setUpBeforeClass () throws Exception
    {
        // testList = new MDList<>(DIMENSIONS, KEY_SPACE);
    }

    @AfterClass
    public static void tearDownAfterClass () throws Exception
    {
    }

    @Before
    public void setUp () throws Exception
    {
    }

    @After
    public void tearDown () throws Exception
    {
    }

    @Test
    public void testSetAndGetMark ()
    {
        Object testObject = new Object();
        AtomicStampedReference<Object> testAsr = new AtomicStampedReference<>(testObject, 0);
        MDList.SetMark(testAsr, MDList.Fadp);

        assert(MDList.IsMarked(testAsr, MDList.Fadp));
        assert(!MDList.IsMarked(testAsr, MDList.Fdel));
    }

    @Test
    public void testClearAndGetMark ()
    {
        Object testObject = new Object();
        AtomicStampedReference<Object> testAsr = new AtomicStampedReference<>(testObject, 0xFFFFFFFF);
        MDList.ClearMark(testAsr, MDList.Fall);

        assert(!MDList.IsMarked(testAsr, MDList.Fall));
    }

    @Test
    public void testKeyToCoord ()
    {
        int[] expectedOutput = { 1, 2, 3 };
        int key = 27;
        int[] actualOutput = MDList.KeyToCoord(key, BASE, DIMENSIONS);

        assert(Arrays.equals(expectedOutput, actualOutput));
    }

}

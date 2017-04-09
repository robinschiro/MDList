package DataStructureTestSuite;

import static org.junit.Assert.*;

import java.util.Arrays;

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
//    private static MDList<Integer> testList;

    @BeforeClass
    public static void setUpBeforeClass () throws Exception
    {
//        testList = new MDList<>(DIMENSIONS, KEY_SPACE);
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
    public void testSetMark ()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testClearMark ()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testIsMarked ()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testKeyToCoord ()
    {
        int[] expectedOutput = {1, 2, 3};
        int key = 27;
        int[] actualOutput = MDList.KeyToCoord(key, BASE, DIMENSIONS);

        assertTrue(Arrays.equals(expectedOutput, actualOutput));
    }

}

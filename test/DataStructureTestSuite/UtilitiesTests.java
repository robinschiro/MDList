package DataStructureTestSuite;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import LockFreeDataStructures.ReferenceUtilities;
import LockFreeDataStructures.MDList;

public class UtilitiesTests
{
    @BeforeClass
    public static void setUpBeforeClass () throws Exception
    {
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
        ReferenceUtilities.SetMark(testAsr, MDList.Fadp);

        assert(ReferenceUtilities.IsMarked(testAsr, MDList.Fadp));
        assert(!ReferenceUtilities.IsMarked(testAsr, MDList.Fdel));
    }

    @Test
    public void testClearAndGetMark ()
    {
        Object testObject = new Object();
        AtomicStampedReference<Object> testAsr = new AtomicStampedReference<>(testObject, 0xFFFFFFFF);
        ReferenceUtilities.ClearMark(testAsr, MDList.Fall);

        assert(!ReferenceUtilities.IsMarked(testAsr, MDList.Fall));
    }

    @Test
    public void testKeyToCoord ()
    {
        int base = 4;
        int dimensions = 3;
        int[] expectedOutput = { 1, 2, 3 };
        int key = 27;
        int[] actualOutput = MDList.KeyToCoord(key, base, dimensions);

        assert(Arrays.equals(expectedOutput, actualOutput));
    }
}

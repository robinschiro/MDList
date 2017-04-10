package DataStructureCorrectnessTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import LockFreeDataStructures.SkipList;

public class SkipListTests
{
    private static final int MAX_LEVEL = 5;
    private static SkipList<Integer> testList;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        testList = new SkipList<>(MAX_LEVEL);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testInsertAndFindSuccess ()
    {
        int testKey = 7;
        int testValue = 14;
        testList.Insert(testKey, testValue);
        assert(testValue == testList.Find(testKey));
    }

    @Test
    public void testInsertAndFindFail ()
    {
        testList.Insert(7, 14);
        testList.Insert(3, 45);
        testList.Insert(72, 18);

        Integer value = testList.Find(4);
        assert(null == value);
        value = testList.Find(0);
        assert(null == value);
        value = testList.Find(2);
        assert(null == value);
        value = testList.Find(100);
        assert(null == value);
    }

    @Test
    public void testInsertAndDeleteAndFind ()
    {
        Integer value = testList.Delete(1);
        assert(null == value);
        testList.Insert(4, 83);
        testList.Insert(8, 123);
        value = testList.Delete(9);
        assert(null == value);
        value = testList.Delete(4);
        assert(83 == value);
        value = testList.Find(4);
        assert(null == value);
    }
}

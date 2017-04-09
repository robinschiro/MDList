package DataStructureTestSuite;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import LockFreeDataStructures.MDList;

public class MDListTests
{
    private MDList<Integer> testList;

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
        testList = new MDList<>(3, 64);
    }

    @After
    public void tearDown () throws Exception
    {
    }

    @Test
    public void testInsertAndFind ()
    {
        // Sample data.
        int key = 5;
        int expectedValue = 6;

        // Insert and check sample node.
        testList.Insert(key, expectedValue);
        int actualValue = testList.Find(key);
        assert(actualValue == expectedValue);
    }

    @Test
    public void testDelete ()
    {
        fail("Not yet implemented");
    }

}

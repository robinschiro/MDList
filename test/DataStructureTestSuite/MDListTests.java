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
    public void testInsertMany ()
    {
        // Add some nodes to the list.
        testList.Insert(1, 1);
        testList.Insert(2, 2);
        testList.Insert(3, 3);
    }

    @Test
    // Insert a node and then remove it.
    public void testValidDelete ()
    {
        // Sample data.
        int key = 5;
        int expectedValue = 6;

        // Insert node.
        testList.Insert(key, expectedValue);

        // Delete the node.
        Integer actualValue = testList.Delete(key);

        // Verify success.
        assert(actualValue == expectedValue);

        // Verify that node is no longer in list.
        actualValue = testList.Find(key);
        assert(null == actualValue);
    }

    @Test
    // Attempt to delete a node that we know is not in the list.
    public void testInvalidDelete()
    {
        int key = 1;

        // Delete the node.
        Integer actualValue = testList.Delete(key);

        assert(null == actualValue);
    }

    @Test
    public void testPrint ()
    {
        // Add some nodes to the list.
        testList.Insert(1, 10);
        testList.Insert(2, 20);
        testList.Insert(3, 30);
        testList.Insert(4, 40);
        testList.Insert(10, 100);
        testList.Insert(11, 110);
        testList.Insert(12, 120);
        testList.Insert(13, 130);
        // Print the list.
        testList.PrintList();
        // Add more nodes to the list.
        testList.Insert(5, 50);
        testList.Insert(6, 60);
        testList.Insert(7, 70);
        testList.Insert(8, 80);
        testList.Insert(9, 90);
        // Print the list again.
        testList.PrintList();
    }
}

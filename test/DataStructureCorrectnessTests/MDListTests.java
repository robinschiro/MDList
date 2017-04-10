package DataStructureCorrectnessTests;

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
    public void testInsertOneAndFind ()
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
    public void testInvalidInput ()
    {
        // Add some invalid nodes to the list.
        try {
            testList.Insert(0, 1);
            fail( "Did not throw exception 1." );
        } catch (IllegalArgumentException expectedException) {
        }
        try {
            testList.Insert(64, 3);
            fail( "Did not throw exception 2." );
        } catch (IllegalArgumentException expectedException) {
        }
        try {
            testList.Insert(-2, 5);
            fail( "Did not throw exception 3." );
        } catch (IllegalArgumentException expectedException) {
        }
        try {
            testList.Find(0);
            fail( "Did not throw exception 3." );
        } catch (IllegalArgumentException expectedException) {
        }
        try {
            testList.Find(64);
            fail( "Did not throw exception 3." );
        } catch (IllegalArgumentException expectedException) {
        }
        testList.Insert(5, 234);
        testList.PrintList();
        Integer test = testList.Find(5);
        assert(test == 234);
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

    @Test
    public void testPrintDelete ()
    {
        testList.Insert(1, 10);
        testList.Insert(2, 20);
        testList.Insert(3, 30);
        testList.Insert(5, 50);
        testList.Insert(10, 100);
        testList.Insert(11, 110);
        testList.Insert(12, 120);
        testList.Insert(13, 130);
        testList.Delete(10);
        testList.Delete(5);
        testList.PrintList();
        testList.Insert(8, 234);
        testList.PrintList();
        testList.Insert(4, 123);
        testList.PrintList();
    }
    
    @Test
    public void testChildAdoption ()
    {
        testList.Insert(1, 11);
        testList.Insert(2, 22);
        testList.Insert(4, 44);
        testList.Insert(5, 55);
        testList.Insert(8, 88);
        testList.Insert(18, 1818);
        testList.Insert(19, 1919);
        testList.Insert(22, 2222);
        testList.Insert(33, 3333);
        testList.Insert(34, 3434);
        testList.Insert(36, 3636);
        testList.Insert(40, 4040);
        testList.Insert(48, 4848);
        testList.Insert(49, 4949);
        testList.Insert(51, 5151);
        testList.PrintList();
        testList.Insert(32, 3232);
        testList.PrintList();
        Integer test = testList.Find(48);
        assert(test == 4848);
        test = testList.Find(36);
        assert(test == 3636);
        testList.Delete(18);
        test = testList.Find(18);
        assert(test == null);
        testList.PrintList();
        testList.Insert(16, 1616);
        testList.PrintList();
        test = testList.Find(18);
        assert(test == null);
        test = testList.Find(16);
        assert(test == 1616);
    }
}

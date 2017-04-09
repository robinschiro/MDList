package DataStructureTestSuite;

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
    public void testInsertAndContainsTrue ()
    {
        boolean test = testList.Add(7, 14);
        assert(test == true);
        test = testList.Contains(7);
        assert(test == true);
    }
    
    @Test
    public void testInsertAndContainsFalse ()
    {
        boolean test = testList.Add(7, 14);
        assert(test == true);
        test = testList.Add(3, 45);
        assert(test == true);
        test = testList.Add(72, 18);
        assert(test == true);
        test = testList.Contains(4);
        assert(test == false);
        test = testList.Contains(0);
        assert(test == false);
        test = testList.Contains(2);
        assert(test == false);
        test = testList.Contains(100);
        assert(test == false);
    }
    
    @Test
    public void testInsertAndDeleteAndContains ()
    {
        boolean test = testList.Remove(1);
        assert(test == false);
        test = testList.Add(4, 83);
        assert(test == true);
        test = testList.Add(8, 123);
        assert(test == true);
        test = testList.Remove(9);
        assert(test == false);
        test = testList.Remove(4);
        assert(test == true);
        test = testList.Contains(4);
        assert(test == false);
    }

}

package DataStructurePerformanceTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import DataStructurePerformanceTests.InstructionSetGenerator.InstructionDescriptor;
import LockFreeDataStructures.ILockFreeDictionary;
import LockFreeDataStructures.MDList;
import LockFreeDataStructures.SkipList;;

public class PerformanceTests
{

    private InstructionDescriptor[][] instructionSets;
    private SkipList<Integer> testSkipList;
    private MDList<Integer> testMDList;
    private final int keySpace = 1024;
    private final int dimensions = 5;

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
        testMDList = new MDList<>(dimensions, keySpace);
        testSkipList = new SkipList<>(16);
        instructionSets = InstructionSetGenerator.GenerateInstructionsForThreads(4, 2000000, keySpace, .5, .5, 0);
        System.out.println("Finished creating instruction sets");
    }

    @After
    public void tearDown () throws Exception
    {
    }

    @Test
    public void testMDList2Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testMDList, PerformanceTestUtilities.GetSubsetOfInstructionSets(2, instructionSets));
    }

    @Test
    public void testSkipList2Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testSkipList, PerformanceTestUtilities.GetSubsetOfInstructionSets(2, instructionSets));
    }
}

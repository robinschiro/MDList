package DataStructurePerformanceTests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import DataStructurePerformanceTests.InstructionSetGenerator.InstructionDescriptor;
import LockFreeDataStructures.MDList;
import LockFreeDataStructures.SkipList;;

public class PerformanceTests_12_50I50D00F
{
    private InstructionDescriptor[][] instructionSets;
    private SkipList<Integer> testSkipList;
    private MDList<Integer> testMDList;
    private static final int KEY_SPACE = (int)Math.round(Math.pow(2, 12));
    private static final double INSERT_PROPORTION = 0.5;
    private static final double DELETE_PROPORTION = 0.5;
    private static final double FIND_PROPORTION = 0.0;

    private final int dimensions = MDList.CalculateDimensionsFromBase(2, KEY_SPACE);

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
        testMDList = new MDList<>(dimensions, KEY_SPACE);
        PerformanceTestUtilities.PrefillDictionary(testMDList, KEY_SPACE/2);
        testSkipList = new SkipList<>(PerformanceTestUtilities.SKIP_LIST_HEIGHT);
        PerformanceTestUtilities.PrefillDictionary(testMDList, KEY_SPACE/2);
        instructionSets = InstructionSetGenerator.GenerateInstructionsForThreads(PerformanceTestUtilities.MAX_THREADS, PerformanceTestUtilities.INSTRUCTIONS_PER_THREAD,
                                                                                 KEY_SPACE, INSERT_PROPORTION, DELETE_PROPORTION, FIND_PROPORTION);

        System.out.println("Finished creating instruction sets");
    }

    @After
    public void tearDown () throws Exception
    {
    }

    @Test
    public void testMDList1Thread () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testMDList, PerformanceTestUtilities.GetSubsetOfInstructionSets(1, instructionSets));
    }

    @Test
    public void testMDList2Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testMDList, PerformanceTestUtilities.GetSubsetOfInstructionSets(2, instructionSets));
    }

    @Test
    public void testMDList4Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testMDList, PerformanceTestUtilities.GetSubsetOfInstructionSets(4, instructionSets));
    }

    @Test
    public void testMDList8Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testMDList, PerformanceTestUtilities.GetSubsetOfInstructionSets(8, instructionSets));
    }

    @Test
    public void testSkipList1Thread () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testSkipList, PerformanceTestUtilities.GetSubsetOfInstructionSets(1, instructionSets));
    }

    @Test
    public void testSkipList2Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testSkipList, PerformanceTestUtilities.GetSubsetOfInstructionSets(2, instructionSets));
    }

    @Test
    public void testSkipList4Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testSkipList, PerformanceTestUtilities.GetSubsetOfInstructionSets(4, instructionSets));
    }

    @Test
    public void testSkipList8Threads () throws InterruptedException
    {
        PerformanceTestUtilities.RunPerfomanceTest(testSkipList, PerformanceTestUtilities.GetSubsetOfInstructionSets(8, instructionSets));
    }
}

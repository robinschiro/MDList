package DataStructureTestSuite;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import DataStructureTestSuite.InstructionSetGenerator.InstructionDescriptor;
import LockFreeDataStructures.ILockFreeDictionary;
import LockFreeDataStructures.MDList;;

public class PerformanceTests
{

    private InstructionDescriptor[][] instructionSets;
    private MDList<Integer> testList;

    public static void RunPerfomanceTest(ILockFreeDictionary dict, InstructionDescriptor[][] instructionSets  ) throws InterruptedException
    {
        // Create array to hold threads.
        Thread[] threads = new Thread[instructionSets.length];

        // Assign each instruction set to each thread.
        for ( int threadIndex = 0; threadIndex < instructionSets.length; threadIndex++ )
        {
            InstructionDescriptor[] instructions = instructionSets[threadIndex];
            threads[threadIndex] = new Thread(() -> ExecuteInstructions(dict, instructions));

            // Begin executing instructions on the thread.
            threads[threadIndex].start();
        }

        // Join all of the threads.
        for ( int threadIndex = 0; threadIndex < instructionSets.length; threadIndex++ )
        {
            threads[threadIndex].join();
        }
    }

    public static void ExecuteInstructions(ILockFreeDictionary dict, InstructionDescriptor[] instructions )
    {
        for ( InstructionDescriptor ins : instructions )
        {
            switch (ins.getInstructionName())
            {
                case INSERT:
                {
                    dict.Insert(ins.getKey(), ins.getKey());
                    break;
                }

                case DELETE:
                {
                    dict.Delete(ins.getKey());
                    break;
                }

                case FIND:
                {
                    dict.Find(ins.getKey());
                    break;
                }

                default:
                {
                    throw new RuntimeException("Instruction descriptor contains invalid instruction name");
                }
            }
        }
    }

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
        int keySpace = 1000;
        testList = new MDList<>(20, keySpace);
        instructionSets = InstructionSetGenerator.GenerateInstructionsForThreads(8, 1000, keySpace, .5, .5, 0);
    }

    @After
    public void tearDown () throws Exception
    {
    }

    @Test
    public void test2Threads50Insert50DeleteMDList () throws InterruptedException
    {
        RunPerfomanceTest(testList, instructionSets);
    }

}

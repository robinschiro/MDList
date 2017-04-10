package DataStructurePerformanceTests;

import DataStructurePerformanceTests.InstructionSetGenerator.InstructionDescriptor;
import LockFreeDataStructures.ILockFreeDictionary;

public class PerformanceTestUtilities
{

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

    public static InstructionDescriptor[][] GetSubsetOfInstructionSets ( int subsetSize, InstructionDescriptor[][] instructionSets )
    {
        InstructionDescriptor[][] subset = new InstructionDescriptor[subsetSize][];

        for ( int i = 0; i < subset.length; i++ )
        {
            subset[i] = instructionSets[i];
        }

        return subset;
    }
}

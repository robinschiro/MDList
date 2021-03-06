package DataStructurePerformanceTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import DataStructurePerformanceTests.InstructionSetGenerator;
import DataStructurePerformanceTests.InstructionSetGenerator.InstructionDescriptor;
import LockFreeDataStructures.MDList;

public class InstructionSetGeneratorTests
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
    public void testGenerateInstructionsValidArgs ()
    {
        int numIns = 1000;
        int keySpace = 10;
        InstructionDescriptor[] instructions = InstructionSetGenerator.GenerateInstructions(numIns, keySpace, 0.05, 0.05, 0.9);

        assert(numIns == instructions.length);

        // Verify keys are created within bounds.
        boolean keysAreValid = true;
        for ( InstructionDescriptor ins : instructions )
        {
            assert(MDList.IsKeyValid(ins.getKey(), keySpace));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGenerateInstructionsInvalidArgs ()
    {
        InstructionSetGenerator.GenerateInstructions(10, 10, 0.5, 0.5, 0.5);
    }

}

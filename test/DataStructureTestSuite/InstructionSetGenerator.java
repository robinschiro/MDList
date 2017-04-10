package DataStructureTestSuite;

import java.util.Random;

public class InstructionSetGenerator
{

    public enum InstructionName
    {
        INSERT,
        DELETE,
        FIND
    }

    public static final class InstructionDescriptor
    {
        private InstructionName instruction;
        public InstructionName getInstructionName ()
        {
            return instruction;
        }

        private int key;
        public int getKey ()
        {
            return key;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( instruction == null ) ? 0 : instruction.hashCode() );
            result = prime * result + key;
            return result;
        }

        @Override
        public boolean equals ( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( getClass() != obj.getClass() )
            {
                return false;
            }
            InstructionDescriptor other = (InstructionDescriptor) obj;
            if ( instruction != other.instruction )
            {
                return false;
            }
            if ( key != other.key )
            {
                return false;
            }
            return true;
        }

        public InstructionDescriptor( InstructionName instruction, int key )
        {
            this.instruction = instruction;
            this.key = key;
        }
    }

    public static InstructionDescriptor[][] GenerateInstructionsForThreads( int numThreads, int numInstructions, int keySpace,
                                                                          double insertProportion, double deleteProportion, double findProportion )
    {
        InstructionDescriptor[][] instructionSets = new InstructionDescriptor[numThreads][numInstructions];

        for ( int i = 0; i < instructionSets.length; i++ )
        {
            instructionSets[i] = GenerateInstructions(numInstructions, keySpace, insertProportion, deleteProportion, findProportion);
        }

        return instructionSets;
    }

    public static InstructionDescriptor[] GenerateInstructions( int numInstructions, int keySpace,
                                                                double insertProportion, double deleteProportion, double findProportion )
    {
        // Verify that proportions sum to 1.
        if ( ((insertProportion + deleteProportion + findProportion) - 1.0) > 1E-9 )
        {
            throw new IllegalArgumentException("Instruction proportions do not sum to 1.");
        }

        InstructionDescriptor[] instructions = new InstructionDescriptor[numInstructions];

        // Create RNG object for select descriptors and arguments.
        Random selector = new Random();

        InstructionName insName;

        // For each instruction descriptor that needs to be generated, select an instruction randomly (according to the proportions).
        for ( int i = 0; i < instructions.length; i++ )
        {
            double opSelection = selector.nextDouble();

            // Default to FIND.
            insName = InstructionName.FIND;

            if ( insertProportion > opSelection )
            {
                insName = InstructionName.INSERT;
            }
            else if ( (insertProportion + deleteProportion) > opSelection )
            {
                insName = InstructionName.DELETE;
            }

            // Create desciptor with a random key and add to array.
            instructions[i] = new InstructionDescriptor(insName, selector.nextInt(keySpace));
        }

        return instructions;
    }
}

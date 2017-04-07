package LockFreeDataStructures;

// This is a test

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicStampedReference;

public class MDList
{

    class Node<T>
    {
        private int key;
        private int[] mappedKey;
        private T value;
        private ArrayList<AtomicStampedReference<Node<T>>> children;
        private AdoptionDescriptor adoptDesc;

        public Node( int key, int dimensions, T value )
        {
            this.key = key;
            this.value = value;
            this.children = new ArrayList<>(dimensions);

            // Generate mapped key

        }
    }

    class AdoptionDescriptor<T>
    {
        AtomicStampedReference<Node<T>> current;
        int dimOfPred;
        int dimOfCurr;

        public AdoptionDescriptor( AtomicStampedReference<Node<T>> current, int dimOfPred, int dimOfCurr )
        {
            this.current = current;
            this.dimOfPred = dimOfPred;
            this.dimOfCurr = dimOfCurr;
        }
    }

    private int dimensions;
    private int keySpace;

    // Set upon construction
    private int base;


}

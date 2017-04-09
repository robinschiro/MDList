package LockFreeDataStructures;

// This is a test

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicStampedReference;

public class MDList<T>
{
    private static final int PRED_INDEX = 0;
    private static final int CURR_INDEX = 1;

    class Node<V>
    {
        private int key;
        private int[] mappedKey;
        private V value;
        private ArrayList<AtomicStampedReference<Node<V>>> children;
        private AdoptionDescriptor adoptDesc;

        public Node( int key, V value )
        {
            this.key = key;
            this.value = value;
            this.children = new ArrayList<>(dimensions);

            // Generate mapped key
            mappedKey = KeyToCoord(key, MDList.this.base, MDList.this.dimensions);
        }
    }

    class AdoptionDescriptor<V>
    {
        AtomicStampedReference<Node<V>> current;
        int dimOfPred;
        int dimOfCurr;

        public AdoptionDescriptor( AtomicStampedReference<Node<V>> current, int dimOfPred, int dimOfCurr )
        {
            this.current = current;
            this.dimOfPred = dimOfPred;
            this.dimOfCurr = dimOfCurr;
        }
    }

    public static final int Fadp = 0x1;
    public static final int Fdel = 0x2;
    public static final int Fall = 0x3;

    private int dimensions;
    private int keySpace;

    // Needed to convert key to set of coordinates in the MDList.
    // Calculate upon construction.
    private int base;

    // The head of the list.
    public AtomicStampedReference<Node<T>> head;

    public MDList( int dimensions, int keySpace )
    {
        this.dimensions = dimensions;
        this.keySpace = keySpace;
        this.base = (int) Math.pow(keySpace, 1 / dimensions);
        this.head = new AtomicStampedReference<>(new Node(0, null), 0);
    }

    /** Utilities **/

    public static AtomicStampedReference SetMark ( AtomicStampedReference asr, int mark )
    {
        if ( asr != null )
        {
            int[] stampHolder = { 0 };
            Object pointer = asr.get(stampHolder);
            stampHolder[0] = stampHolder[0] | mark;
            asr.attemptStamp(pointer, stampHolder[0]);
        }
        return asr;
    }

    public static AtomicStampedReference ClearMark ( AtomicStampedReference asr, int mark )
    {
        if ( asr != null )
        {
            int[] stampHolder = { 0 };
            Object pointer = asr.get(stampHolder);
            stampHolder[0] = stampHolder[0] & ~mark;
            asr.attemptStamp(pointer, stampHolder[0]);
        }
        return asr;
    }

    // Check if the specified bit (or bits) of the stamp are marked.
    // Return false if the node is null.
    public static boolean IsMarked ( AtomicStampedReference asr, int mark )
    {
        if ( asr != null )
        {
            int stamp = asr.getStamp();
            stamp = 0x3 & stamp;
            stamp = stamp & mark;
            return ( stamp == mark );
        }
        return false;
    }

    public static int[] KeyToCoord ( int key, int base, int dimensions )
    {
        int partialKey = key;
        int[] mappedKey = new int[dimensions];

        for ( int dim = dimensions - 1; partialKey > 0; dim-- )
        {
            mappedKey[dim] = partialKey % base;
            partialKey = partialKey / base;
        }

        return mappedKey;
    }

    // Determine if either the AtomicStampedReference or the Node that it is wrapping is null.
    private boolean IsRefNull ( AtomicStampedReference<Node<T>> nodeAsr )
    {
        if ( null != nodeAsr )
        {
            if ( null == nodeAsr.getReference() )
            {
                throw new RuntimeException("Node inside AtomicStampedReference is null. This should not happen");
            }
            return false;
        }
        return true;
    }

    // Given a key, find the associated value if a node with the key exists in the MDList.
    private T Find ( int key )
    {
        // Create an arraylist in order to pass the pred and curr atomic references by reference.
        ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr = new ArrayList<>(2);
        // Create arrays for the ints to pass by reference.
        int[] dimOfPred = { 0 };
        int[] dimOfCurr = { 0 };

        // Start locating the pred and curr nodes from the head node.
        predAndCurrAsr.set(CURR_INDEX, head);

        LocatePred(KeyToCoord(key, base, dimensions), predAndCurrAsr, dimOfPred, dimOfCurr);

        // The find is successful if and only if dimOfCurr int matches the total number of dimensions.
        if ( dimOfCurr[0] == dimensions )
        {
            return predAndCurrAsr.get(CURR_INDEX).getReference().value;
        }
        return null;
    }

    private void LocatePred ( int[] mappedKey, ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr, int[] dimOfPred, int[] dimOfCurr )
    {
        AtomicStampedReference<Node<T>> predAsr = predAndCurrAsr.get(PRED_INDEX);
        AtomicStampedReference<Node<T>> currAsr = predAndCurrAsr.get(CURR_INDEX);

        while ( dimOfCurr[0] < dimensions )
        {
            while ( !IsRefNull(currAsr) && mappedKey[dimOfCurr[0]] > currAsr.getReference().mappedKey[dimOfCurr[0]] )
            {
                predAsr = currAsr;
                dimOfPred[0] = dimOfCurr[0];
                AdoptionDescriptor<T> adesc = currAsr.getReference().adoptDesc;

                if ( adesc != null && dimOfPred[0] >= adesc.dimOfPred && dimOfPred[0] <= adesc.dimOfCurr )
                {
                    FinishInserting(currAsr, adesc);
                }

                currAsr = ClearMark(currAsr.getReference().children.get(dimOfCurr[0]), Fall);
            }
            if ( IsRefNull(currAsr) || mappedKey[dimOfCurr[0]] < currAsr.getReference().mappedKey[dimOfCurr[0]] )
            {
                break;
            }
            else
            {
                dimOfCurr[0]++;
            }
        }

        predAndCurrAsr.set(PRED_INDEX, predAsr);
        predAndCurrAsr.set(CURR_INDEX, currAsr);
    }

    private void FinishInserting ( AtomicStampedReference<Node<T>> nodeAsr, AdoptionDescriptor<T> adoptDesc )
    {

    }

}

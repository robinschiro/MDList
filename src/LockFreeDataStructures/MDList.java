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
            mappedKey = KeyToCoord(key);
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

    private static final int Fadp = 0x1; // Flag 0001
    private static final int Fdel = 0x2; // Flag 0010
    private static final int Fall = 0x3; // Flag 0011
    private static final int StampInc = 0x4; // Increment Stamp by 0100 so it doesn't mess with flags

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

    private AtomicStampedReference<Node<T>> SetMark( AtomicStampedReference<Node<T>> node, int mark )
    {
        if ( node != null )
        {
            int[] stampHolder = { 0 };
            Node<T> pointer = node.get(stampHolder);
            stampHolder[0] = stampHolder[0] | mark;
            node.attemptStamp(pointer, stampHolder[0]);
        }
        return node;
    }

    private AtomicStampedReference<Node<T>> ClearMark( AtomicStampedReference<Node<T>> node, int mark )
    {
        if ( node != null )
        {
            int[] stampHolder = { 0 };
            Node<T> pointer = node.get(stampHolder);
            stampHolder[0] = stampHolder[0] & ~mark;
            node.attemptStamp(pointer, stampHolder[0]);
        }
        return node;
    }

    // Check if the specified bit (or bits) of the stamp are marked.
    // Return false if the node is null.
    private boolean IsMarked( AtomicStampedReference<Node<T>> node, int mark )
    {
        if ( node != null )
        {
            int stamp = node.getStamp();
            stamp = 0x3 & stamp;
            stamp = stamp & mark;
            return ( stamp == mark );
        }
        return false;
    }

    private int[] KeyToCoord( int key )
    {
        int partialKey = key;
        int[] mappedKey = new int[base];

        for ( int dim = dimensions - 1; partialKey > 0; dim-- )
        {
            mappedKey[dim] = partialKey % base;
            partialKey = partialKey / base;
        }

        return mappedKey;
    }

    // Determine if either the AtomicStampedReference or the Node that it is wrapping is null.
    private boolean IsRefNull( AtomicStampedReference<Node<T>> nodeAsr )
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
    private T Find( int key )
    {
        // Create an arraylist in order to pass the pred and curr atomic references by reference.
        ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr = new ArrayList<>(2);
        // Create arrays for the ints to pass by reference.
        int[] dimOfPred = { 0 };
        int[] dimOfCurr = { 0 };

        // Start locating the pred and curr nodes from the head node.
        predAndCurrAsr.set(CURR_INDEX, head);

        LocatePred(KeyToCoord(key), predAndCurrAsr, dimOfPred, dimOfCurr);

        // The find is successful if and only if dimOfCurr int matches the total number of dimensions.
        if ( dimOfCurr[0] == dimensions )
        {
            return predAndCurrAsr.get(CURR_INDEX).getReference().value;
        }
        return null;
    }

    private void LocatePred( int[] mappedKey, ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr, int[] dimOfPred, int[] dimOfCurr )
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

    private void FinishInserting( AtomicStampedReference<Node<T>> nodeAsr, AdoptionDescriptor<T> adoptDesc )
    {

    }

    private void Insert( int key, T value )
    {
        AtomicStampedReference<Node<T>> nodeAsr;
        AtomicStampedReference<Node<T>> predAsr, currAsr;
        ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr = new ArrayList<>(2);
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };
        AdoptionDescriptor<T> adesc;
        nodeAsr = new AtomicStampedReference<>(new Node(key, value), 0);

        while ( true )
        {
            predAsr = null;
            currAsr = head;
            predAndCurrAsr.set(PRED_INDEX, predAsr);
            predAndCurrAsr.set(CURR_INDEX, currAsr);

            LocatePred(nodeAsr.getReference().mappedKey, predAndCurrAsr, dimOfPred, dimOfCurr);

            if ( !IsRefNull(currAsr) )
            {
                adesc = currAsr.getReference().adoptDesc;
            }
            else
            {
                adesc = null;
            }

            if ( adesc != null && dimOfPred[0] != dimOfCurr[0] )
            {
                FinishInserting(currAsr, adesc);
            }

            if ( IsMarked(predAsr.getReference().children.get(dimOfPred[0]), Fdel) )
            {
                currAsr = SetMark(currAsr, Fdel);
                if ( dimOfCurr[0] == dimensions - 1 )
                {
                    dimOfCurr[0] = dimensions;
                }
            }
            
            // FillNewNode start
            adesc = null;
            if ( dimOfPred[0] != dimOfCurr[0] )
            {
                adesc = new AdoptionDescriptor<T>(currAsr, dimOfPred[0], dimOfCurr[0]);
            }
            for ( int dim = 0; dim < dimOfPred[0]; dim++ )
            {
                nodeAsr.getReference().children.set(dim, new AtomicStampedReference<>(null, Fadp));
            }
            for ( int dim = dimOfPred[0]; dim < dimensions; dim++ )
            {
                nodeAsr.getReference().children.set(dim, null);
            }
            if ( dimOfCurr[0] < dimensions )
            {
                nodeAsr.getReference().children.set(dimOfCurr[0], currAsr);
            }
            nodeAsr.getReference().adoptDesc = adesc;
            // FillNewNode done
            
            int currStamp = currAsr.getStamp();
            int nodeStamp = nodeAsr.getStamp();
            nodeStamp += 4;
            if ( predAsr.getReference().children.get(dimOfPred[0]).compareAndSet(currAsr.getReference(), nodeAsr.getReference(), currStamp, nodeStamp) )
            {
                if ( adesc != null )
                {
                    FinishInserting(nodeAsr, adesc);
                }
                break;
            }
        }
    }

}

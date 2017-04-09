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

    public static final int Fadp = 0x1; // Flag 0001
    public static final int Fdel = 0x2; // Flag 0010
    public static final int Fall = 0x3; // Flag 0011
    public static final int StampInc = 0x4; // Increment Stamp by 0100 so it doesn't mess with flags

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

    private void Insert ( int key, T value )
    {
        AtomicStampedReference<Node<T>> nodeAsr;
        AtomicStampedReference<Node<T>> predAsr, currAsr;
        ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr = new ArrayList<>(2);
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };
        AdoptionDescriptor<T> adesc;
        nodeAsr = new AtomicStampedReference<>(new Node(key, value), 0);

        while ( true )
        {
            // Start at the head with the two references
            predAsr = null;
            currAsr = head;
            predAndCurrAsr.set(PRED_INDEX, predAsr);
            predAndCurrAsr.set(CURR_INDEX, currAsr);
            // Find the predecessor using LocatePred and the mapped key of the new node
            LocatePred(nodeAsr.getReference().mappedKey, predAndCurrAsr, dimOfPred, dimOfCurr);
            // If the we aren't at a leaf node, get the adoption descriptor
            if ( !IsRefNull(currAsr) )
            {
                adesc = currAsr.getReference().adoptDesc;
            }
            else
            {
                adesc = null;
            }
            // If the adoption descriptor exists and the predecessor and current aren't in the same dimension
            if ( adesc != null && dimOfPred[0] != dimOfCurr[0] )
            {
                FinishInserting(currAsr, adesc);
            }
            // If the predecessor node's child in its dimension is marked, do something weird
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
            // If the dimension of the predecessor and current nodes isn't the same, we need an adoption descriptor
            if ( dimOfPred[0] != dimOfCurr[0] )
            {
                adesc = new AdoptionDescriptor<T>(currAsr, dimOfPred[0], dimOfCurr[0]);
            }
            // All the children of dimension less than the predecessor can exist so we make them
            for ( int dim = 0; dim < dimOfPred[0]; dim++ )
            {
                nodeAsr.getReference().children.set(dim, new AtomicStampedReference<>(null, Fadp));
            }
            // All the children with dimension of or greater than the pred get marked null
            for ( int dim = dimOfPred[0]; dim < dimensions; dim++ )
            {
                nodeAsr.getReference().children.set(dim, new AtomicStampedReference<>(null, 0));
            }
            // If the pred node's child in the dimension of the pred wasn't marked,
            // we set the new node's child in the curr dim to curr. 
            if ( dimOfCurr[0] < dimensions )
            {
                nodeAsr.getReference().children.set(dimOfCurr[0], currAsr);
            }
            nodeAsr.getReference().adoptDesc = adesc;
            // FillNewNode done
            
            // So CAS needs the stamps to be check too
            int currStamp = currAsr.getStamp();
            int nodeStamp = nodeAsr.getStamp();
            // Increment the stamp
            nodeStamp += StampInc;
            // If CAS succeeds we try finish inserting and end, else we start over with the while loop
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
    
    private T Delete ( int key )
    {
        AtomicStampedReference<Node<T>> currAsr, predAsr, childAsr, markedAsr;
        ArrayList<AtomicStampedReference<Node<T>>> predAndCurrAsr = new ArrayList<>(2);
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };
        
        while ( true )
        {
         // Start at the head with the two references
            predAsr = null;
            currAsr = head;
            predAndCurrAsr.set(PRED_INDEX, predAsr);
            predAndCurrAsr.set(CURR_INDEX, currAsr);
            // Find the predecessor using LocatePred and the mapped key
            LocatePred(KeyToCoord(key, base, dimensions), predAndCurrAsr, dimOfPred, dimOfCurr);
            
            // If we failed to locate the key to delete
            if ( dimOfCurr[0] != dimensions )
            {
                return null;
            }
            
            markedAsr = SetMark(currAsr, Fdel);
            // So CAS needs the stamps to be check too
            int currStamp = currAsr.getStamp();
            int markedStamp = markedAsr.getStamp();
            // Increment the stamp
            markedStamp += StampInc;
            childAsr = predAsr.getReference().children.get(dimOfPred[0]);
            predAsr.getReference().children.get(dimOfPred[0]).compareAndSet(currAsr.getReference(), markedAsr.getReference(), currStamp, markedStamp);
            
            if ( ClearMark(childAsr, Fall) == currAsr )
            {
                if ( !IsMarked(childAsr, Fall) )
                {
                    return currAsr.getReference().value;
                }
                else if ( IsMarked(childAsr, Fdel) )
                {
                    return null;
                }
            }
        }
    }
    
    public void PrintList(AtomicStampedReference<Node<Integer>> node)
    {
        for ( int dim = 0; dim < dimensions; dim++)
        {
            if ( node.getReference() != null )
            {
                System.out.print(node.getReference().key + " -- (");
                for ( int num : node.getReference().mappedKey )
                {
                    System.out.print(node.getReference().mappedKey[num] + ", ");
                }
                System.out.println(") -- " + node.getReference().value);
                
                PrintList(node.getReference().children.get(dim));
            }
        }
    }

}

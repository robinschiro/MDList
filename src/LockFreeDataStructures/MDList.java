package LockFreeDataStructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
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
        private AtomicStampedReference[] children;
        private AtomicReference<AdoptionDescriptor> adoptDesc = new AtomicReference<>(null);

        public Node( int key, V value )
        {
            this.key = key;
            this.value = value;
            this.children = new AtomicStampedReference[dimensions];

            // Initialize all elements of the array with empty atomic references.
            for ( int i = 0; i < this.children.length; i++ )
            {
                this.children[i] = new AtomicStampedReference(null, 0);
            }

            // Generate mapped key
            mappedKey = KeyToCoord(key, MDList.this.base, MDList.this.dimensions);
        }
    }

    class AdoptionDescriptor
    {
        Node current;
        int dimOfPred;
        int dimOfCurr;

        public AdoptionDescriptor( Node current, int dimOfPred, int dimOfCurr )
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
    public int getDimensions ()
    {
        return dimensions;
    }

    private int keySpace;
    public int getKeySpace ()
    {
        return keySpace;
    }

    // Needed to convert key to set of coordinates in the MDList.
    // Calculate upon construction.
    private int base;
    public int getBase ()
    {
        return base;
    }

    // The head of the list.
    private AtomicStampedReference<Node<T>> head;

    // Constructor
    public MDList( int dimensions, int keySpace )
    {
        this.dimensions = dimensions;
        this.keySpace = keySpace;
        this.base = (int) Math.pow(keySpace, 1.0 / dimensions);
        this.head = new AtomicStampedReference<>(new Node(0, null), 0);
    }


    /** Utilities **/

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


    /** Helper methods **/

    private void LocatePred ( int[] mappedKey, AtomicStampedReference[] predAndCurrAsr, int[] dimOfPred, int[] dimOfCurr )
    {
        AtomicStampedReference<Node<T>> predAsr = predAndCurrAsr[PRED_INDEX];
        AtomicStampedReference<Node<T>> currAsr = predAndCurrAsr[CURR_INDEX];

        while ( dimOfCurr[0] < dimensions )
        {
            while ( !ReferenceUtilities.IsRefNull(currAsr) && mappedKey[dimOfCurr[0]] > currAsr.getReference().mappedKey[dimOfCurr[0]] )
            {
                predAsr = currAsr;
                dimOfPred[0] = dimOfCurr[0];
                AdoptionDescriptor adesc = currAsr.getReference().adoptDesc.get();

                if ( adesc != null && dimOfPred[0] >= adesc.dimOfPred && dimOfPred[0] <= adesc.dimOfCurr )
                {
                    FinishInserting(currAsr, adesc);
                }

                currAsr = ReferenceUtilities.ClearMark(currAsr.getReference().children[dimOfCurr[0]], Fall);
            }
            if ( ReferenceUtilities.IsRefNull(currAsr) || mappedKey[dimOfCurr[0]] < currAsr.getReference().mappedKey[dimOfCurr[0]] )
            {
                break;
            }
            else
            {
                dimOfCurr[0]++;
            }
        }

        predAndCurrAsr[PRED_INDEX] = predAsr;
        predAndCurrAsr[CURR_INDEX] = currAsr;
    }

    private void FinishInserting ( AtomicStampedReference<Node<T>> adopterAsr, AdoptionDescriptor adoptDesc )
    {
        // Read in values from adoption context.
        Node donorAsr = adoptDesc.current;
        int dimOfPred = adoptDesc.dimOfPred;
        int dimOfCurr = adoptDesc.dimOfCurr;

        // Iterate through the children of the node whose children are being adopted (the "adoptee").
        for ( int i = dimOfPred; i < dimOfCurr; i++ )
        {
            // Retrieve the child of the adoptee.
            AtomicStampedReference<Node<T>> childAsr = donorAsr.children[i];

            // Set the adoption flag in order to prevent insert operations from modifying this node while it's being adopted.
            ReferenceUtilities.SetMark(childAsr, Fadp);

            // Get the atomic reference wrapping the child of the adopter. Only fill it if it is empty.
            AtomicStampedReference<Node<T>> adopterChildAsr = adopterAsr.getReference().children[i];
            if ( null == adopterChildAsr.getReference() )
            {
                int expectedStamp = adopterChildAsr.getStamp();
                adopterChildAsr.compareAndSet(null, childAsr.getReference(), expectedStamp, (expectedStamp + StampInc) | Fadp );
            }
        }

        // Nullify the adoption descriptor for the adopter now that it has finished adopting its children.
        adopterAsr.getReference().adoptDesc.compareAndSet(adoptDesc, null);
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

                PrintList(node.getReference().children[dim]);
            }
        }
    }


    /** Abstract Dictionary Implementation **/

    // Given a key, find the associated value if a node with the key exists in the MDList.
    public T Find ( int key )
    {
        // Create an arraylist in order to pass the pred and curr atomic references by reference.
        AtomicStampedReference[] predAndCurrAsr = new AtomicStampedReference[2];
        // Create arrays for the ints to pass by reference.
        int[] dimOfPred = { 0 };
        int[] dimOfCurr = { 0 };

        // Start locating the pred and curr nodes from the head node.
        predAndCurrAsr[CURR_INDEX] = head;

        LocatePred(KeyToCoord(key, base, dimensions), predAndCurrAsr, dimOfPred, dimOfCurr);

        // The find is successful if and only if dimOfCurr int matches the total number of dimensions.
        if ( dimOfCurr[0] == dimensions )
        {
            return ((Node<T>)predAndCurrAsr[CURR_INDEX].getReference()).value;
        }
        return null;
    }


    public void Insert ( int key, T value )
    {
        AtomicStampedReference<Node<T>> nodeAsr;
        AtomicStampedReference<Node<T>> predAsr, currAsr;
        AtomicStampedReference[] predAndCurrAsr = new AtomicStampedReference[2];
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };
        AdoptionDescriptor adesc;
        nodeAsr = new AtomicStampedReference<>(new Node(key, value), 0);

        while ( true )
        {
            // Start at the head with the two references
            predAndCurrAsr[PRED_INDEX] = null;
            predAndCurrAsr[CURR_INDEX] = head;

            // Find the predecessor using LocatePred and the mapped key of the new node
            LocatePred(nodeAsr.getReference().mappedKey, predAndCurrAsr, dimOfPred, dimOfCurr);

            predAsr = predAndCurrAsr[PRED_INDEX];
            currAsr = predAndCurrAsr[CURR_INDEX];

            // If the we aren't at a leaf node, get the adoption descriptor
            if ( !ReferenceUtilities.IsRefNull(currAsr) )
            {
                adesc = currAsr.getReference().adoptDesc.get();
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
            if ( ReferenceUtilities.IsMarked(predAsr.getReference().children[dimOfPred[0]], Fdel) )
            {
                currAsr = ReferenceUtilities.SetMark(currAsr, Fdel);
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
                adesc = new AdoptionDescriptor(currAsr.getReference(), dimOfPred[0], dimOfCurr[0]);
            }

            // Set the Fadp flag of a subset of the children slots to invalidate these positions for future insertions.
            // All remaining positions are valid.
            for ( int dim = 0; dim < dimOfPred[0]; dim++ )
            {
                ReferenceUtilities.SetMark(nodeAsr.getReference().children[dim], Fadp);
            }

            // If the pred node's child in the dimension of the pred wasn't marked,
            // we set the new node's child in the curr dim to curr.
            if ( dimOfCurr[0] < dimensions )
            {
                nodeAsr.getReference().children[dimOfCurr[0]] = currAsr;
            }
            nodeAsr.getReference().adoptDesc.set(adesc);
            // FillNewNode done

            // So CAS needs the stamps to be check too
            int currStamp = currAsr.getStamp();
            int nodeStamp = nodeAsr.getStamp();

            // Increment the stamp
            nodeStamp += StampInc;

            // If CAS succeeds we try finish inserting and end, else we start over with the while loop
            if ( predAsr.getReference().children[dimOfPred[0]].compareAndSet(currAsr.getReference(), nodeAsr.getReference(), currStamp, nodeStamp) )
            {
                if ( adesc != null )
                {
                    FinishInserting(nodeAsr, adesc);
                }
                break;
            }
        }
    }

    public T Delete ( int key )
    {
        AtomicStampedReference<Node<T>> currAsr, predAsr, childAsr, markedAsr;
        AtomicStampedReference[] predAndCurrAsr = new AtomicStampedReference[2];
        Node[] predAndCurr = new Node[2];
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };

        while ( true )
        {
            // Start at the head with the two references
            predAndCurrAsr[PRED_INDEX] = null;
            predAndCurrAsr[CURR_INDEX] = head;
            // Find the predecessor using LocatePred and the mapped key
            LocatePred(KeyToCoord(key, base, dimensions), predAndCurrAsr, dimOfPred, dimOfCurr);
            predAsr = predAndCurrAsr[PRED_INDEX];
            currAsr = predAndCurrAsr[CURR_INDEX];
            // If we failed to locate the key to delete
            if ( dimOfCurr[0] != dimensions )
            {
                return null;
            }

            markedAsr = ReferenceUtilities.SetMark(currAsr, Fdel);
            // So CAS needs the stamps to be check too
            int currStamp = currAsr.getStamp();
            int markedStamp = markedAsr.getStamp();
            // Increment the stamp
            markedStamp += StampInc;
            childAsr = predAsr.getReference().children[dimOfPred[0]];
            predAsr.getReference().children[dimOfPred[0]].compareAndSet(currAsr.getReference(), markedAsr.getReference(), currStamp, markedStamp);

            if ( ReferenceUtilities.ClearMark(childAsr, Fall) == currAsr )
            {
                if ( !ReferenceUtilities.IsMarked(childAsr, Fall) )
                {
                    return currAsr.getReference().value;
                }
                else if ( ReferenceUtilities.IsMarked(childAsr, Fdel) )
                {
                    return null;
                }
            }
        }
    }
}

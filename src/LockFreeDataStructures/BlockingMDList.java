package LockFreeDataStructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingMDList<T> implements ILockFreeDictionary<T>
{
    private static final int PRED_INDEX = 0;
    private static final int CURR_INDEX = 1;

    class Node<V>
    {
        private int key;
        private int[] mappedKey;
        private V value;
        private Node[] children;
        private AdoptionDescriptor adoptDesc = null;

        public Node( int key, V value )
        {
            this.key = key;
            this.value = value;
            this.children = new Node[dimensions];

            // Initialize all elements of the array with empty atomic references.
            for ( int i = 0; i < this.children.length; i++ )
            {
                this.children[i] = null;
            }

            // Generate mapped key
            mappedKey = KeyToCoord(key, BlockingMDList.this.base, BlockingMDList.this.dimensions);
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

    // Needed to convert key to set of coordinates in the BlockingMDList.
    // Calculate upon construction.
    private int base;
    public int getBase ()
    {
        return base;
    }

    // The head of the list.
    private Node<T> head;
    private final ReentrantLock lock;
    
    // Constructor
    public BlockingMDList( int dimensions, int keySpace )
    {
        this.dimensions = dimensions;
        this.keySpace = keySpace;
        this.base = (int) Math.ceil(Math.pow(keySpace, 1.0 / dimensions));
        this.head = new Node(0, null);
        this.lock = new ReentrantLock();
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

    // Compares the reference and ONLY the flags, not the whole stamp
    public boolean AreEqual ( AtomicStampedReference<Node<T>> first, AtomicStampedReference<Node<T>> second )
    {
        return (first.getReference() == second.getReference() && ((first.getStamp() & 0x3)  == (second.getStamp() & 0x3)));
    }

    public static boolean IsKeyValid ( int key, int keySpace )
    {
        return (key > 0 && key < keySpace);
    }

    public void CheckKeyValid ( int key )
    {
        if ( !IsKeyValid(key, keySpace) )
        {
            throw new IllegalArgumentException("Key out of bounds.");
        }
    }

    public static int CalculateDimensionsFromBase( int desiredBase, int keySpace )
    {
        return (int)Math.round(Math.log(keySpace) / Math.log(2));
    }

    public void PrintList()
    {
        System.out.println("Dimensions: " + dimensions);
        System.out.println("Base: " + base);
        System.out.println("KeySpace: " + keySpace);
        PrintListHelper(head, 0);
    }

    private void PrintListHelper(Node<T> node, int startDim)
    {
        if ( node != null )
        {
            System.out.print(node.key + " -- (");
            for ( int num = 0; num < dimensions - 1; num++ )
            {
                System.out.print(node.mappedKey[num] + ", ");
            }
            System.out.println(node.mappedKey[dimensions - 1] + ") -- " + node.value);

            for ( int dim = dimensions - 1; dim >= startDim; dim--)
            {
                PrintListHelper(node.children[dim], 0);
            }
        }
    }


    /** Helper methods **/

    private void LocatePred ( int[] mappedKey, Node[] predAndCurr, int[] dimOfPred, int[] dimOfCurr )
    {
        Node<T> pred = predAndCurr[PRED_INDEX];
        Node<T> curr = predAndCurr[CURR_INDEX];

        while ( dimOfCurr[0] < dimensions )
        {
            while ( curr != null && mappedKey[dimOfCurr[0]] > curr.mappedKey[dimOfCurr[0]] )
            {
                pred = curr;
                dimOfPred[0] = dimOfCurr[0];
                AdoptionDescriptor adesc = curr.adoptDesc;

                if ( adesc != null && dimOfPred[0] >= adesc.dimOfPred && dimOfPred[0] <= adesc.dimOfCurr )
                {
                    FinishInserting(curr, adesc);
                }

                curr = curr.children[dimOfCurr[0]];
            }
            if ( curr == null || mappedKey[dimOfCurr[0]] < curr.mappedKey[dimOfCurr[0]] )
            {
                break;
            }
            else
            {
                dimOfCurr[0]++;
            }
        }

        predAndCurr[PRED_INDEX] = pred;
        predAndCurr[CURR_INDEX] = curr;
    }

    // Child adoption
    private void FinishInserting ( Node adopter, AdoptionDescriptor adoptDesc )
    {
        // Read in values from adoption context.
        Node donor = adoptDesc.current;
        int dimOfPred = adoptDesc.dimOfPred;
        int dimOfCurr = adoptDesc.dimOfCurr;

        // Iterate through the children of the node whose children are being adopted (the "adoptee").
        for ( int i = dimOfPred; i < dimOfCurr; i++ )
        {
            // Retrieve the child of the donor.
            //Node<T> donorChild = donor.children[i];

            // Clear any remnant Fadp flag in the atomic reference
            // so that this flag is not set when this child is given to the adopter.
            //AtomicStampedReference donorChildClone = ReferenceUtilities.CloneAsr(donorChildAsr);
            //donorChild = ReferenceUtilities.ClearMark(donorChildAsrClone, Fadp);

            // Get the atomic reference wrapping the child of the adopter.
            //Node<T> adopterChild = adopter.children[i];

            // Only fill the atomic reference if it is empty.
            if ( null == adopter.children[i] )
            {
                //int newStamp = donorChildAsrClone.getStamp() + StampInc;
                adopter.children[i] = donor.children[i];
                //adopterChildAsr.compareAndSet(null, donorChildAsrClone.getReference(), 0, newStamp);
            }
        }

        // Nullify the adoption descriptor for the adopter now that it has finished adopting its children.
        adopter.adoptDesc = null;
    }


    /** Abstract Dictionary Implementation **/

    // Given a key, find the associated value if a node with the key exists in the BlockingMDList.
    @Override
    public T Find ( int key )
    {
        CheckKeyValid(key);
        // Create an arraylist in order to pass the pred and curr atomic references by reference.
        Node<T>[] predAndCurr = new Node[2];
        // Create arrays for the ints to pass by reference.
        int[] dimOfPred = { 0 };
        int[] dimOfCurr = { 0 };

        // Start locating the pred and curr nodes from the head node.
        predAndCurr[CURR_INDEX] = head;

        LocatePred(KeyToCoord(key, base, dimensions), predAndCurr, dimOfPred, dimOfCurr);

        // The find is successful if and only if dimOfCurr int matches the total number of dimensions.
        // We don't know how Find is supposed to skip logically deleted nodes so we are checking here manually.
        if ( dimOfCurr[0] == dimensions )
        {
            return predAndCurr[CURR_INDEX].value;
        }
        return null;
    }


    @Override
    public void Insert ( int key, T value )
    {
        lock.lock();
        CheckKeyValid(key);
        Node<T> node;
        Node<T> pred, curr;
        Node[] predAndCurr = new Node[2];
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };
        AdoptionDescriptor adesc;
        node = new Node(key, value);

        while ( true )
        {
            // Reset dimensions of pred and curr.
            dimOfPred[0] = 0;
            dimOfCurr[0] = 0;

            // Start at the head with the two references
            predAndCurr[PRED_INDEX] = null;
            predAndCurr[CURR_INDEX] = head;

            // Find the predecessor using LocatePred and the mapped key of the new node
            LocatePred(node.mappedKey, predAndCurr, dimOfPred, dimOfCurr);

            pred = predAndCurr[PRED_INDEX];
            curr = predAndCurr[CURR_INDEX];

            // If the we aren't at a leaf node, get the adoption descriptor
            if ( curr != null )
            {
                adesc = curr.adoptDesc;
            }
            else
            {
                adesc = null;
            }

            // If the adoption descriptor exists and the predecessor and current aren't in the same dimension
            if ( adesc != null && dimOfPred[0] != dimOfCurr[0] )
            {
                FinishInserting(curr, adesc);
            }

            // If the predecessor node's child in its dimension is marked, do something weird
//            if ( ReferenceUtilities.IsMarked(predAsr.getReference().children[dimOfPred[0]], Fdel) )
//            {
//                currAsr = ReferenceUtilities.SetMark(currAsr, Fdel);
//                if ( dimOfCurr[0] == dimensions - 1 )
//                {
//                    dimOfCurr[0] = dimensions;
//                }
//            }

            // FillNewNode start
            adesc = null;

            // If the dimension of the predecessor and current nodes isn't the same, we need an adoption descriptor
            if ( dimOfPred[0] != dimOfCurr[0] )
            {
                adesc = new AdoptionDescriptor(curr, dimOfPred[0], dimOfCurr[0]);
            }

            // Set the Fadp flag of a subset of the children slots to invalidate these positions for future insertions.
            // All remaining positions are valid.
//            for ( int dim = 0; dim < dimOfPred[0]; dim++ )
//            {
//                node.children[dim].set(null, Fadp);
//            }

            // If the pred node's child in the dimension of the pred wasn't marked,
            // we set the new node's child in the curr dim to curr.
            if ( dimOfCurr[0] < dimensions )
            {
                node.children[dimOfCurr[0]] = curr;
            }
            node.adoptDesc = adesc;
            // FillNewNode done

//            // So CAS needs the stamps to be check too
//            int currStamp = currAsr.getStamp();
//            int nodeStamp = nodeAsr.getStamp();

            // Increment the stamp
            //nodeStamp += StampInc;

            // If CAS succeeds we try finish inserting and end, else we start over with the while loop
            pred.children[dimOfPred[0]] = node;
            if ( adesc != null )
            {
                FinishInserting(node, adesc);
            }
            break;
//            if ( predAsr.getReference().children[dimOfPred[0]].compareAndSet(currAsr.getReference(), nodeAsr.getReference(), currStamp, nodeStamp) )
//            {
//                if ( adesc != null )
//                {
//                    FinishInserting(node, adesc);
//                }
//                break;
//            }
        }
        lock.unlock();
    }

    @Override
    public T Delete ( int key )
    {
        lock.lock();
        CheckKeyValid(key);
        Node<T> curr, pred, child, marked;
        Node[] predAndCurr = new Node[2];
        int[] dimOfPred = { 0 }, dimOfCurr = { 0 };

        while ( true )
        {
            // Reset dimensions of pred and curr.
            dimOfPred[0] = 0;
            dimOfCurr[0] = 0;

            // Start at the head with the two references
            predAndCurr[PRED_INDEX] = null;
            predAndCurr[CURR_INDEX] = head;
            // Find the predecessor using LocatePred and the mapped key
            LocatePred(KeyToCoord(key, base, dimensions), predAndCurr, dimOfPred, dimOfCurr);
            pred = predAndCurr[PRED_INDEX];
            curr = predAndCurr[CURR_INDEX];
            // If we failed to locate the key to delete
            if ( dimOfCurr[0] != dimensions )
            {
                return null;
            }
            AdoptionDescriptor adesc = new AdoptionDescriptor(curr, dimOfPred[0], dimOfCurr[0]);
            for(int dim = dimensions-1; dim >= 0; dim-- )
            {
                if(curr.children[dim] != null)
                {
                    curr.children[dim].adoptDesc = adesc;
                    FinishInserting(curr.children[dim], adesc);
                    pred.children[dimOfPred[0]] = curr.children[dim];
                    lock.unlock();
                    return curr.value;
                }
            }
            //marked = ReferenceUtilities.SetMark(currAsr, Fdel);
            //assert(ReferenceUtilities.IsMarked(markedAsr, Fdel));
            // So CAS needs the stamps to be check too
            //int currStamp = currAsr.getStamp();
            //int markedStamp = markedAsr.getStamp();
            // Increment the stamp
            //markedStamp += StampInc;
            //childAsr = ReferenceUtilities.CloneAsr(predAsr.getReference().children[dimOfPred[0]]);
            //int childStamp = childAsr.getStamp();
//            predAsr.getReference().children[dimOfPred[0]].compareAndSet(childAsr.getReference(), markedAsr.getReference(),
//                                                                                          childStamp, markedStamp);
//            if ( AreEqual(ReferenceUtilities.ClearMark(childAsr, Fall), currAsr) )
//            {
//                if ( !ReferenceUtilities.IsMarked(childAsr, Fall) )
//                {
//                    return currAsr.getReference().value;
//                }
//                else if ( ReferenceUtilities.IsMarked(childAsr, Fdel) )
//                {
//                    return null;
//                }
//            }
        }
    }
}

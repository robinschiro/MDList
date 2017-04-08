package LockFreeDataStructures;

// This is a test

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicStampedReference;

public class MDList<T>
{

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

    private static final int Fadp = 0x1;
	private static final int Fdel = 0x2;
	private static final int Fall = 0x3;

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
    	this.base = (int) Math.pow(keySpace, 1/dimensions);
    	this.head = new AtomicStampedReference<>(new Node(0, null), 0);
    }

    private boolean SetMark(AtomicStampedReference<Node<T>> node, int mark )
    {
    	int[] stampHolder = {0};
    	Node<T> pointer = node.get(stampHolder);
    	stampHolder[0] = stampHolder[0] | mark;
    	return node.attemptStamp(pointer, stampHolder[0]);
    }

    private boolean ClearMark(AtomicStampedReference<Node<T>> node, int mark)
    {
    	int[] stampHolder = {0};
    	Node<T> pointer = node.get(stampHolder);
    	stampHolder[0] = stampHolder[0] & ~mark;
    	return node.attemptStamp(pointer, stampHolder[0]);
    }

    private boolean IsMarked(AtomicStampedReference<Node<T>> node, int mark)
    {
    	int stamp = node.getStamp();
    	stamp = 0x3 & stamp;
    	stamp = stamp & mark;
    	return (stamp == mark);
    }

    private int[] KeyToCoord(int key)
    {
    	int partialKey = key;
        int[] mappedKey = new int[base];

        for( int dim = dimensions - 1; partialKey > 0; dim-- )
        {
        	mappedKey[dim] = partialKey % base;
        	partialKey = partialKey/base;
        }

        return mappedKey;
    }

    private T Find( int key )
    {
        AtomicStampedReference<Node<T>> curr, pred;
    	int[] dimOfPred = {0};
    	int[] dimOfCurr = {0};

    	pred = null;
    	curr = head;

    	LocatePred( KeyToCoord(key), curr, pred, dimOfPred, dimOfCurr );

    	if( dimOfCurr[0] == dimensions )
    	{
    		return curr.getReference().value;
    	}
    	return null;
    }

    private void LocatePred( int[] mappedKey, AtomicStampedReference<Node<T>> curr, AtomicStampedReference<Node<T>> pred, int[] dimOfPred, int[] dimOfCurr )
    {
    	while( dimOfCurr[0] < dimensions )
    	{
    		while( curr != null && mappedKey[dimOfCurr[0]] > curr.getReference().mappedKey[dimOfCurr[0]] )
    		{
    			pred = curr;
    			dimOfPred[0] = dimOfCurr[0];
    			AdoptionDescriptor<T> adesc = curr.getReference().adoptDesc;

    			if( adesc != null && dimOfPred[0] >= adesc.dimOfPred && dimOfPred[0] <= adesc.dimOfCurr )
    			{
    				//FinishInserting(curr, adesc);
    			}
    			// paper has this as curr = ClearMark(curr.child[dc], Fall)
    			// does this mean that clear mark should return the node?
    			ClearMark( curr.getReference().children.get(dimOfCurr[0]), Fall );
    		}
    		if( curr == null || mappedKey[dimOfCurr[0]] < curr.getReference().mappedKey[dimOfCurr[0]] )
    		{
    			break;
    		}
    		else
    		{
    			dimOfCurr[0] = dimOfCurr[0] + 1;
    		}
    	}
    }

}

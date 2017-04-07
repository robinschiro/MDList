package LockFreeDataStructures;

// This is a test

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicStampedReference;

public class MDList<T>
{
	
    class Node<T>
    {
        private int key;
        private int[] mappedKey;
        private T value;
        private ArrayList<AtomicStampedReference<Node<T>>> children;
        private AdoptionDescriptor adoptDesc;

        public Node( int key, T value )
        {
            this.key = key;
            this.value = value;
            this.children = new ArrayList<>(dimensions);

            // Generate mapped key
            mappedKey = KeyToCoord(key);
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

    private static final int Fadp = 0x1;
	private static final int Fdel = 0x2;
	private static final int Fall = 0x3;
	
    private int dimensions;
    private int keySpace;

    // Set upon construction
    private int base;
    
    public Node<T> head;

    public MDList( int dimensions, int keySpace )
    {
    	this.dimensions = dimensions;
    	this.keySpace = keySpace;
    	this.base = (int) Math.pow(keySpace, 1/dimensions);
    	head = new Node(0, 0);
    }
    
    
    
    private boolean SetMark( AtomicStampedReference<Node<T>> node, int mark )
    {
    	int[] stampHolder = {0};
    	Node<T> pointer = node.get(stampHolder);
    	stampHolder[0] = stampHolder[0] | mark;
    	return node.attemptStamp(pointer, stampHolder[0]);
    }
    
    private boolean ClearMark( AtomicStampedReference<Node<T>> node, int mark)
    {
    	int[] stampHolder = {0};
    	Node<T> pointer = node.get(stampHolder);
    	stampHolder[0] = stampHolder[0] & ~mark;
    	return node.attemptStamp(pointer, stampHolder[0]);
    }
    
    private boolean IsMarked( AtomicStampedReference<Node<T>> node, int mark)
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
    
    T Find(int key)
    {
    	Node<T> curr, pred;
    	int dimOfPred, dimOfCurr;
    	
    	pred = null;
    	curr = head;
    	dimOfPred = 0;
    	dimOfCurr = 0;
    	
    	// inline function?
    	LocatePred(KeyToCoord(key), curr, pred, dimOfPred, dimOfCurr);
    	
    	if(dimOfCurr == dimensions)
    	{
    		return curr.value;
    	}
    	return null;
    }
    
}

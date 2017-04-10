package LockFreeDataStructures;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class SkipList<T>
{
    static int MAX_LEVEL;
    final Node<T> head;
    final Node<T> tail;

    public SkipList(int maxLevel)
    {
        MAX_LEVEL = maxLevel;
        head = new Node<>(Integer.MIN_VALUE);
        tail = new Node<>(Integer.MAX_VALUE);
        for ( int i = 0; i < head.next.length; i++ )
        {
            head.next[i] = new AtomicMarkableReference<>(tail, false);
        }
    }

    public static final class Node<T>
    {
        final T value;
        final int key;
        final AtomicMarkableReference<SkipList.Node<T>>[] next;
        private int topLevel;

        // Sentinel Node Constructor
        public Node(int key)
        {
            this.value = null;
            this.key = key;
            next = new AtomicMarkableReference[MAX_LEVEL + 1];
            for ( int i = 0; i < next.length; i++ )
            {
                next[i] = new AtomicMarkableReference<>(null, false);
            }
            topLevel = MAX_LEVEL;
        }
        // Constructor for ordinary nodes
        public Node(int key, T value, int height)
        {
            this.value = value;
            this.key = key;
            next = new AtomicMarkableReference[height + 1];
            for ( int i = 0; i < next.length; i++ )
            {
                next[i] = new AtomicMarkableReference<>(null, false);
            }
            this.topLevel = height;
        }
    }

    static int RandomLevel()
    {
        int height = 0;
        while ( height < MAX_LEVEL && ThreadLocalRandom.current().nextInt(0, 2) == 1 )
        {
            height++;
        }
        return height;
    }

    public boolean Add ( int key, T value) {
        int topLevel = RandomLevel();
        int bottomLevel = 0;
        Node<T>[] preds = new Node[MAX_LEVEL + 1];
        Node<T>[] succs = new Node[MAX_LEVEL + 1];

        while ( true )
        {
            boolean found = Find(key, preds, succs);
            if ( found )
            {
                return false;
            }
            Node<T> newNode = new Node(key, value, topLevel);
            for ( int level = bottomLevel; level <= topLevel; level++ )
            {
                Node<T> succ = succs[level];
                newNode.next[level].set(succ, false);
            }
            Node<T> pred = preds[bottomLevel];
            Node<T> succ = succs[bottomLevel];
            newNode.next[bottomLevel].set(succ,  false);
            if ( !pred.next[bottomLevel].compareAndSet(succ, newNode, false, false) )
            {
                continue;
            }
            for ( int level = bottomLevel + 1; level <= topLevel; level++ )
            {
                while ( true )
                {
                    pred = preds[level];
                    succ = succs[level];
                    if ( pred.next[level].compareAndSet(succ, newNode, false, false) )
                    {
                        break;
                    }
                    Find(key, preds, succs);
                }
            }
            return true;
        }
    }

    public boolean Remove ( int key )
    {
        int bottomLevel = 0;
        Node<T>[] preds = new Node[MAX_LEVEL + 1];
        Node<T>[] succs = new Node[MAX_LEVEL + 1];
        Node<T> succ;

        while ( true )
        {
            boolean found = Find(key, preds, succs);
            if ( !found )
            {
                return false;
            }
            Node<T> nodeToRemove = succs[bottomLevel];
            boolean[] marked = {false};
            for ( int level = nodeToRemove.topLevel; level >= bottomLevel + 1; level -- )
            {
                marked[0] = false;
                succ = nodeToRemove.next[level].get(marked);
                while ( !marked[0] )
                {
                    nodeToRemove.next[level].attemptMark(succ, true);
                    succ = nodeToRemove.next[level].get(marked);
                }
            }
            marked[0] = false;
            succ = nodeToRemove.next[bottomLevel].get(marked);
            while ( true )
            {
                boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                succ = succs[bottomLevel].next[bottomLevel].get(marked);
                if ( iMarkedIt )
                {
                    Find(key, preds, succs);
                    return true;
                }
                else if ( marked[0] )
                {
                    return false;
                }
            }
        }
    }

    private boolean Find ( int key, Node<T>[] preds, Node<T>[] succs )
    {
        int bottomLevel = 0;
        boolean[] marked = {false};
        boolean snip;
        Node<T> pred = null, curr = null, succ = null;
        retry:
            while ( true )
            {
                pred = head;
                for ( int level = MAX_LEVEL; level >= bottomLevel; level-- )
                {
                    curr = pred.next[level].getReference();
                    while ( true )
                    {
                        succ = curr.next[level].get(marked);
                        while ( marked[0] )
                        {
                            snip = pred.next[level].compareAndSet(curr, succ, false, false);
                            if ( !snip )
                            {
                                continue retry;
                            }
                            curr = pred.next[level].getReference();
                            succ = curr.next[level].get(marked);
                        }
                        if ( curr.key < key )
                        {
                            pred = curr;
                            curr = succ;
                        }
                        else
                        {
                            break;
                        }
                    }
                    preds[level] = pred;
                    succs[level] = curr;
                }
                return (curr.key == key);
            }
    }

    public boolean Contains ( int key )
    {
        int bottomLevel = 0;
        boolean[] marked = {false};
        Node<T> pred = head, curr = null, succ = null;
        for ( int level = MAX_LEVEL; level >= bottomLevel; level-- )
        {
            curr = pred.next[level].getReference();
            while ( true )
            {
                succ = curr.next[level].get(marked);
                while ( marked[0] )
                {
                    curr = pred.next[level].getReference();
                    succ = curr.next[level].get(marked);
                }
                if ( curr.key < key )
                {
                    pred = curr;
                    curr = succ;
                }
                else
                {
                    break;
                }
            }
        }
        return (curr.key == key);
    }

}

package LockFreeDataStructures;

import java.util.concurrent.atomic.AtomicStampedReference;

import LockFreeDataStructures.MDList.Node;

public final class ReferenceUtilities
{
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

    public static AtomicStampedReference CloneAsr ( AtomicStampedReference asrToClone )
    {
        return new AtomicStampedReference(asrToClone.getReference(), asrToClone.getStamp());
    }

    // Determine if the node that is inside the AtomicStampedReference is null.
    public static boolean IsRefNull ( AtomicStampedReference nodeAsr )
    {
        if ( null != nodeAsr )
        {
            return null == nodeAsr.getReference();
        }
        throw new RuntimeException("The AtomicStampedReference is null. This should not happen");
    }
}

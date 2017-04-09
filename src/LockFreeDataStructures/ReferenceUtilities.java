package LockFreeDataStructures;

import java.util.concurrent.atomic.AtomicStampedReference;

import LockFreeDataStructures.MDList.Node;

public final class ReferenceUtilities
{
    private static final String NULL_ERROR_MESSAGE = "The AtomicStampedReference is null.";

    public static AtomicStampedReference SetMark ( AtomicStampedReference asr, int mark )
    {
        if ( asr != null )
        {
            int[] stampHolder = { 0 };
            Object pointer = asr.get(stampHolder);
            stampHolder[0] = stampHolder[0] | mark;
            asr.attemptStamp(pointer, stampHolder[0]);
            return asr;
        }
        throw new NullPointerException(NULL_ERROR_MESSAGE);
    }

    public static AtomicStampedReference ClearMark ( AtomicStampedReference asr, int mark )
    {
        if ( asr != null )
        {
            int[] stampHolder = { 0 };
            Object pointer = asr.get(stampHolder);
            stampHolder[0] = stampHolder[0] & ~mark;
            asr.attemptStamp(pointer, stampHolder[0]);
            return asr;
        }
        throw new NullPointerException(NULL_ERROR_MESSAGE);
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
        throw new NullPointerException(NULL_ERROR_MESSAGE);
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
        throw new RuntimeException(NULL_ERROR_MESSAGE);
    }
}

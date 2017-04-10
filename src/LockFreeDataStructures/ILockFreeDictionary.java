package LockFreeDataStructures;

public interface ILockFreeDictionary<T>
{
    T Find ( int key );
    void Insert ( int key, T value );
    T Delete ( int key );
}

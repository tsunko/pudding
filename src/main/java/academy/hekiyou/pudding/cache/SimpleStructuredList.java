package academy.hekiyou.pudding.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A cache list that implements a linked-list-like structure but keeps a mapping of each entry to a node
 * for O(1) access, remove, _immediate_ inserts, and O(n) sorting
 *
 * Caveat: Only O(1) if we don't overflow capacity, which we shouldn't.
 * Other caveat: Immediate means fresh, new entries with no frequency at all.
 * Catastrophic caveat: Big-O notation is hard. O(n) sorting is absolute worst case; average case is probably O(n/2)
 *
 * Why?
 * - Entries need to be sorted; using Collections.sort() took too long as it properly sorts _all_ elements at once.
 *   Ultimately, we don't care about elements from 0 to (idx-1); we only care about idx to n.
 * - We also need fast inserts, deletes, and access, as we promote and demote entries almost every TieredCache.get()
 *   call.
 *
 * In actuality, this probably isn't the most suitable solution by any means - might as well be an XY problem.
 * To sum it up, this data structure is a bastardization of data structures - linked lists, dictionaries, and queues.
 *
 * @param <E> Type of elements to be stored
 */
public class SimpleStructuredList<E extends Comparable<E>> implements StructuredList<E> {
    
    private final Map<E, Node> nodeMap;
    private Node head, tail;
    
    public SimpleStructuredList(int capacity){
        this.nodeMap = new HashMap<>(capacity + 1);
    }
    
    @Override
    public void add(@NotNull E entry){
        // by default, new elements have the lowest frequency, so insert it as our head
        Node newNode = new Node(entry);
        if(head == null){
            tail = newNode;
        } else {
            head.prev = newNode;
            newNode.next = head;
        }
        
        head = newNode;
        // now map the entry to newNode
        nodeMap.put(entry, newNode);
    }
    
    @Override
    public void remove(@NotNull E entry){
        Node node = nodeMap.remove(entry);
        if(node == null)
            throw new NoSuchElementException();
        unlink(node);
    }
    
    @Override
    public void restructure(@NotNull E changed){
        Node changedNode = nodeMap.get(changed);
        if(changedNode == null)
            throw new NoSuchElementException("attempted to restructure " + changed);
        
        // if tail node, don't bother restructuring
        if(changedNode == tail)
            return;

        // unlink the current node
        unlink(changedNode);
        
        Node current = changedNode.next;
        if(current == null)
            throw new IllegalStateException("current == null but not tail?");
        
        while(current.next != null && changedNode.compareTo(current) < 1)
            current = current.next;
        
        // current is now either at the end or the next node is higher than the one we're re-sorting
        Node oldCurrentNext = current.next;
        current.next = changedNode;
        changedNode.prev = current;
        changedNode.next = oldCurrentNext;
        
        // we're at the tail of the list - set re-set tail
        if(current == tail){
            tail = changedNode;
        } else {
            // not a tail - change oldCurrentNext.prev
            oldCurrentNext.prev = changedNode;
        }
    }
    
    @Override
    @NotNull
    public E getHighest(){
        if(isEmpty())
            throw new NoSuchElementException();
        return tail.value;
    }
    
    @Override
    public boolean hasHighest(){
        return tail != null;
    }
    
    @Override
    public @NotNull E popHighest(){
        E entry = getHighest();
        remove(entry);
        return entry;
    }
    
    @Override
    @NotNull
    public E getLowest(){
        if(isEmpty())
            throw new NoSuchElementException();
        return head.value;
    }
    
    @Override
    public boolean hasLowest(){
        return head != null;
    }
    
    @Override
    public @NotNull E popLowest(){
        E entry = getLowest();
        remove(entry);
        return entry;
    }
    
    @Override
    @NotNull
    public Iterator<E> iterator(){
        return new Iter();
    }
    
    @Override
    public int size(){
        return nodeMap.size();
    }
    
    @Override
    public boolean isEmpty(){
        return size() == 0;
    }
    
    @Override
    public String toString(){
        return "SimpleCacheList{size=" + size() +
               ", min=" + head +
               ", max=" + tail + "}";
    }
    
    private void unlink(@NotNull Node node){
        if(node.prev != null)
            node.prev.next = node.next;
        
        if(node.next != null)
            node.next.prev = node.prev;
        
        if(node == tail)
            tail = node.prev;
        
        if(node == head)
            head = node.next;
    }
    
    private class Node implements Comparable<Node> {
        
        @Nullable
        public Node next, prev;
        
        public E value;
        
        Node(E val){
            this.value = val;
        }
    
        @Override
        public int compareTo(@NotNull Node o){
            return value.compareTo(o.value);
        }
    
        @Override
        public String toString(){
            return "Node{value=" + value + '}';
        }
    
    }
    
    private class Iter implements Iterator<E> {
        
        private Node current;
        
        Iter(){
            current = new Node(null);
            current.next = head;
        }
    
        @Override
        public boolean hasNext(){
            return current.next != null;
        }
    
        @Override
        public E next(){
            current = current.next;
            if(current == null)
                throw new NoSuchElementException();
            return current.value;
        }
    
    }
    
}

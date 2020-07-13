package academy.hekiyou.pudding.cache;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface StructuredList<E extends Comparable<? extends E>> extends Iterable<E> {
    
    void add(@NotNull E entry);
    
    void remove(@NotNull E entry);
    
    void restructure(@NotNull E changed);
    
    int size();
    
    boolean isEmpty();
    
    boolean hasHighest();
    
    @NotNull
    E getHighest();
    
    @NotNull
    E popHighest();
    
    boolean hasLowest();
    
    @NotNull
    E getLowest();
    
    @NotNull
    E popLowest();
    
    static void printList(StructuredList<?> list){
        System.out.print("Min = " + (list.hasLowest() ? list.getLowest() : "null") +
                ", Max = " + (list.hasHighest() ? list.getHighest() : "null") + ", Elements = ");
        
        if(list.size() < 10){
            Iterator<?> iter = list.iterator();
            if(iter.hasNext()){
                System.out.print(iter.next());
                while(iter.hasNext()){
                    System.out.print(", " + iter.next());
                }
            } else {
                System.out.print("empty list");
            }
        } else {
            // big list; don't bother printing
            System.out.print("... (too many elements)");
        }
        System.out.println();
    }
    
}

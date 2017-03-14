package aos;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;


public class CollectionsBinarySearchTest {
    
    @Test
    public void testCollections(){
        Node n1 = new Node(1, "1", "1");
        Node n2 = new Node(4, "4", "4");
        Node n3 = new Node(5, "5", "5");
        Node n4 = new Node(2, "2", "2");
        
        List<Node> list = new ArrayList<>();
        list.add(n1);
        list.add(n2);
        list.add(n3);
        list.add(n4);
        
        Collections.sort(list);
        
        int idx = -1;
        // Test node with id 2
        idx = Collections.binarySearch(list, new Node(2));
        System.out.println(idx);
        assertEquals(idx, 1);
        
        // Test node with id 4
        idx = Collections.binarySearch(list, new Node(4));
        System.out.println(idx);
        assertEquals(idx, 2);
    }
}

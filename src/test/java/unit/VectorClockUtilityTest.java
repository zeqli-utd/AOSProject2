package unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clock.VectorClock;

public class VectorClockUtilityTest {
    /**
     * Test happens before functionalities 
     * The time series graph refers from Wikipedia vector clock page
     * https://en.wikipedia.org/wiki/Vector_clock
     */
    @Test
    public void testHappensBefore(){
        VectorClock A1 = new VectorClock(3, 1);  // TopoSize = 3, id = 1
        VectorClock A2 = new VectorClock(3, 1);  // TopoSize = 3, id = 1
        VectorClock A3 = new VectorClock(3, 1);  // TopoSize = 3, id = 1
        VectorClock A4 = new VectorClock(3, 1);  // TopoSize = 3, id = 1
        
        VectorClock B1 = new VectorClock(3, 2);  // TopoSize = 3, id = 1
        VectorClock B2 = new VectorClock(3, 2);  // TopoSize = 3, id = 1
        VectorClock B3 = new VectorClock(3, 2);  // TopoSize = 3, id = 1
        VectorClock B4 = new VectorClock(3, 2);  // TopoSize = 3, id = 1
        VectorClock B5 = new VectorClock(3, 2);  // TopoSize = 3, id = 1
        
        VectorClock C1 = new VectorClock(3, 3);  // TopoSize = 3, id = 1
        VectorClock C2 = new VectorClock(3, 3);  // TopoSize = 3, id = 1
        VectorClock C3 = new VectorClock(3, 3);  // TopoSize = 3, id = 1
        VectorClock C4 = new VectorClock(3, 3);  // TopoSize = 3, id = 1
        VectorClock C5 = new VectorClock(3, 3);  // TopoSize = 3, id = 1
        
        
        int[][] v1 = {
                {0, 1, 2, 1},
                {0, 2, 2, 1},
                {0, 3, 3, 3},
                {0, 4, 5, 5}
            };
            int[][] v2 = {
                {0, 0, 1, 1},
                {0, 0, 2, 1},
                {0, 0, 3, 1},
                {0, 2, 4, 1},
                {0, 2, 5, 1},
            };
            
            int[][] v3 = {
                {0, 0, 0, 1},
                {0, 0, 3, 2},
                {0, 0, 3, 3},
                {0, 2, 5, 4},
                {0, 2, 5, 5}
            };
        A1.setVector(v1[0]);
        A2.setVector(v1[1]);
        A3.setVector(v1[2]);
        A4.setVector(v1[3]);
        
        B1.setVector(v2[0]);
        B2.setVector(v2[1]);
        B3.setVector(v2[2]);
        B4.setVector(v2[3]);
        B5.setVector(v2[4]);
        
        C1.setVector(v3[0]);
        C2.setVector(v3[1]);
        C3.setVector(v3[2]);
        C4.setVector(v3[3]);
        C5.setVector(v3[4]);
        
        // Same process A
        assertTrue(A1.happensBefore(A2));
        assertTrue(A1.happensBefore(A3));
        assertTrue(A1.happensBefore(A4));
        
        // Same process B
        assertTrue(B1.happensBefore(B2));
        assertTrue(B1.happensBefore(B3));
        assertTrue(B1.happensBefore(B4));
        assertTrue(B1.happensBefore(B5));
        
        // Same process C
        assertTrue(C1.happensBefore(C2));
        assertTrue(C1.happensBefore(C3));
        assertTrue(C1.happensBefore(C4));
        assertTrue(C1.happensBefore(C5));
        
        // Cross process C to A
        assertTrue(C1.happensBefore(A1));
        assertTrue(C1.happensBefore(A2));
        assertTrue(C1.happensBefore(A3));
        assertTrue(C1.happensBefore(A4));
        
        // Consistent state
        assertFalse(B4.happensBefore(C3));
        assertFalse(C3.happensBefore(B4));
        assertFalse(B4.happensBefore(A3));
        assertFalse(A3.happensBefore(B4));
    }
    
    @Test
    public void testConsistency(){
        VectorClock S0 = new VectorClock(5, 0);  // TopoSize = 3, id = 1
        VectorClock S1 = new VectorClock(5, 1);  // TopoSize = 3, id = 1
        VectorClock S2 = new VectorClock(5, 2);  // TopoSize = 3, id = 1
        VectorClock S3 = new VectorClock(5, 3);  // TopoSize = 3, id = 1
        VectorClock S4 = new VectorClock(5, 4);  // TopoSize = 3, id = 1
        S0.setVector(new int[]{51, 74, 42, 56, 21, 0});
        S1.setVector(new int[]{35, 80, 41, 76, 21, 0});
        S2.setVector(new int[]{35, 76, 66, 75, 21, 0});
        S3.setVector(new int[]{35, 75, 41, 76, 21, 0});
        S4.setVector(new int[]{34, 44, 36, 52, 43, 0});
        

        assertFalse(S0.happensBefore(S1));
        assertFalse(S0.happensBefore(S2));
        assertFalse(S0.happensBefore(S3));
        assertFalse(S0.happensBefore(S4));  
        

        assertFalse(S1.happensBefore(S0));
        assertFalse(S1.happensBefore(S2));
        assertFalse(S1.happensBefore(S3));
        assertFalse(S1.happensBefore(S4));
        

        assertFalse(S2.happensBefore(S0));
        assertFalse(S2.happensBefore(S1));
        assertFalse(S2.happensBefore(S3));
        assertFalse(S2.happensBefore(S4));
        

        assertFalse(S3.happensBefore(S0));
        //assertFalse(S3.happensBefore(S1));
        assertFalse(S3.happensBefore(S2));
        assertFalse(S3.happensBefore(S4));  
        

        assertFalse(S4.happensBefore(S0));
        assertFalse(S4.happensBefore(S1));
        assertFalse(S4.happensBefore(S2));
        assertFalse(S4.happensBefore(S3));
    }
}

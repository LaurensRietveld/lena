package de.unikoblenz.isweb.lena.util;

public class Clock {
    private long start;
    
    public Clock(){}
    
    public void start() {
        start = System.currentTimeMillis(); // start timing
    }
    
    public Float timeElapsed() {
        return new Float((System.currentTimeMillis() - start)/1000F);
    }
    
    public String toString() {
        return Float.toString(timeElapsed());// print execution time
    }
}

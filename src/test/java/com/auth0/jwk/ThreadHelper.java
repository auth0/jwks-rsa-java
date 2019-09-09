package com.auth0.jwk;

import java.util.ArrayList;
import java.util.List;

public class ThreadHelper extends Thread {

    private Object lock = new Object();

    private final List<Runnable> runnables = new ArrayList<>();

    @Override
    public void run() {
        int i = 0;
        try {
            while(i < runnables.size()) {
                runnables.get(i).run();
                
                i++;
            }
        } catch (Exception e) {
            // ignore, run rest with out locking
            while(i < runnables.size()) {
                runnables.get(i).run();
                
                i++;
            }
        }
    }

    public void next() {
        synchronized(lock) {
            lock.notifyAll();
        }
    }

    public void close() {
        synchronized(lock) {
            lock.notifyAll();
        }
        
        this.interrupt();
    }
    
    public void begin() {
        start();
        while(getState() != State.WAITING && getState() != State.TERMINATED) {
            Thread.yield();
        }        
    }
    
    public ThreadHelper addRun(Runnable runnable) {
        runnables.add(runnable);
        
        return this;
    }
    
    public ThreadHelper addPause() {
        runnables.add(new Runnable() {
            
            @Override
            public void run() {
                synchronized(lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        });
        
        return this;
    }
}

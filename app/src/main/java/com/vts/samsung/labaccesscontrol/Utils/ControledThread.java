package com.vts.samsung.labaccesscontrol.Utils;

import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

public class ControledThread implements Runnable {

    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int interval;

    public ControledThread(int sleepInterval) {
        interval = sleepInterval;
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                Toast.makeText(Application.getInstance().getApplicationContext(), "Provera NETa", Toast.LENGTH_SHORT).show();
                Thread.sleep(interval);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            // do something here
        }
    }
}
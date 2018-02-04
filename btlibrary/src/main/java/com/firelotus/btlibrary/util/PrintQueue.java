package com.firelotus.btlibrary.util;

import android.content.Context;

import java.util.ArrayList;

/**
 * <p/>
 * this is print queue.
 * you can simple add print bytes to queue. and this class will send those bytes to bluetooth device
 */
public class PrintQueue {

    /**
     * instance
     */
    private static PrintQueue mInstance;
    /**
     * context
     */
    private static Context mContext;

    /**
     * print queue
     */
    private ArrayList<byte[]> mQueue = new ArrayList<byte[]>();

    private PrintQueue() {
    }

    public ArrayList<byte[]> getmQueue() {
        return mQueue;
    }

    public static PrintQueue getQueue(Context context) {
        if (null == mInstance) {
            mInstance = new PrintQueue();
        }
        if (null == mContext) {
            mContext = context;
        }
        return mInstance;
    }

    /**
     * add print bytes to queue. and call print
     *
     * @param bytes bytes
     */
    public synchronized void add(byte[] bytes) {
        if (null != bytes) {
            mQueue.add(bytes);
        }
    }

    /**
     * add print bytes to queue. and call print
     *
     * @param bytesList bytesList
     */
    public synchronized void add(ArrayList<byte[]> bytesList) {
        if (null != bytesList) {
            mQueue.addAll(bytesList);
        }
    }

}

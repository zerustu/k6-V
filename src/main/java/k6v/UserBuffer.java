package k6v;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

public class UserBuffer {
    BlockingQueue<byte[]> dataQueue;
    ArrayList<byte[]> framebuffer;
    int index;
    int capacity;

    public boolean ready;
    public boolean run;

    public UserBuffer(int capacity)
    {
        dataQueue = new ArrayBlockingQueue<>(capacity);
        framebuffer = new ArrayList<byte[]>(capacity);
        index = 0;
        this.capacity = capacity;

        ready = true;
        run = true;
    }

    public void addData(byte[] inData)
    {
        if (dataQueue.remainingCapacity() <= 0) {
            System.err.println("A user buffer ran out of space");
        }
        dataQueue.offer(inData);
    }

    public List<byte[]> getData()
    {
        ready = false;
        ArrayList<byte[]> result = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            int j = (i + index) % capacity;
            result.add(framebuffer.get(j));
        }
        return result;
    }

    public void register()
    {
        @Nullable byte[] currentByte = null;
        while (run) {
            if (currentByte == null)
            {
                if (!dataQueue.isEmpty()) {
                    try {
                        currentByte = dataQueue.poll(20, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                    }
                    //System.out.println("processing new data");
                }
            }
            if (currentByte != null)
            {
                framebuffer.set(index, currentByte);
                if (index >= capacity) {
                    index = 0;
                }
            }
            
        }
    }
}

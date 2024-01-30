package k6v;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jetbrains.annotations.Nullable;

public class UserBuffer {
    BlockingQueue<byte[]> dataQueue;
    short[] framebuffer;
    int index;
    int size;
    int length;

    boolean ready;
    boolean run;

    public UserBuffer(int length, int packetNumber, int capacity)
    {
        dataQueue = new ArrayBlockingQueue<>(capacity);
        this.length = length;
        size = length * packetNumber;
        framebuffer = new short[size];
        index = 0;

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

    public List<short[]> getData()
    {
        ready = false;
        ArrayList<short[]> result = new ArrayList<>();
        short[] sample = null;
        for (int i = 0; i < size; i++) {
            int j = i % length;
            if (j == 0 || sample == null) {
                sample = new short[length];
            }
            sample[j] = framebuffer[index];
            framebuffer[index] = 0;
            index++;
            if (index >= size) index = 0;
            if (j == length-1)
            {
                result.add(sample);
            }
        }
        return result;
    }

    public void register()
    {
        @Nullable byte[] currentByte = null;
        short samplebuffer = 0;
        int j = 0;
        while (run) {
            if (currentByte == null)
            {
                if (!dataQueue.isEmpty()) {
                    currentByte = dataQueue.poll();
                    //System.out.println("processing new data");
                }
            }
            if (currentByte != null)
            {
                short sample = STTModule.formatConvert(currentByte[4*j], currentByte[4*j+1], currentByte[4*j+2], currentByte[4*j+3]);
                j++;
                samplebuffer += sample;

                if (j%3 == 0)
                {
                    while (!ready){};
                    framebuffer[index] = samplebuffer;
                    samplebuffer = 0;
                    index++;
                }
                if (index >= size) {
                    index = 0;
                }
                if (4*j >= currentByte.length)
                {
                    currentByte = null;
                    j = 0;
                }
            }
            
        }
    }
}

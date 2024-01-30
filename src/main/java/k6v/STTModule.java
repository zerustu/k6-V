package k6v;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jetbrains.annotations.Nullable;

import ai.picovoice.cheetah.Cheetah;
import ai.picovoice.cheetah.CheetahException;
import ai.picovoice.cheetah.CheetahTranscript;
import ai.picovoice.porcupine.*;
import net.dv8tion.jda.api.entities.User;

public class STTModule {

    static final Float limite = (float) (0);

    BlockingQueue<byte[]> fifoQueue;
    BlockingQueue<List<String>> userQueue;
    HashMap<String, Float> usersProba;
    List<String> activUsers;

    Cheetah localcheetah;
    Porcupine localporc;
    String transcript;
    String LastTranscript;
    Boolean run;

    public void addData(byte[] dataIn, List<User> users)
    {
        if (fifoQueue.remainingCapacity() > 0)
        {
            fifoQueue.add(dataIn);
            List<String> usersString = new ArrayList<>();
            users.forEach((u) -> {usersString.add(u.toString());});
            userQueue.add(usersString);
            for (User user : users) {
                usersProba.putIfAbsent(user.toString(), 0f);
            }
        }
        else
        {
            System.err.println("Fifo for voice control OverFLow, droping package");
        }
    }

    protected short formatConvert(byte left1, byte left2, byte right1, byte right2)
    {
        short leftSample = (short) ((left1 << 8) | (left2 & 0xFF));
        short rightSample = (short) ((right1 << 8) | (right2 & 0xFF));
        return (short) ((leftSample + rightSample)/2);
    }

    protected void feedSTT(short[] dataIn)
    {
        int keywordsIndex;
        try {
            keywordsIndex = localporc.process(dataIn);
            if (keywordsIndex == 0) {
                System.out.println("K6-V was detected !");
                usersProba.forEach((k,v) -> {System.out.println("la probabilité que ce soit " + k + " est de " + (v));});
            }
        } catch (PorcupineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        CheetahTranscript transcriptObj;
        try {
            transcriptObj = localcheetah.process(dataIn);
            transcript += transcriptObj.getTranscript();
            if (transcript.compareTo(LastTranscript) != 0)
            {
                System.out.println(transcript);
                LastTranscript = transcript;
            }

            if (transcriptObj.getIsEndpoint()) {
                flushSTT(localcheetah);
            }

        } catch (CheetahException e) {
            e.printStackTrace();
        } */
    }

    protected void flushSTT(Cheetah localcheetah)
    {
        /*
        CheetahTranscript finalTranscriptObj;
        try {
            finalTranscriptObj = localcheetah.flush();
            transcript += finalTranscriptObj.getTranscript();
            System.out.println(transcript);
            LastTranscript = transcript;
            transcript = "";
        } catch (CheetahException e) {
            e.printStackTrace();
        } */
    }

    protected void ProcessData()
    {
        int framelength = localporc.getFrameLength();
        short[] framebuffer = new short[framelength];
        @Nullable byte[] currentByte = null;
        int bufferIndex = 0;
        short samplebuffer = 0;
        short lastSample = 0;

        int j = 0;
        int TimeOutTimer = 0;

        System.out.println("Process data is alive");
        while (run) {
            if (currentByte == null)
            {
                if (!fifoQueue.isEmpty()) {
                    currentByte = fifoQueue.poll();
                    activUsers = userQueue.poll();
                    TimeOutTimer = 0;
                    //System.out.println("processing new data");
                }
                else if (TimeOutTimer > 50)
                {
                    System.out.println("TimeOuted, let's flush the data");
                    if (j != 0)
                    {
                        framebuffer[bufferIndex] = samplebuffer;
                        samplebuffer = 0;
                        j = 0;
                        bufferIndex++;
                    }
                    if (bufferIndex != 0)
                    {
                        while (bufferIndex < framelength) {
                            framebuffer[bufferIndex] = (short) 0;
                            bufferIndex++;
                        }
                    }
                    TimeOutTimer = -1;
                }
                else if (TimeOutTimer >= 0)
                {
                    TimeOutTimer++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (currentByte != null)
            {
                short sample = formatConvert(currentByte[4*j], currentByte[4*j+1], currentByte[4*j+2], currentByte[4*j+3]);
                j++;
                samplebuffer += sample;

                if (j%3 == 0)
                {
                    lastSample = samplebuffer;
                    framebuffer[bufferIndex] = samplebuffer;
                    samplebuffer = 0;
                    bufferIndex++;
                    int coef = java.lang.Math.abs(lastSample - samplebuffer);
                    usersProba.replaceAll((k, v) -> {
                        if (activUsers.contains(k))
                            return v*0.9f + coef;
                        return v * 0.9f;
                    });
                }
                if (4*j >= currentByte.length)
                {
                    currentByte = null;
                    j = 0;
                }
            }
            if (bufferIndex == framelength) {
                //System.out.println("let's send some data");
                feedSTT(framebuffer);
                if (TimeOutTimer == -1) {
                    flushSTT(localcheetah);
                    usersProba.forEach((k, v) -> {v = 0f;});
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                bufferIndex = 0;
            }
        }
        localporc.delete();
        System.out.println("ProcessData is now dead");
    }

    /**
     * @param accessKey String of the accessKey to PicoVoice
     * @param porcupinePath String to a path to builtin Keywords for porcupine
     * @param capacity 
     * @throws CheetahException
     * @throws PorcupineException 
     */
    public STTModule(String accessKey, String porcupinePath, int capacity, String porcupineModelPath) throws CheetahException, PorcupineException
    {
        run = true;

        fifoQueue = new ArrayBlockingQueue<byte[]>(capacity);
        userQueue = new ArrayBlockingQueue<List<String>>(capacity);
        activUsers = new ArrayList<String>();
        transcript = "";
        LastTranscript = "";
        usersProba = new HashMap<>();
        //localcheetah = new Cheetah.Builder().setAccessKey(accessKey).setEnableAutomaticPunctuation(true).build();
        localporc = new Porcupine.Builder().setAccessKey(accessKey).setKeywordPath(porcupinePath).setModelPath(porcupineModelPath).build();
    }
}

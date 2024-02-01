package k6v;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.*;

import net.dv8tion.jda.api.audio.AudioSendHandler;
public class SendModule implements AudioSendHandler {

    AudioInputStream inStream;
    int buffersize;
    byte[] bb;
    boolean ready;
    boolean is_reading;

    public SendModule() {
        buffersize = 960;//(int)(20*INPUT_FORMAT.getSampleRate()/1000);
        bb = new byte[buffersize*4];
        ready = false;
        is_reading = false;
        //System.out.println("is there a difference between " + INPUT_FORMAT.getFrameSize() + " and " + (int)(20*INPUT_FORMAT.getSampleRate()/1000));
    }

    @Override
    public boolean canProvide() {
        return ready;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        Thread gonnaprocess = new Thread(() -> processData());
        gonnaprocess.start();
        return ByteBuffer.wrap(bb);
    }

    private void processData() {
        ready = false;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] tempbuf = new byte[buffersize*2/3];
        int byteread;
        try {
            byteread = inStream.read(tempbuf, 0, buffersize*2/3);
            if (byteread == -1) 
            {
                inStream.close();
                inStream = null;
                is_reading = false;
                return;
            }
            for (int i = 0; i < byteread; i+=2) {
                bb[6*i] = tempbuf[i+1];
                bb[6*i + 1] = tempbuf[i];
                bb[6*i + 2] = tempbuf[i+1];
                bb[6*i + 3] = tempbuf[i];
                bb[6*i + 4] = tempbuf[i+1];
                bb[6*i + 5] = tempbuf[i];
                bb[6*i + 6] = tempbuf[i+1];
                bb[6*i + 7] = tempbuf[i];
                bb[6*i + 8] = tempbuf[i+1];
                bb[6*i + 9] = tempbuf[i];
                bb[6*i + 10] = tempbuf[i+1];
                bb[6*i + 11] = tempbuf[i];
            }
            for (int i = byteread*6; i < buffersize*4; i++) {
                bb[i] = 0;
            }
            ready = (byteread != 0);
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    public void load(String path)
    {
        try {
            inStream = AudioSystem.getAudioInputStream(new File(path));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        is_reading = true;
        /*System.out.println("the file have been loaded");
        System.out.println(inStream.getFormat());
        System.out.println(INPUT_FORMAT);
        System.out.println("input framerate:" + inStream.getFormat().getFrameRate() + "\ninput sample rate : " + inStream.getFormat().getSampleRate() + "\n out frame rate : " + INPUT_FORMAT.getFrameRate() + "\n input sample rate : " + INPUT_FORMAT.getSampleRate());
        */
        processData();
    }

    public void respond(String message)
    {
        is_reading = true;
        try {
            //SynthetiseurMbrola synth = new SynthetiseurMbrola(path, path, message, path, buffersize);
            FileWriter text = new FileWriter("H:\\k6v\\main\\textToRead.txt");
            text.write(message + "\n");
            text.close();

            String[] command = {
                "java",
                "-jar",
                "H:\\k6v\\SI_VOX-src\\SI_VOX.jar",
                "-f",
                "H:\\k6v\\main\\textToRead.txt",
                "H:\\k6v\\main\\audio"
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process sivox = processBuilder.start();
            sivox.waitFor();

            load("H:\\k6v\\main\\audio.wav");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
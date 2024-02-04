package k6v;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

import javax.sound.sampled.*;

import net.dv8tion.jda.api.audio.AudioSendHandler;
public class SendModule implements AudioSendHandler {

    AudioInputStream inStream;
    int buffersize;
    BlockingQueue<byte[]> samples;
    boolean is_reading;


    /**
     * create a 'SendModule' that implements 'AudioSendHandler', it link itself to the 'STTModule' to get feed back information
     * @param transcriver 'STTModule' to know when to play sounds
     */
    public SendModule(STTModule transcriver) {
        buffersize = (int)(20*INPUT_FORMAT.getFrameRate()/1000*INPUT_FORMAT.getFrameSize());
        is_reading = false;
        transcriver.sender = this;
        samples = new ArrayBlockingQueue<>(16);
    }

    @Override
    public boolean canProvide() {
        return samples.size() > 0;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        System.out.println("sending some data");
        return ByteBuffer.wrap(samples.poll());
    }

    private void processData() {
        int byteread;
        while(true) {
            try {
                byte[] buffer = new byte[buffersize];
                byteread = inStream.read(buffer, 0, buffersize);
                if (byteread == -1) 
                {
                    inStream.close();
                    inStream = null;
                    is_reading = false;
                    System.out.println("done reading file. is_reading is set to false");
                    return;
                }
                for (int i = byteread; i < buffersize; i++) {
                    buffer[i] = 0;
                    System.out.println("I must fill in some data!");
                }
                samples.put(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    /**
     * play a audio file give by the path to the file.
     * only one file can be read at a time
     * @param path path to the audio file to play
     * @throws IllegalStateException an audio file is already playing and is not finished
     * @throws IOException if the File does not point to valid audio file data recognized by the system
     * @throws UnsupportedAudioFileException if an I/O exception occurs
     */
    public void load(String path) throws IllegalStateException, UnsupportedAudioFileException, IOException
    {
        if (is_reading) throw new IllegalStateException("The previous file is not finished being read");
        is_reading = true;
        loadNoCheck(path);
    }

    protected void loadNoCheck(String path) throws UnsupportedAudioFileException, IOException
    {
        AudioInputStream FileStream = AudioSystem.getAudioInputStream(new File(path));
        inStream = AudioSystem.getAudioInputStream(INPUT_FORMAT, FileStream);
        new Thread(() -> processData()).start();
    }

    public void respond(String message) throws IllegalStateException
    {
        if (is_reading) throw new IllegalStateException("The previous file is not finished being read");
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

            loadNoCheck("H:\\k6v\\main\\audio.wav");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
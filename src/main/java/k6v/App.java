package k6v;

import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Hello world!
 *
 */
public class App 
{

    public static STTModule sttDecoder;

    static String[] ops;

    public static void main( String[] args ) throws Exception
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        FileReader reader = new FileReader("option.json");

         //Read JSON file
        JSONObject obj = (JSONObject)jsonParser.parse(reader);
        ops = (String[])obj.get("Op");
        String BOT_TOKEN = (String)obj.get("DiscordKey");

        sttDecoder = new STTModule((String)obj.get("PicoVoiceKey"), (String)obj.get("PorcupineKeyPath"), 10000, (String)obj.get("PorcupineModelPath"));
        Thread picovoicThread = new Thread(() -> {sttDecoder.ProcessData();});
        picovoicThread.start();

        JDA api = JDABuilder.createDefault(BOT_TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).build();

        api.addEventListener(new CommandHandler());
        api.addEventListener(new VocalModule());
    }
}
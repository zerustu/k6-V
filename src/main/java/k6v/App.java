package k6v;

import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import k6v.memory.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Hello world!
 *
 */
public class App 
{
    static Option opts;
    
    static Memory mem;
    public static void main( String[] args ) throws Exception
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        FileReader reader = new FileReader("option.json");

         //Read JSON file
        JSONObject obj = (JSONObject)jsonParser.parse(reader);

        opts = new Option(obj);

        JDA api = JDABuilder.createDefault(opts.getDiscordToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).build();

        mem = new Memory(api, opts);

        api.addEventListener(new CommandHandler());
        api.addEventListener(new VocalModule());
    }
}
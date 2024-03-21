package k6v.memory;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class Option {
    protected JSONObject option;

    protected String discordToken;

    protected ArrayList<Long> ops;

    public String getDiscordToken() {
        return discordToken;
    }

    public ArrayList<Long> getOps() {
        return ops;
    }

    public String Si_voix_folder;

    public Option(JSONObject options) {
       this.option = options;
       this.discordToken = (String)option.get("DiscordKey");
       this.ops = (ArrayList<Long>)options.get("Op");
       this.Si_voix_folder = (String)option.get("si_voix_folder");
    }

    @Override
    public String toString()
    {
        String result = String.format(
            "Option values (truncated for security): {\n" + 
            "   discord Token : %.4s\n" +
            "   List of Op user : %s\n" +
            "}",
            discordToken, ops
        );
        return result;
    }

    protected String getFullString()
    {
        String result = String.format(
            "Option values : {\n" + 
            "   discord Token : %s\n" +
            "   List of Op user : %s\n" +
            "}",
            discordToken, ops
        );
        return result;
    }
}

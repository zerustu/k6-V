package k6v.memory;

public class Option {
    protected String discordToken;

    protected String picoToken;
    protected String porcupineModelPath;
    protected String porcupineKeyPath;
    protected String rhinoModelPath;
    protected String rhinoKeyPath;

    protected long[] ops;

    public String getDiscordToken() {
        return discordToken;
    }

    public String getPicoToken() {
        return picoToken;
    }

    public String getPorcupineModelPath() {
        return porcupineModelPath;
    }

    public String getPorcupineKeyPath() {
        return porcupineKeyPath;
    }

    public String getRhinoModelPath() {
        return rhinoModelPath;
    }

    public String getRhinoKeyPath() {
        return rhinoKeyPath;
    }

    public long[] getOps() {
        return ops;
    }

    public Option(String discordToken, String picoToken, String porcupineModelPath, String porcupineKeyPath,
            String rhinoModelPath, String rhinoKeyPath, long[] ops) {
        this.discordToken = discordToken;
        this.picoToken = picoToken;
        this.porcupineModelPath = porcupineModelPath;
        this.porcupineKeyPath = porcupineKeyPath;
        this.rhinoModelPath = rhinoModelPath;
        this.rhinoKeyPath = rhinoKeyPath;
        this.ops = ops;
    }

    @Override
    public String toString()
    {
        String result = String.format(
            "Option values (truncated for security): {\n" + 
            "   discord Token : %.4s\n" +
            "   picovoice Token : %.4s\n" +
            "   porcupine Model Path : %s\n" +
            "   porcupine Key Path : %s\n" +
            "   rhino Model Path : %s\n" +
            "   rhino Key Path : %s\n" +
            "   List of Op user : %s\n" +
            "}",
            discordToken, picoToken, porcupineModelPath, porcupineKeyPath, 
            rhinoModelPath, rhinoKeyPath, ops
        );
        return result;
    }

    protected String getFullString()
    {
        String result = String.format(
            "Option values : {\n" + 
            "   discord Token : %s\n" +
            "   picovoice Token : %s\n" +
            "   porcupine Model Path : %s\n" +
            "   porcupine Key Path : %s\n" +
            "   rhino Model Path : %s\n" +
            "   rhino Key Path : %s\n" +
            "   List of Op user : %s\n" +
            "}",
            discordToken, picoToken, porcupineModelPath, porcupineKeyPath, 
            rhinoModelPath, rhinoKeyPath, ops
        );
        return result;
    }
}

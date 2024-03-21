package k6v.memory;

import net.dv8tion.jda.api.JDA;

public class Memory {

    protected JDA client;
    public JDA getClient() {
        return client;
    }

    private long id;
    protected Option options;

    public Option getOptions() {
        return options;
    }

    protected OTTModule ottAuth;

    public Memory(JDA jda, Option opts)
    {
        client = jda;
        this.options = opts; 
        ottAuth = new OTTModule();
    }

    public long getId()
    {
        return id;
    }

    @Override
    public String toString() {
        return "Memory object (id " + id + ")\n" + 
        options.toString() + "\n" +
        ottAuth.toString() + "\n";
    }

    public String toFullString()
    {
        return "Memory object (id " + id + ")\n" + 
        options.getFullString() + "\n";
    }

    public boolean reset()
    {
        return true;
    }

    public boolean isOp(long id)
    {
        for (long op : this.options.getOps()) {
            if (op == id) return true;
        }
        return false;
    }

    public boolean checkOTP(long otp)
    {
        return ottAuth.checkOTP(otp);
    }
}

package k6v.memory;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public Memory(JDA jda, File jsonOption) throws StreamReadException, DatabindException, IOException
    {
        ObjectMapper objmapper = new ObjectMapper();
        client = jda;
        id = client.getSelfUser().getIdLong();
        options = objmapper.readValue(jsonOption, Option.class);
    }

    public long getId()
    {
        return id;
    }

    @Override
    public String toString() {
        return "Memory object (id " + id + ")\n" + options.toString();
    }

    public String toFullString()
    {
        return "Memory object (id " + id + ")\n" + options.getFullString();
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
}
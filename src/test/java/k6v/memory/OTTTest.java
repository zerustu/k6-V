package k6v.memory;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OTTTest {

    @Test
    public void nullToken() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        OTTModule module = new OTTModule();

        Field field = OTTModule.class.getDeclaredField("otp");
        field.setAccessible(true);
        long otpValue = (long) field.get(module);

        assertTrue(otpValue == -1);
    }

    @Test
    public void canGenerateToken()
    {
        OTTModule module = new OTTModule();
        long token = module.genOTP(0);
        assertTrue(token != -1);
    }
    
    @Test
    public void nullTokenCantlog()
    {
        OTTModule module = new OTTModule();
        
        long token 
    }
}

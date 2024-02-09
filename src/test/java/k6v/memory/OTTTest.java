package k6v.memory;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
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
        
        assertFalse(module.checkOTP(0));
        assertFalse(module.checkOTP(1));
        assertFalse(module.checkOTP(-1));
        
        assertFalse(module.checkOTP((long) Math.random() * Long.MAX_VALUE));
    }

    @Test
    public void canAuth()
    {
        OTTModule module = new OTTModule();
        long token = module.genOTP(0);
        
        assertFalse(module.checkOTP(0));
        assertFalse(module.checkOTP(1));
        assertFalse(module.checkOTP(-1));

        assertFalse(module.checkOTP(token-1));
        assertFalse(module.checkOTP(token+1));
        assertTrue(module.checkOTP(token));

        assertFalse(module.checkOTP(0));
        assertFalse(module.checkOTP(1));
        assertFalse(module.checkOTP(-1));

        assertFalse(module.checkOTP(token-1));
        assertFalse(module.checkOTP(token+1));
        assertFalse(module.checkOTP(token));
    }

    @Test
    public void Timeout() throws InterruptedException
    {
        OTTModule module = new OTTModule();
        long token = module.genOTP(5);

        // Use CountDownLatch to wait for the timeout
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Thread.sleep(6000); // Wait for 3 seconds (more than the timeout)
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for the timeout or until token is revoked
        latch.await();

        assertFalse(module.checkOTP(token));
    }
}

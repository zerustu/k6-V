package k6v.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OTTModule {
    private static final long DEFAULT_TIMEOUT = 60; // Default timeout in seconds

    private long otp;
    private Map<Long, ScheduledFuture<?>> scheduledTasks;
    private ScheduledExecutorService executor;

    public OTTModule() {
        otp = -1;
        scheduledTasks = new ConcurrentHashMap<>();
        executor = Executors.newScheduledThreadPool(1);
    }

    public synchronized long genOTP(int timeout) throws IllegalStateException {
        if (otp != -1) {
            throw new IllegalStateException("A token is already generated");
        }

        otp = generateRandomOTP();
        long timeoutSeconds = (timeout > 0) ? timeout : DEFAULT_TIMEOUT;
        ScheduledFuture<?> task = executor.schedule(() -> {
            revokeOTP(otp);
        }, timeoutSeconds, TimeUnit.SECONDS);
        scheduledTasks.put(otp, task);

        return otp;
    }

    public synchronized boolean checkOTP(long token) {
        if (otp == -1) return false;
        if (token == otp) {
            revokeOTP(token);
            return true;
        }
        return false;
    }

    private void revokeOTP(long token) {
        ScheduledFuture<?> task = scheduledTasks.get(token);
        if (task != null) {
            task.cancel(false);
            scheduledTasks.remove(token);
        }
        otp = -1; // Reset OTP
    }

    public void revokeOTP()
    {
        revokeOTP(otp);
    }

    private long generateRandomOTP() {
        // Generate your random OTP here
        return (long) (Math.random() * Long.MAX_VALUE);
    }

    @Override
    public String toString()
    {
        if (otp == -1) {
            return "OTT : no OTP token is valid";
        }
        ScheduledFuture<?> task = scheduledTasks.get(otp);
        if (task == null)
        {
            otp = -1;
            return "OTT : Warning! : an OTP token existe but with no revoker ! it got revoked";
        }
        return "OTT : an OTP token exist, it will expire in " + task.getDelay(TimeUnit.SECONDS) + "s";
    }

    public String getFullString()
    {
        if (otp == -1) {
            return "OTT : no OTP token is valid";
        }
        ScheduledFuture<?> task = scheduledTasks.get(otp);
        if (task == null)
        {
            otp = -1;
            return "OTT : Warning! : an OTP token existe but with no revoker ! it got revoked";
        }
        return "OTT : " + otp + ", it will expire in " + task.getDelay(TimeUnit.SECONDS) + "s";
    }
}

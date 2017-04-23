package de.warhog.fpvlaptracker.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ShutdownUtil.class);

    /**
     * shutdown computer
     *
     * @throws java.io.IOException
     */
    public static void shutdown() throws IOException {
        String shutdownCommand;
        if (SystemUtils.IS_OS_LINUX) {
            shutdownCommand = "/sbin/shutdown -a -h now";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            shutdownCommand = "shutdown.exe -s -t 0";
        } else {
            throw new UnsupportedOperationException("Not supported operating system.");
        }

        Process process = Runtime.getRuntime().exec(shutdownCommand);
        try {
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOG.error("interrupted during process wait");
        }
        LOG.info("exit code: " + process.exitValue());
        if (process.exitValue() != 0) {
            throw new RuntimeException("cannot shutdown machine");
        }
    }

}

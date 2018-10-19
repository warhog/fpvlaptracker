package de.warhog.fpvlaptracker;

import de.warhog.fpvlaptracker.communication.UdpHandler;
import de.warhog.fpvlaptracker.db.Db;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan
@EnableScheduling
public class Laptracker {

    private static final Logger LOG = LoggerFactory.getLogger(Laptracker.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        LOG.info("fpvlaptracker starting up...");
        try {
            Db.createDatabase();
        } catch (Exception ex) {
            LOG.error("cannot setup database: " + ex.getMessage(), ex);
            System.exit(1);
        }

        ConfigurableApplicationContext ctx = SpringApplication.run(Laptracker.class, args);

        UdpHandler udpHandler = ctx.getBean(UdpHandler.class);
        try {
            udpHandler.setup();
        } catch (Exception ex) {
            LOG.error("error during udp thread setup: " + ex.getMessage(), ex);
            System.exit(1);
        }

    }

}

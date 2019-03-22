package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.communication.UdpHandler;
import java.awt.Color;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LedService {

    private static final Logger LOG = LoggerFactory.getLogger(LedService.class);

    private List<InetAddress> inetAddresses = new ArrayList<>();

    @Autowired
    UdpHandler udpHandler;

    private void send(String data) {
        if (!available()) {
            return;
        }
        for (InetAddress inetAddress : inetAddresses) {
            udpHandler.sendDataUnicast(inetAddress, data);
        }
    }

    public void blinkColor(Color color, Integer intervalMs) {
        send("led blink " + getColor(color) + " " + intervalMs);
    }

    public void staticColor(Color color) {
        send("led static " + getColor(color));
    }

    public void rightColor(Color color, Integer intervalMs) {
        send("led right " + getColor(color) + " " + intervalMs);
    }

    public void leftColor(Color color, Integer intervalMs) {
        send("led left " + getColor(color) + " " + intervalMs);
    }

    public void countdownColor(Color color, Integer intervalMs) {
        send("led countdown " + getColor(color) + " " + intervalMs);
    }
    
    public void expandColor(Color color, Integer intervalMs) {
        send("led expand " + getColor(color) + " " + intervalMs);
    }
    
    public void off() {
        send("led static black");
    }

    private String getColor(Color color) {
        if (color == Color.RED) {
            return "red";
        } else if (color == Color.GREEN) {
            return "green";
        } else if (color == Color.BLUE) {
            return "blue";
        } else if (color == Color.WHITE) {
            return "white";
        } else if (color == Color.YELLOW) {
            return "yellow";
        } else if (color == Color.CYAN) {
            return "cyan";
        } else if (color == Color.MAGENTA) {
            return "magenta";
        } else if (color == Color.PINK) {
            return "pink";
        } else if (color == Color.ORANGE) {
            return "orange";
        }
        return "black";
    }

    public boolean available() {
        return inetAddresses.size() > 0;
    }

    public void addInetAddress(InetAddress inetAddress) {
        inetAddresses.add(inetAddress);
    }
    
}

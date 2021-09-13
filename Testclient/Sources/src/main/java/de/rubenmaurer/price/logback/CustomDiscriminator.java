package de.rubenmaurer.price.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;

import java.util.Map;

public class CustomDiscriminator implements Discriminator<ILoggingEvent> {

    private String key;

    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();
        if (map == null) return "unknown";

        String value = map.get("runtime-id");
        String test = map.get("test");
        if (value == null || test == null) return "unknown";
        return String.format("%s/%s", value, test);
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }
}

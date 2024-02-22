package io.pixel.api;

import io.pixel.api.event.Event;
import io.pixel.pcall.PixelCraft;
import org.apache.logging.log4j.Logger;

public final class PixelAPI {
    private PixelAPI(){}


    public static void shutdown(){
        PixelCraft.LOGGER.info("Stopping server...");
        PixelCraft.instance.setRunning(false);
        PixelCraft.instance.getNetwork().close();
        System.exit(0);
    }

    public static Logger getServerLogger(){
        return PixelCraft.LOGGER;
    }

    public static void callEvent(Event event){

    }
}

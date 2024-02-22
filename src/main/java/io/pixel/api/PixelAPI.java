package io.pixel.api;

import io.pixel.PixelCraft;
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
}

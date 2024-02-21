package io.pixel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.pixel.network.NetworkServer;
import io.pixel.network.ServerStatusResponse;
import io.pixel.util.ServerConfig;
import io.pixel.util.text.TextComponentString;
import joptsimple.OptionSet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class PixelCraft implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(PixelCraft.class);
    static PixelCraft instance;
    NetworkServer network;
    ServerConfig config;
    Thread server_thread;
    ServerStatusResponse statusResponse;

    public PixelCraft(ServerConfig config){
        this.config = config;
        this.network = new NetworkServer(this);
        this.statusResponse = new ServerStatusResponse();
        this.server_thread = new Thread(this);
        this.server_thread.setName("Server Thread");
        this.server_thread.start();
    }

    public static void launch(OptionSet option){
        try {
            LOGGER.info("Launching auth server...");
            ServerConfig config = new ServerConfig((File) option.valueOf("config"));
            PixelCraft.instance = new PixelCraft(config);
        }catch (Exception e){
            LOGGER.fatal("Launch server was throw exception.",e);
            System.exit(-1);
        }
    }

    public ServerConfig getConfig() {
        return config;
    }

    public ServerStatusResponse getServerStatusResponse() {
        return this.statusResponse;
    }

    public void applyServerIconToResponse(ServerStatusResponse response) {
        File file1 = new File("server-icon.png");

        if(!file1.exists()){
            LOGGER.error("Couldn't load server icon.");
            return;
        }

        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);
                response.setFavicon("data:image/png;base64," + bytebuf1.toString(StandardCharsets.UTF_8));
            } catch (Exception exception) {
                LOGGER.error("Couldn't load server icon", (Throwable) exception);
            } finally {
                bytebuf.release();
            }
        }
    }

    @Override
    public void run() {
        this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
        this.statusResponse.setServerDescription(new TextComponentString(config.getMotd()));
        this.statusResponse.setPlayers(new ServerStatusResponse.Players(0,0));
        applyServerIconToResponse(this.statusResponse);
        this.network.connect();
    }
}

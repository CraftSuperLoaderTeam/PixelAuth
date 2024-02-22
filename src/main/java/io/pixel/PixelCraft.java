package io.pixel;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.pixel.network.NetworkServer;
import io.pixel.network.ServerStatusResponse;
import io.pixel.util.CryptManager;
import io.pixel.util.ServerConfig;
import io.pixel.util.text.TextComponentString;
import joptsimple.OptionSet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.UUID;

public class PixelCraft implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(PixelCraft.class);
    static PixelCraft instance;
    NetworkServer network;
    ServerConfig config;
    Thread server_thread;
    ServerStatusResponse statusResponse;
    private KeyPair serverKeyPair;
    private String serverOwner;
    boolean isRunning;
    private boolean field_190519_A;
    MinecraftSessionService sessionService;

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
            if (option.has("singleplayer")) {
                String s = (String) option.valueOf("singleplayer");
                if(s != null) PixelCraft.instance.setServerOwner(s);
            }
            LOGGER.info("Generating keypair");
        }catch (Exception e){
            LOGGER.fatal("Launch server was throw exception.",e);
            System.exit(-1);
        }
    }

    public void setServerOwner(String serverOwner) {
        this.serverOwner = serverOwner;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.serverKeyPair = keyPair;
    }

    public void setSessionService(MinecraftSessionService sessionService) {
        this.sessionService = sessionService;
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
        try {
            this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
            this.statusResponse.setServerDescription(new TextComponentString(config.getMotd()));
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(0, 0));
            applyServerIconToResponse(this.statusResponse);
            LOGGER.info("Init minecraft auth service");
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            setSessionService(yggdrasilauthenticationservice.createMinecraftSessionService());
            this.setKeyPair(CryptManager.generateKeyPair());
            this.network.connect();
            LOGGER.info("Server load done! Type '/help'");
            while (isRunning) {
                this.network.update();
            }
        }catch (IOException e){
            LOGGER.warn("**** FAILED TO BIND TO PORT! ****");
            LOGGER.warn("The exception was: {}", e.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
        }
    }

    public boolean isServerInOnlineMode() {
        return this.getConfig().isOnlineMode();
    }

    public KeyPair getKeyPair() {
        return this.serverKeyPair;
    }

    public String getServerOwner() {
        return serverOwner;
    }

    public boolean isSinglePlayer() {
        return this.serverOwner != null;
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.sessionService;
    }

    public boolean func_190518_ac() {
        return this.field_190519_A;
    }
}

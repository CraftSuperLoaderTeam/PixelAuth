package io.pixel.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfig.class);
    Configuration configuration;
    File file;

    String ip,minecraft_version,motd;
    int port,timeout;
    boolean online_mode;

    public ServerConfig(File file){
        this.file = file;
        initConfig();
    }

    public void createConfigFile() throws IOException {
        if(!file.exists()) {
            file.createNewFile();
            try(BufferedOutputStream target = new BufferedOutputStream(new FileOutputStream(file))){
                InputStream source = Util.getResource("config.yml");
                byte[] buf = new byte[8192];
                int length;
                while ((length = source.read(buf)) > 0) {
                    target.write(buf, 0, length);
                }
            }
        }
    }

    public void initConfig(){
        try{
            createConfigFile();
            YamlConfiguration yaml = new YamlConfiguration();
            this.configuration = yaml.load(file);

            this.port = this.configuration.getInt("server.port",25566);
            this.timeout = this.configuration.getInt("server.timeout",30);
            this.ip = this.configuration.getString("server.ip","127.0.0.1");

            this.online_mode = this.configuration.getBoolean("auth.online_mode",true);
            this.minecraft_version = this.configuration.getString("auth.minecraft_version","1.12.2");
            this.motd = this.configuration.getString("motd","A Auth Server");

        }catch (Exception e){
            LOGGER.error("Cannot load server config.",e);
            LOGGER.error("Please check your config file.");
        }
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getIp() {
        return ip;
    }

    public boolean isOnlineMode() {
        return online_mode;
    }

    public String getMotd() {
        return motd;
    }

    public String getMinecraftVersion() {
        return minecraft_version;
    }
}

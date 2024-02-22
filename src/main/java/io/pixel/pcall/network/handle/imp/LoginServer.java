package io.pixel.pcall.network.handle.imp;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import io.pixel.api.CraftClient;
import io.pixel.api.PixelAPI;
import io.pixel.api.event.ClientLoginEvent;
import io.pixel.pcall.PixelCraft;
import io.pixel.pcall.apiimp.ClientImp;
import io.pixel.pcall.network.packet.login.PacketDisconnect;
import io.pixel.pcall.network.packet.login.PacketEncryptionRequest;
import io.pixel.pcall.network.NetworkServer;
import io.pixel.pcall.network.PlayerConnect;
import io.pixel.pcall.network.handle.ILoginServer;
import io.pixel.pcall.network.packet.login.PacketEncryptionResponse;
import io.pixel.pcall.network.packet.login.PacketLoginStart;
import io.pixel.schedule.NetworkTask;
import io.pixel.pcall.util.CryptManager;
import io.pixel.pcall.util.text.ITextComponent;
import io.pixel.pcall.util.text.TextComponentString;
import io.pixel.pcall.util.text.TextComponentTranslation;
import org.apache.commons.lang3.Validate;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginServer implements ILoginServer, NetworkTask {
    private static final Random RANDOM = new Random();
    private static final AtomicInteger AUTHENTICATOR_THREAD_ID = new AtomicInteger(0);
    PixelCraft server;
    PlayerConnect connect;
    private SecretKey secretKey;
    private final byte[] verifyToken = new byte[4];
    LoginState state;
    private int connectionTimer;
    private GameProfile loginGameProfile;

    public LoginServer(PixelCraft serverIn, PlayerConnect connect) {
        this.server = serverIn;
        this.connect = connect;
        this.state = LoginState.HELLO;
        RANDOM.nextBytes(this.verifyToken);
    }

    public String getConnectionInfo() {
        return this.loginGameProfile != null ? this.loginGameProfile.getId() + " (" + this.connect.getRemoteAddress() + ")" : String.valueOf((Object) this.connect.getRemoteAddress());
    }

    @Override
    public void onDisconnect(ITextComponent component) {
        NetworkServer.LOGGER.info("{} lost connection: {}", this.getConnectionInfo(), component.getUnformattedText());
    }

    @Override
    public void processLoginStart(PacketLoginStart packetIn) {
        Validate.validState(this.state == LoginServer.LoginState.HELLO, "Unexpected hello packet");
        this.loginGameProfile = packetIn.getProfile();

        if (this.server.isServerInOnlineMode() && !this.connect.isLocalChannel()) {
            this.state = LoginServer.LoginState.KEY;
            this.connect.sendPacket(new PacketEncryptionRequest("", this.server.getKeyPair().getPublic(), this.verifyToken));
        } else {
            this.state = LoginServer.LoginState.READY_TO_ACCEPT;
            onLogin();
        }
    }

    @Override
    public void processEncryptionResponse(PacketEncryptionResponse packetIn) {
        Validate.validState(this.state == LoginServer.LoginState.KEY, "Unexpected key packet");
        PrivateKey privatekey = this.server.getKeyPair().getPrivate();

        if (!Arrays.equals(this.verifyToken, packetIn.getVerifyToken(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.secretKey = packetIn.getSecretKey(privatekey);
            this.state = LoginServer.LoginState.AUTHENTICATING;
            this.connect.enableEncryption(this.secretKey);

            (new Thread("User Authenticator #" + AUTHENTICATOR_THREAD_ID.incrementAndGet()) {
                public void run() {
                    GameProfile gameprofile = LoginServer.this.loginGameProfile;
                    try {
                        String s = (new BigInteger(CryptManager.getServerIdHash("", LoginServer.this.server.getKeyPair().getPublic(), LoginServer.this.secretKey))).toString(16);
                        LoginServer.this.loginGameProfile = LoginServer.this.server.getMinecraftSessionService().hasJoinedServer(new GameProfile((UUID) null, gameprofile.getName()), s);

                        if (LoginServer.this.loginGameProfile != null) {
                            NetworkServer.LOGGER.info("UUID of player {} is {}", LoginServer.this.loginGameProfile.getName(), LoginServer.this.loginGameProfile.getId());
                            LoginServer.this.state = LoginState.READY_TO_ACCEPT;
                            onLogin();
                        } else if (LoginServer.this.server.isSinglePlayer()) {
                            NetworkServer.LOGGER.warn("Failed to verify username.");
                            LoginServer.this.loginGameProfile = LoginServer.this.getOfflineProfile(gameprofile);
                            LoginServer.this.state = LoginState.READY_TO_ACCEPT;
                        } else {
                            LoginServer.this.disconnect(new TextComponentTranslation("multiplayer.disconnect.unverified_username", new Object[0]));
                            NetworkServer.LOGGER.error("Username '{}' tried to join with an invalid session", (Object) gameprofile.getName());
                        }
                    } catch (AuthenticationUnavailableException var3) {
                        if (LoginServer.this.server.isSinglePlayer()) {
                            NetworkServer.LOGGER.warn("Authentication servers are down.");
                            LoginServer.this.loginGameProfile = LoginServer.this.getOfflineProfile(gameprofile);
                            LoginServer.this.state = LoginState.READY_TO_ACCEPT;
                        } else {
                            LoginServer.this.disconnect(new TextComponentTranslation("multiplayer.disconnect.authservers_down", new Object[0]));
                            NetworkServer.LOGGER.error("Couldn't verify username because servers are unavailable");
                        }
                    }
                }

                private InetAddress func_191235_a() {
                    SocketAddress socketaddress = LoginServer.this.connect.getRemoteAddress();
                    return LoginServer.this.server.func_190518_ac() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
                }
            }).start();
        }
    }

    public void onLogin(){
        CraftClient client = new ClientImp(this.loginGameProfile,this.connect.getRemoteAddress()){
            @Override
            public void disconnect(String message) {
                super.disconnect(message);
                LoginServer.this.disconnect(new TextComponentString(message));
            }
        };
        ClientLoginEvent event = new ClientLoginEvent(client);
        PixelAPI.callEvent(event);
        if(event.getClient().isDisconnect()) return;
        String message = "您已成功登录至AuthServer,此操作为正常踢出服务器";
        this.disconnect(new TextComponentString(message));
    }

    protected GameProfile getOfflineProfile(GameProfile original) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + original.getName()).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, original.getName());
    }

    @Override
    public void update() {
        if (this.state == LoginState.READY_TO_ACCEPT || this.state == LoginState.DELAY_ACCEPT) ;
        if (this.connectionTimer++ == 600) {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.slow_login", new Object[0]));
        }
    }

    public void disconnect(ITextComponent component) {
        try {
            NetworkServer.LOGGER.info("Disconnecting {}: {}", this.getConnectionInfo(), component.getUnformattedText());
            this.connect.sendPacket(new PacketDisconnect(component));
            this.connect.closeChannel(component);
        } catch (Exception exception) {
            NetworkServer.LOGGER.error("Error whilst disconnecting player", (Throwable) exception);
        }
    }

    enum LoginState {
        HELLO,
        KEY,
        AUTHENTICATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED;
    }
}

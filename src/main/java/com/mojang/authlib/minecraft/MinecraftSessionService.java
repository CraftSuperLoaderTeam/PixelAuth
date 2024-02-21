package com.mojang.authlib.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;

import java.util.Map;

public interface MinecraftSessionService {

    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException;


    public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException;


    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure);


    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure);
}

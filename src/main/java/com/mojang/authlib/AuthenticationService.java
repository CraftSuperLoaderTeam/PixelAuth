package com.mojang.authlib;

import com.mojang.authlib.minecraft.MinecraftSessionService;

public interface AuthenticationService {

    public UserAuthentication createUserAuthentication(Agent agent);


    public MinecraftSessionService createMinecraftSessionService();


    public GameProfileRepository createProfileRepository();
}

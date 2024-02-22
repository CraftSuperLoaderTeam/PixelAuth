package io.pixel.command;

import io.pixel.api.PixelAPI;
import io.pixel.api.command.Command;

public class CommandStop implements Command {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void executor(String[] args) {
        PixelAPI.getServerLogger().info("Command stop server.");
        PixelAPI.shutdown();
    }
}

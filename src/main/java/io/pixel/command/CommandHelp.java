package io.pixel.command;

import io.pixel.api.PixelAPI;
import io.pixel.api.command.Command;

public class CommandHelp implements Command {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void executor(String[] args) {
        PixelAPI.getServerLogger().info("""
          ** *[Server Command Helper]* **
        /help         -Print server command help info.
        /stop         -Stop server.
        """);
    }
}

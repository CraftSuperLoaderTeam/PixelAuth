package io.pixel.command;

import io.pixel.PixelCraft;
import io.pixel.api.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommandHandle {
    Thread handle;
    PixelCraft server;
    List<Command> commands;
    public CommandHandle(PixelCraft server){
        this.commands = new ArrayList<>();
        handle = new Thread("Server console handler") {
            public void run() {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                String s4;
                try {
                    while ((s4 = bufferedreader.readLine()) != null) {
                        executorCommand(s4);
                    }
                } catch (IOException e) {
                    PixelCraft.LOGGER.error("Exception handling console input", (Throwable) e);
                }
            }
        };
        this.commands.add(new CommandStop());
        this.commands.add(new CommandHelp());
        handle.start();
    }

    public void executorCommand(String rawCommand){
        rawCommand = rawCommand.trim();

        if (rawCommand.startsWith("/")) {
            rawCommand = rawCommand.substring(1);
        }

        String[] astring = rawCommand.split(" ");
        String s = astring[0];
        astring = dropFirstString(astring);

        for(Command command:commands){
            if(command.getName().equals(s)){
                command.executor(astring);
            }
        }
    }

    private static String[] dropFirstString(String[] input) {
        String[] astring = new String[input.length - 1];
        System.arraycopy(input, 1, astring, 0, input.length - 1);
        return astring;
    }
}

package io.pixel.api.command;

public interface Command {
    String getName();
    void executor(String[] args);
}

package io.pixel.api.event;

public abstract class Event {
    public String getEventName(){
        return getClass().getSimpleName();
    }
}

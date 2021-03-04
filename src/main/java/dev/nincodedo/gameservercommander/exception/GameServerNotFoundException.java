package dev.nincodedo.gameservercommander.exception;

import lombok.Getter;

@Getter
public class GameServerNotFoundException extends Throwable {

    private String name;

    public GameServerNotFoundException(String name) {
        this.name = name;
    }
}

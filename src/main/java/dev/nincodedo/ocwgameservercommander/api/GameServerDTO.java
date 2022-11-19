package dev.nincodedo.ocwgameservercommander.api;

import lombok.Data;

@Data
public class GameServerDTO {
    private String name;
    private String description;
    private String game;
    private String connectionInfo;
    private boolean online;
}

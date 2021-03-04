package dev.nincodedo.gameservercommander.api;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameServerResult {
    private String id;
    private String game;
    private String status;
}

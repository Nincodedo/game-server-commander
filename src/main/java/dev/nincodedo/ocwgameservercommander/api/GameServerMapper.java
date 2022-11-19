package dev.nincodedo.ocwgameservercommander.api;

import dev.nincodedo.ocwgameservercommander.GameServer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameServerMapper {
    GameServerDTO mapToDto(GameServer gameServer);
}

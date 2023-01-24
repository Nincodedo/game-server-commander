package dev.nincodedo.ocwgameservercommander.api;

import dev.nincodedo.ocwgameservercommander.gameserver.GameServer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameServerMapper {
    GameServerDTO mapToDto(GameServer gameServer);
}

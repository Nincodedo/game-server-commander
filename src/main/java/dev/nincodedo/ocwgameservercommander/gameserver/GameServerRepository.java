package dev.nincodedo.ocwgameservercommander.gameserver;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GameServerRepository extends CrudRepository<GameServer, Long> {
    @NotNull
    List<GameServer> findAll();

    Optional<GameServer> findGameServerByName(String name);

}

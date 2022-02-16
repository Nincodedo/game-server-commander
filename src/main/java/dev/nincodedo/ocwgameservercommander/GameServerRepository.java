package dev.nincodedo.ocwgameservercommander;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameServerRepository extends CrudRepository<GameServer, Long> {
    @NotNull List<GameServer> findAll();

    GameServer findGameServerByName(String name);

    GameServer findGameServerByGame(String game);
}

package dev.nincodedo.ocwgameservercommander;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameServerService {

    private final GameServerRepository gameServerRepository;

    public GameServerService(GameServerRepository gameServerRepository) {
        this.gameServerRepository = gameServerRepository;
    }

    public Optional<GameServer> findGameServerByGameName(String gameName) {
        return Optional.of(gameServerRepository.findGameServerByName(gameName));
    }

    public void save(GameServer gameServer) {
        gameServerRepository.save(gameServer);
    }
}

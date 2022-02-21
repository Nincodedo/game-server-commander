package dev.nincodedo.ocwgameservercommander;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameServerService {

    private final GameServerRepository gameServerRepository;
    @Getter
    @Setter
    private boolean recentChangesMade = false;

    public GameServerService(GameServerRepository gameServerRepository) {
        this.gameServerRepository = gameServerRepository;
    }

    public void save(GameServer gameServer) {
        recentChangesMade = true;
        gameServerRepository.save(gameServer);
    }

    public int getOnlineGameServerCount() {
        return (int) gameServerRepository.findAll().stream().filter(GameServer::isOnline).count();
    }

    public Optional<GameServer> findGameServerByName(String serverName) {
        return gameServerRepository.findGameServerByName(serverName);
    }

    public List<GameServer> findAll() {
        return gameServerRepository.findAll();
    }

    public Optional<GameServer> findById(Long id) {
        return gameServerRepository.findById(id);
    }

    public void acknowledgeRecentChangesMade() {
        recentChangesMade = false;
    }
}

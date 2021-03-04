package dev.nincodedo.gameservercommander.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import dev.nincodedo.gameservercommander.api.GameServerResult;
import dev.nincodedo.gameservercommander.common.Constants;
import dev.nincodedo.gameservercommander.exception.GameServerNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Controller
@RequestMapping("/game-server")
public class GameServerController {

    private DockerClient dockerClient;

    public GameServerController(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @GetMapping("/game")
    @ResponseBody
    public List<GameServerResult> getGameServers() {
        return getGameServerStream().collect(Collectors.toList());
    }

    @GetMapping("/game/{gameName}")
    @ResponseBody
    public List<GameServerResult> getGameServersByGameName(
            @PathVariable String gameName) throws GameServerNotFoundException {
        List<GameServerResult> gameServers = getGameServerStream()
                .filter(gameServerResult -> gameServerResult.getGame().toLowerCase().contains(gameName.toLowerCase()))
                .collect(Collectors.toList());
        if (!gameServers.isEmpty()) {
            return gameServers;
        }
        throw new GameServerNotFoundException(gameName);
    }

    private Stream<GameServerResult> getGameServerStream() {
        return dockerClient.listContainersCmd()
                .exec()
                .stream()
                .filter(container -> container.getLabels().containsKey(Constants.DOCKER_LABEL_GAME_SERVER))
                .map(this::getGameServerResultFromContainer);
    }

    private GameServerResult getGameServerResultFromContainer(Container container) {
        return GameServerResult.builder()
                .id(container.getId())
                .game(container.getLabels().get(Constants.DOCKER_LABEL_GAME_SERVER))
                .status(container.getStatus())
                .build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ExceptionHandler(GameServerNotFoundException.class)
    public void handleGameServerNotFound(GameServerNotFoundException e) {
        log.error("Could not find game server named {}", e.getName());
    }
}

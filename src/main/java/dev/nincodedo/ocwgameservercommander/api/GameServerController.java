package dev.nincodedo.ocwgameservercommander.api;

import dev.nincodedo.ocwgameservercommander.gameserver.GameServerService;
import dev.nincodedo.ocwgameservercommander.common.api.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;

@RestController
@RequestMapping(value = "/v1/game-servers", produces = MediaType.APPLICATION_JSON_VALUE)
public class GameServerController {

    private final GameServerService gameServerService;
    private final GameServerMapper gameServerMapper;
    private final Comparator<GameServerDTO> comparator = Comparator.comparing(GameServerDTO::getGame);

    public GameServerController(GameServerService gameServerService, GameServerMapper gameServerMapper) {
        this.gameServerService = gameServerService;
        this.gameServerMapper = gameServerMapper;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<GameServerDTO>> allServers() {
        var list = gameServerService.findAll()
                .stream()
                .map(gameServerMapper::mapToDto)
                .sorted(comparator)
                .toList();
        return new ResponseEntity<>(new BaseResponse<>(list), HttpStatus.OK);
    }

    @GetMapping(value = "/online")
    public ResponseEntity<BaseResponse<GameServerDTO>> allOnlineServers() {
        var list = gameServerService.findAll()
                .stream()
                .map(gameServerMapper::mapToDto)
                .filter(GameServerDTO::isOnline)
                .sorted(comparator)
                .toList();
        return new ResponseEntity<>(new BaseResponse<>(list), HttpStatus.OK);
    }


}

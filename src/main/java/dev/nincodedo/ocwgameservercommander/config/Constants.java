package dev.nincodedo.ocwgameservercommander.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public class Constants {
    @Getter
    @Value("${nincodedoUserId}")
    private String nincodedoUserId;
}

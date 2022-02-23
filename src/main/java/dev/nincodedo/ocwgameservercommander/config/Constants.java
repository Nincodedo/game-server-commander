package dev.nincodedo.ocwgameservercommander.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

    public Constants(@Value("${nincodedoUserId}") String nincodedoUserId){
        this.nincodedoUserId = nincodedoUserId;
    }

    @Getter
    private String nincodedoUserId;
}

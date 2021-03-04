package dev.nincodedo.gameservercommander.common;

import lombok.Data;

@Data
public class ListResponse<T> {
    private T response;
    private int size;
}

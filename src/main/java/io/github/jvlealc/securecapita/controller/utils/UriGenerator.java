package io.github.jvlealc.securecapita.controller.utils;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class UriGenerator {

    private UriGenerator() {
    }

    public static URI generate(Object id) {
        return ServletUriComponentsBuilder.
                fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}

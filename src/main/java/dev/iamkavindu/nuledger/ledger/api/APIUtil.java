package dev.iamkavindu.nuledger.ledger.api;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class APIUtil {
    private APIUtil() {}

    public static URI locationHeader(String slug, Object identifier) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path(slug)
                .buildAndExpand(identifier)
                .toUri();
    }
}

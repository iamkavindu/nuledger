package dev.iamkavindu.nuledger.ledger.api.problem;

import java.net.URI;

public final class ProblemTypes {

    public static final URI BASE = URI.create("https://nuledger.dev/problems/");

    public static URI of(String slug) {
        return BASE.resolve(slug);
    }

    private ProblemTypes() {}
}

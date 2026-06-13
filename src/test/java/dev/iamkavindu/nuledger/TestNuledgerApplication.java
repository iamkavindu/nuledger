package dev.iamkavindu.nuledger;

import org.springframework.boot.SpringApplication;

public class TestNuledgerApplication {

    public static void main(String[] args) {
        SpringApplication.from(NuledgerApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}

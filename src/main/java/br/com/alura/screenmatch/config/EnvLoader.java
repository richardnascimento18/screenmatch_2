package br.com.alura.screenmatch.config;
import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue()) // makes it available as env or system property
        );
    }
}

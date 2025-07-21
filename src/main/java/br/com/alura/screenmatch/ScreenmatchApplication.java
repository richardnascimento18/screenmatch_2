package br.com.alura.screenmatch;

import br.com.alura.screenmatch.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication {

	public static void main(String[] args) {
		EnvLoader.load();
		SpringApplication.run(ScreenmatchApplication.class, args);
	}
}

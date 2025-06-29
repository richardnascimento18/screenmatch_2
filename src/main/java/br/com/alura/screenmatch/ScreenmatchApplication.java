package br.com.alura.screenmatch;

import br.com.alura.screenmatch.config.EnvLoader;
import br.com.alura.screenmatch.principal.Principal;
import br.com.alura.screenmatch.repository.SerieRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {
	@Autowired
	private SerieRepository serieRepository;

	public static void main(String[] args) {
		EnvLoader.load();
		Dotenv dotenv = Dotenv.configure().load();
		System.out.println("Loaded DB_PASS: " + dotenv.get("DB_PASS"));
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(serieRepository);
		principal.exibeMenu();
	}
}

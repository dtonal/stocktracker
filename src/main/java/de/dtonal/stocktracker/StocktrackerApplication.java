package de.dtonal.stocktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StocktrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StocktrackerApplication.class, args);
	}

}

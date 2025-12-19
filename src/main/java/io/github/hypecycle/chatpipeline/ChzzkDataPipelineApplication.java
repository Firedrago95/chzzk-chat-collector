package io.github.hypecycle.chatpipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChzzkDataPipelineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChzzkDataPipelineApplication.class, args);
	}

}

package pe.edu.upeu.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CasaReservaMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasaReservaMsApplication.class, args);
	}

}

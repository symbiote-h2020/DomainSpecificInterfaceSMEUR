package eu.h2020.symbiote.smeur.dsi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;

@EnableDiscoveryClient    
@EnableAutoConfiguration
@SpringBootApplication
public class DomainSpecificInterfaceSMEUR {

	public static void main(String[] args) {
		SpringApplication.run(DomainSpecificInterfaceSMEUR.class, args);
    }
	
	@Component
    public static class CLR implements CommandLineRunner {

        private final RabbitManager rabbitManager;

        @Autowired
        public CLR(RabbitManager rabbitManager) {
            this.rabbitManager = rabbitManager;
        }

        @Override
        public void run(String... args) throws Exception {

            //message retrieval - start rabbit exchange and consumers
            this.rabbitManager.init();
        }
    }
}

package eu.h2020.symbiote.smeur.dsi.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rabbitmq.client.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Bean used to manage internal communication using RabbitMQ. It is responsible
 * for declaring exchanges and using routing keys from centralized config
 * server.
 * <p>
 * Created by Petar Krivic (13.7.2017)
 */
@Component ("RabbitManagerDSI")
public class RabbitManager {

	private static Log log = LogFactory.getLog(RabbitManager.class);

	@Value("${rabbit.host}")
	private String rabbitHost;
	@Value("${rabbit.username}")
	private String rabbitUsername;
	@Value("${rabbit.password}")
	private String rabbitPassword;

	private Connection connection;
	private RabbitTemplate rabbitTemplate;

	public RabbitManager(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * Initiates connection with Rabbit server using parameters from
	 * bootstrapProperties
	 *
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public Connection getConnection() throws IOException, TimeoutException {
		if (connection == null) {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(this.rabbitHost);
			factory.setUsername(this.rabbitUsername);
			factory.setPassword(this.rabbitPassword);
			this.connection = factory.newConnection();
		}
		return this.connection;
	}

	/**
	 * Method creates channel and declares Rabbit exchanges. It triggers start
	 * of all consumers used in Registry communication.
	 */
	public void init() {

		log.info("RabbitManager of DomainSpecificInterface is being initialized!");

		try {
			getConnection();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cleanup method for rabbit - set on pre destroy
	 */
	@PreDestroy
	public void cleanup() {

		log.info("Rabbit cleaned!");
		try {
			if (this.connection != null && this.connection.isOpen()) {
				this.connection.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAsyncMessage(String exchange, String routingKey, Object obj) {
		
		log.info("Sending async message");
		rabbitTemplate.convertAndSend(exchange, routingKey, obj);
	}
	
	public Object sendRpcMessage(String exchange, String routingKey, Object obj) {
		log.info("Sending RPC message");

		String correlationId = UUID.randomUUID().toString();
		rabbitTemplate.setReplyTimeout(60000);
		Object receivedObj = rabbitTemplate.convertSendAndReceive(exchange, routingKey, obj,
				new CorrelationData(correlationId));
		if (receivedObj == null) {
			log.info("Received null or Timeout!");
			return null;
		}

		log.info("RPC Response received obj: " + receivedObj);

		return receivedObj;
	}

	public Object sendRpcMessageJSON(String exchange, String routingKey, Object obj) {
		log.info("Sending RPC message");

		String correlationId = UUID.randomUUID().toString();
		rabbitTemplate.setReplyTimeout(60000);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter(mapper);
		rabbitTemplate.setMessageConverter(messageConverter);
		Object receivedObj = rabbitTemplate.convertSendAndReceive(exchange, routingKey, obj,
				new CorrelationData(correlationId));
		if (receivedObj == null) {
			log.info("Received null or Timeout!");
			return null;
		}

		log.info("RPC Response received obj: " + receivedObj);
		rabbitTemplate.setMessageConverter(new SimpleMessageConverter()); // set theSimpleMessageConverter again
		return receivedObj;
	}

}

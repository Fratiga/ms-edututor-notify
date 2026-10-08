package cl.duoc.edututornotify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Bean
	MessageConverter jsonMessageConverter() {
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		return new Jackson2JsonMessageConverter(mapper);
	}

	// ACK manual: solo confirmamos el mensaje después de "procesar" el
	// efecto (envío simulado). Si el procesamiento falla de forma
	// permanente, se hace NACK sin requeue y el mensaje cae a su DLQ.
	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			MessageConverter jsonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jsonMessageConverter);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		// Una sola entrega pendiente por consumidor: reparto justo y un fallo no
		// deja varios mensajes sin confirmar. Escala hasta 3 consumidores por cola.
		factory.setPrefetchCount(1);
		factory.setConcurrentConsumers(1);
		factory.setMaxConcurrentConsumers(3);
		// Si las colas todavía no existen (topología sin declarar), reintenta en
		// vez de tumbar el arranque; este factory no hereda spring.rabbitmq.listener.*.
		factory.setMissingQueuesFatal(false);
		return factory;
	}
}

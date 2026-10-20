package cl.duoc.edututornotify.email;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.duoc.edututornotify.ComandoEnvelope;
import cl.duoc.edututornotify.ComandoProcesador;

// Dominio: avisos por email/push al estudiante.
@Component
public class EmailListener {

	private final ComandoProcesador procesador;

	public EmailListener(ComandoProcesador procesador) {
		this.procesador = procesador;
	}

	@RabbitListener(queues = "${edututor.rabbit.email.queue}")
	public void onEmail(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesador.procesar(evento, channel, tag, "email/push al estudiante");
	}
}

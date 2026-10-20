package cl.duoc.edututornotify.ticket;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.duoc.edututornotify.ComandoEnvelope;
import cl.duoc.edututornotify.ComandoProcesador;

// Dominio: ticket de sesión para el tutor.
@Component
public class TicketListener {

	private final ComandoProcesador procesador;

	public TicketListener(ComandoProcesador procesador) {
		this.procesador = procesador;
	}

	@RabbitListener(queues = "${edututor.rabbit.session.queue}")
	public void onSessionTicket(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesador.procesar(evento, channel, tag, "ticket de sesión al tutor");
	}
}

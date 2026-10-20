package cl.duoc.edututornotify.certificado;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.duoc.edututornotify.ComandoEnvelope;
import cl.duoc.edututornotify.ComandoProcesador;

// Dominio: generación de la constancia de la sesión.
@Component
public class CertificadoListener {

	private final ComandoProcesador procesador;

	public CertificadoListener(ComandoProcesador procesador) {
		this.procesador = procesador;
	}

	@RabbitListener(queues = "${edututor.rabbit.certificate.queue}")
	public void onCertificate(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesador.procesar(evento, channel, tag, "generación de certificado/constancia");
	}
}

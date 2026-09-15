package cl.duoc.edututornotify;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class NotifyListener {

	private static final Logger log = LoggerFactory.getLogger(NotifyListener.class);

	private final EventosProcesadosStore eventosProcesados;

	public NotifyListener(EventosProcesadosStore eventosProcesados) {
		this.eventosProcesados = eventosProcesados;
	}

	@RabbitListener(queues = "q.cmd.email")
	public void onEmail(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesar(evento, channel, tag, "email/push al estudiante");
	}

	@RabbitListener(queues = "q.cmd.session")
	public void onSessionTicket(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesar(evento, channel, tag, "ticket de sesión al tutor");
	}

	@RabbitListener(queues = "q.cmd.certificate")
	public void onCertificate(@Payload ComandoEnvelope evento, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		procesar(evento, channel, tag, "generación de certificado/constancia");
	}

	private void procesar(ComandoEnvelope evento, Channel channel, long tag, String descripcion) throws IOException {
		if (!eventosProcesados.marcarSiEsNuevo(evento.eventId())) {
			// Reentrega de un evento ya procesado (semántica at-least-once):
			// confirmamos sin repetir el efecto.
			log.info("Evento {} ya procesado antes, se ignora (idempotencia)", evento.eventId());
			channel.basicAck(tag, false);
			return;
		}

		try {
			if (evento.payload() == null || evento.payload().isEmpty()) {
				// Fallo de negocio (payload inválido), no transitorio: a la DLQ.
				throw new IllegalArgumentException("Payload vacío, no se puede completar: " + descripcion);
			}

			// Aquí iría la integración real (SMTP, push, generador de PDF).
			// No hay proveedor real disponible en este entorno, así que se
			// deja registrado el efecto simulado.
			log.info("Procesado [{}] eventId={} correlationId={} payload={}",
				descripcion, evento.eventId(), evento.correlationId(), evento.payload());

			channel.basicAck(tag, false);
		} catch (IllegalArgumentException e) {
			log.warn("Fallo de negocio en {}: {} — enviando a DLQ", descripcion, e.getMessage());
			channel.basicNack(tag, false, false); // requeue=false -> DLQ
		} catch (Exception e) {
			log.error("Fallo transitorio en {}: {} — se reintenta", descripcion, e.getMessage());
			channel.basicNack(tag, false, true); // requeue=true -> reintento
		}
	}
}

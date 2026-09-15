package cl.duoc.edututornotify;

import java.time.Instant;
import java.util.Map;

// Envelope común de mensajes acordado en el documento de arquitectura:
// type, eventId, timestamp, traceId, correlationId, payload.
public record ComandoEnvelope(
	String type,
	String eventId,
	Instant timestamp,
	String traceId,
	String correlationId,
	Map<String, Object> payload
) {
}

package cl.duoc.edututornotify;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

// Sin base de datos (según el caso), así que la deduplicación por eventId es
// en memoria — se pierde si el proceso se reinicia, pero cubre el escenario
// típico de reentrega inmediata por at-least-once que describe el documento.
// Tamaño acotado para no crecer sin límite.
@Component
public class EventosProcesadosStore {

	private static final int CAPACIDAD_MAXIMA = 10_000;

	private final Map<String, Boolean> vistos = Collections.synchronizedMap(
		new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
				return size() > CAPACIDAD_MAXIMA;
			}
		});

	// true si es la primera vez que se ve este eventId (y lo marca como visto).
	public boolean marcarSiEsNuevo(String eventId) {
		return vistos.putIfAbsent(eventId, Boolean.TRUE) == null;
	}
}

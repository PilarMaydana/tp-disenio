package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Selecciona una estrategia de exportación sin acoplar el controlador al formato. */
@Service
public class ExportadorInformeOrdenMerito {
    private final Map<FormatoInformeOrdenMerito, GeneradorInformeOrdenMerito> generadores;

    public ExportadorInformeOrdenMerito(List<GeneradorInformeOrdenMerito> estrategias) {
        this.generadores = new EnumMap<>(FormatoInformeOrdenMerito.class);
        estrategias.forEach(estrategia -> generadores.put(estrategia.formato(), estrategia));
    }

    public ArchivoInformeOrdenMerito exportar(InformeOrdenMeritoDTO informe, FormatoInformeOrdenMerito formato)
            throws IOException {
        GeneradorInformeOrdenMerito generador = generadores.get(formato);
        if (generador == null) {
            throw new IllegalArgumentException("No existe un generador para el formato " + formato);
        }
        return new ArchivoInformeOrdenMerito(generador.generar(informe), formato.contentType(), formato.extension());
    }
}

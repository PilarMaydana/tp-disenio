package ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.exportacion;

import ar.edu.utn.frsf.g9.capitalhumano.features.ordenmerito.dto.InformeOrdenMeritoDTO;

import java.io.IOException;

/** Estrategia de generación para un formato descargable. */
public interface GeneradorInformeOrdenMerito {
    FormatoInformeOrdenMerito formato();
    byte[] generar(InformeOrdenMeritoDTO informe) throws IOException;
}

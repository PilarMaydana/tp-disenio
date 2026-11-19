-- Datos iniciales / semillas (H2 en modo PostgreSQL)

INSERT INTO competencia (codigo, nombre, descripcion) VALUES
    ('COM-01', 'COMUNICACIÓN', 'CAPACIDAD PARA TRANSMITIR IDEAS E INFORMACIÓN DE MANERA CLARA Y EFECTIVA.'),
    ('LID-01', 'LIDERAZGO', 'CAPACIDAD PARA GUIAR, MOTIVAR Y DIRIGIR EQUIPOS DE TRABAJO HACIA EL LOGRO DE OBJETIVOS.'),
    ('TRAB-01', 'TRABAJO EN EQUIPO', 'CAPACIDAD PARA COLABORAR DE MANERA EFICAZ CON OTRAS PERSONAS PARA EL LOGRO DE METAS COMUNES.'),
    ('RESOL-01', 'RESOLUCIÓN DE PROBLEMAS', 'CAPACIDAD PARA IDENTIFICAR PROBLEMAS COMPLEJOS Y EVALUAR OPCIONES DE SOLUCIÓN.'),
    ('TEC-01', 'COMPETENCIAS TÉCNICAS', 'CAPACIDAD PARA APLICAR CONOCIMIENTOS TÉCNICOS EN EL DESARROLLO DE SOFTWARE.');

INSERT INTO factor (codigo, nombre, descripcion, nro_orden, competencia_id, eliminado) VALUES
    ('FAC-01', 'COMUNICACIÓN ORAL', 'CAPACIDAD PARA TRANSMITIR INFORMACIÓN DE MANERA VERBAL CLARA.', 1, 1, FALSE),
    ('FAC-02', 'COMUNICACIÓN ESCRITA', 'CAPACIDAD PARA REDACTAR DOCUMENTOS DE MANERA ESTRUCTURADA.', 2, 1, FALSE),
    ('FAC-03', 'DESARROLLO DE SOFTWARE', 'CAPACIDAD PARA DISEÑAR Y PROGRAMAR SOLUCIONES DE SOFTWARE.', 1, 5, FALSE);

INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('DEV-01', 'DESARROLLADOR SENIOR', 'DESARROLLO Y ARQUITECTURA DE SOFTWARE.', 'TECH SOLUTIONS', FALSE),
    ('QA-01', 'ANALISTA QA', 'CONTROL Y ASEGURAMIENTO DE CALIDAD DE SOFTWARE.', 'TECH SOLUTIONS', FALSE);

INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    (1, 1, 8.0),
    (1, 3, 9.0),
    (2, 1, 7.5),
    (2, 4, 8.5);

INSERT INTO pregunta (nombre, enunciado, descripcion, factor_id, eliminado) VALUES
    ('PREGUNTA POO', '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?', 'CONCEPTOS BÁSICOS', 3, FALSE),
    ('PATRONES DE DISEÑO', '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?', 'PREGUNTA DE ARQUITECTURA', 3, FALSE);

INSERT INTO pregunta_opcion_respuesta (pregunta_id, nombre, descripcion, nro_orden, ponderacion) VALUES
    (1, 'OPCION A', 'UN PARADIGMA BASADO EN OBJETOS', 1, 1.0),
    (1, 'OPCION B', 'UN LENGUAJE DE PROGRAMACIÓN', 2, 0.0),
    (2, 'INSTANCIA ÚNICA', 'GARANTIZA UNA ÚNICA INSTANCIA', 1, 1.0),
    (2, 'MÚLTIPLES INSTANCIAS', 'CREA MUCHAS INSTANCIAS', 2, 0.0);

INSERT INTO opcion_respuesta (nombre, descripcion) VALUES
    ('FRECUENCIA', 'ESCALA DE FRECUENCIA DE COMPORTAMIENTO OBSERVADO'),
    ('ACUERDO', 'ESCALA DE GRADO DE ACUERDO CON LA AFIRMACIÓN'),
    ('FRECUENCIA SEMANAL', 'ESCALA DE FRECUENCIA DE EJECUCIÓN SEMANAL DE TAREAS'),
    ('SATISFACCIÓN', 'ESCALA DE NIVEL DE SATISFACCIÓN GENERAL');

INSERT INTO item_opcion_respuesta (opcion_respuesta_id, orden, descripcion) VALUES
    (1, 1, 'NUNCA'),
    (1, 2, 'A VECES'),
    (1, 3, 'SIEMPRE'),
    (2, 1, 'EN DESACUERDO'),
    (2, 2, 'NEUTRAL'),
    (2, 3, 'DE ACUERDO'),
    (3, 1, 'NINGUNA VEZ'),
    (3, 2, 'UNA A DOS VECES'),
    (3, 3, 'TRES O MÁS VECES'),
    (4, 1, 'INSATISFECHO'),
    (4, 2, 'NEUTRO'),
    (4, 3, 'SATISFECHO'),
    (4, 4, 'MUY SATISFECHO');

INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    (1, 'DNI', '12345678', 'PÉREZ', 'JUAN', 'juan.perez@example.com', FALSE),
    (2, 'DNI', '87654321', 'GÓMEZ', 'MARÍA', 'maria.gomez@example.com', FALSE),
    (3, 'DNI', '11223344', 'LÓPEZ', 'CARLOS', 'carlos.lopez@example.com', FALSE);

-- CU 025: evaluación que agrupa los cuestionarios de ejemplo
INSERT INTO evaluacion (puesto_id, fecha_creacion) VALUES
    (1, CURRENT_TIMESTAMP);

INSERT INTO cuestionario (candidato_id, evaluacion_id, puesto_id, estado, clave_acceso, fecha_creacion, eliminado) VALUES
    (1, 1, 1, 'ACTIVO', 'CLAVE123', CURRENT_TIMESTAMP, FALSE),
    (2, 1, 1, 'EN_PROCESO', 'TEST4567', CURRENT_TIMESTAMP, FALSE),
    (3, 1, 1, 'COMPLETO', 'COMP1234', CURRENT_TIMESTAMP, FALSE);


-- ---------------------------------------------------------------------------
-- CU 025 - Evaluar candidatos: datos adicionales para probar los flujos
-- ---------------------------------------------------------------------------
-- Preguntas para el factor COMUNICACIÓN ORAL (FAC-01): con 2 preguntas la
-- competencia COMUNICACIÓN pasa a ser evaluable.
INSERT INTO pregunta (nombre, enunciado, descripcion, factor_id, eliminado) VALUES
    ('EXPOSICIÓN EN REUNIONES', '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?', 'COMUNICACIÓN VERBAL', 1, FALSE),
    ('ESCUCHA ACTIVA', '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?', 'COMUNICACIÓN VERBAL', 1, FALSE);

INSERT INTO pregunta_opcion_respuesta (pregunta_id, nombre, descripcion, nro_orden, ponderacion) VALUES
    (3, 'NUNCA', 'NUNCA', 1, 0.0),
    (3, 'A VECES', 'A VECES', 2, 4.0),
    (3, 'SIEMPRE', 'SIEMPRE', 3, 6.0),
    (4, 'NUNCA', 'NUNCA', 1, 0.0),
    (4, 'A VECES', 'A VECES', 2, 4.0),
    (4, 'SIEMPRE', 'SIEMPRE', 3, 6.0);

-- Puesto evaluable: todas sus competencias tienen al menos un factor con 2 preguntas.
-- (DEV-01 y QA-01 quedan como ejemplos de puestos NO evaluables - flujo 9.A)
INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('BACK-01', 'DESARROLLADOR BACKEND JUNIOR', 'DESARROLLO DE SERVICIOS Y APIS.', 'TECH SOLUTIONS', FALSE);

INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    (3, 5, 7.0),
    (3, 1, 6.0);

-- Candidatos sin cuestionarios (disponibles para evaluar)
INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    (4, 'DNI', '40111222', 'ROMERO', 'AGUSTÍN', 'agustin.romero@example.com', FALSE),
    (5, 'DNI', '41222333', 'DÍAZ', 'CAMILA', 'camila.diaz@example.com', FALSE),
    (6, 'DNI', '38333444', 'BENÍTEZ', 'MARTÍN', 'martin.benitez@example.com', FALSE),
    (7, 'PP', 'AAB123456', 'FERREYRA', 'SOFÍA', 'sofia.ferreyra@example.com', FALSE),
    (8, 'DNI', '42444555', 'ROMERO', 'LUCAS', 'lucas.romero@example.com', FALSE);

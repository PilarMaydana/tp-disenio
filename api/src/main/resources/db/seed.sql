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
    ('FAC-03', 'DESARROLLO DE SOFTWARE', 'CAPACIDAD PARA DISEÑAR Y PROGRAMAR SOLUCIONES DE SOFTWARE.', 1, 5, FALSE),
    ('FAC-04', 'COLABORACIÓN', 'CAPACIDAD PARA COLABORAR CON EL EQUIPO DE TRABAJO.', 1, 3, FALSE);

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

INSERT INTO pregunta (nombre, enunciado, descripcion, factor_id, eliminado) VALUES
    ('COMUNICACIÓN ORAL', '¿CÓMO PRESENTARÍAS UNA IDEA TÉCNICA A TU EQUIPO?', 'PREGUNTA DE MUESTRA CU028', 1, FALSE),
    ('COMUNICACIÓN ESCRITA', '¿CÓMO DOCUMENTARÍAS UNA DECISIÓN DEL EQUIPO?', 'PREGUNTA DE MUESTRA CU028', 2, FALSE),
    ('COLABORACIÓN 1', '¿CÓMO ACTUÁS CUANDO UN COMPAÑERO NECESITA AYUDA?', 'PREGUNTA DE MUESTRA CU028', 4, FALSE),
    ('COLABORACIÓN 2', '¿CÓMO RESOLVÉS UN DESACUERDO EN EL EQUIPO?', 'PREGUNTA DE MUESTRA CU028', 4, FALSE);

INSERT INTO pregunta_opcion_respuesta (pregunta_id, nombre, descripcion, nro_orden, ponderacion) VALUES
    (1, 'OPCION A', 'UN PARADIGMA BASADO EN OBJETOS', 1, 7.0),
    (1, 'OPCION B', 'UN LENGUAJE DE PROGRAMACIÓN', 2, 3.0),
    (2, 'INSTANCIA ÚNICA', 'GARANTIZA UNA ÚNICA INSTANCIA', 1, 7.0),
    (2, 'MÚLTIPLES INSTANCIAS', 'CREA MUCHAS INSTANCIAS', 2, 3.0),
    (3, 'CLARA', 'EXPLICO LA IDEA CON CLARIDAD Y ESCUCHO PREGUNTAS', 1, 5.0),
    (3, 'PARCIAL', 'PRESENTO LA IDEA SIN ADAPTARLA AL EQUIPO', 2, 4.0),
    (3, 'INSUFICIENTE', 'EVITO COMPARTIR LA IDEA', 3, 1.0),
    (4, 'CLARA', 'REGISTRO CONTEXTO, DECISIÓN Y MOTIVOS', 1, 5.0),
    (4, 'PARCIAL', 'ANOTO SOLO LA DECISIÓN', 2, 4.0),
    (4, 'INSUFICIENTE', 'NO DOCUMENTO EL CAMBIO', 3, 1.0),
    (5, 'COLABORATIVA', 'OFREZCO AYUDA Y COORDINO CON LA PERSONA', 1, 5.0),
    (5, 'PARCIAL', 'AYUDO SOLO SI ME LO PIDEN', 2, 4.0),
    (5, 'INSUFICIENTE', 'CONTINÚO CON MI TAREA SIN RESPONDER', 3, 1.0),
    (6, 'CONSTRUCTIVA', 'ESCUCHO LAS POSTURAS Y BUSCO ACUERDOS', 1, 5.0),
    (6, 'PARCIAL', 'ACEPTO LA DECISIÓN SIN CONVERSARLA', 2, 4.0),
    (6, 'INSUFICIENTE', 'IMPONGO MI POSTURA', 3, 1.0);

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
    (3, 'DNI', '11223344', 'LÓPEZ', 'CARLOS', 'carlos.lopez@example.com', FALSE),
    (4, 'DNI', '20334455', 'FERNÁNDEZ', 'LUCÍA', 'lucia.fernandez@example.com', FALSE),
    (5, 'DNI', '30445566', 'RAMÍREZ', 'JULIO', 'julio.ramirez@example.com', FALSE);

INSERT INTO cuestionario (candidato_id, puesto_id, estado, clave_acceso, fecha_creacion, fecha_inicio, fecha_finalizacion, eliminado) VALUES
    (1, 1, 'ACTIVO', 'CLAVE123', CURRENT_TIMESTAMP, NULL, NULL, FALSE),
    (2, 1, 'EN_PROCESO', 'TEST4567', TIMESTAMP '2026-11-10 08:55:00', TIMESTAMP '2026-11-10 09:00:00', NULL, FALSE),
    (3, 1, 'COMPLETO', 'COMP1234', TIMESTAMP '2026-11-10 09:55:00', TIMESTAMP '2026-11-10 10:00:00', TIMESTAMP '2026-11-10 10:45:00', FALSE),
    (4, 1, 'COMPLETO', 'LUCIA123', TIMESTAMP '2026-11-11 08:55:00', TIMESTAMP '2026-11-11 09:00:00', TIMESTAMP '2026-11-11 09:50:00', FALSE),
    (5, 1, 'COMPLETO', 'JULIO123', TIMESTAMP '2026-11-12 08:55:00', TIMESTAMP '2026-11-12 09:00:00', TIMESTAMP '2026-11-12 10:10:00', FALSE);

-- Resultados de muestra: competencias 1 y 3 tienen mínimos 8 y 9 para DEV-01.
INSERT INTO puntaje_competencia_cuestionario (cuestionario_id, competencia_id, puntaje) VALUES
    (3, 1, 9.0), (3, 3, 10.0),
    (4, 1, 8.0), (4, 3, 9.0),
    (5, 1, 10.0), (5, 3, 8.0);

-- Respuestas de muestra consistentes con los puntajes anteriores.
INSERT INTO respuesta_cuestionario (cuestionario_id, pregunta_id, opcion_respuesta_id) VALUES
    (3, 3, 5), (3, 4, 9), (3, 5, 11), (3, 6, 14),
    (4, 3, 6), (4, 4, 9), (4, 5, 11), (4, 6, 15),
    (5, 3, 5), (5, 4, 8), (5, 5, 12), (5, 6, 15);
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
    (3, 'A VECES', 'A VECES', 2, 3.0),
    (3, 'SIEMPRE', 'SIEMPRE', 3, 7.0),
    (4, 'NUNCA', 'NUNCA', 1, 0.0),
    (4, 'A VECES', 'A VECES', 2, 3.0),
    (4, 'SIEMPRE', 'SIEMPRE', 3, 7.0);

-- Puesto evaluable: todas sus competencias tienen al menos un factor con 2 preguntas.
-- (DEV-01 y QA-01 quedan como ejemplos de puestos NO evaluables - flujo 9.A)
INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('BACK-01', 'DESARROLLADOR BACKEND JUNIOR', 'DESARROLLO DE SERVICIOS Y APIS.', 'TECH SOLUTIONS', FALSE);

INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    (3, 5, 5.0),
    (3, 1, 4.0);

-- Candidatos sin cuestionarios (disponibles para evaluar)
INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    (19, 'DNI', '40111222', 'ROMERO', 'AGUSTÍN', 'agustin.romero@example.com', FALSE),
    (20, 'DNI', '41222333', 'DÍAZ', 'CAMILA', 'camila.diaz@example.com', FALSE),
    (21, 'DNI', '38333444', 'BENÍTEZ', 'MARTÍN', 'martin.benitez@example.com', FALSE),
    (22, 'PP', 'AAB123456', 'FERREYRA', 'SOFÍA', 'sofia.ferreyra@example.com', FALSE),
    (23, 'DNI', '42444555', 'ROMERO', 'LUCAS', 'lucas.romero@example.com', FALSE);

-- ---------------------------------------------------------------------------
-- CU 026 - Completar cuestionario / datos para CU 027 y CU 028
-- ---------------------------------------------------------------------------
-- Parámetros: 60 min para completarlo, 168 h (7 días) en estado ACTIVO,
-- 5 accesos como máximo y 2 preguntas por bloque.
INSERT INTO configuracion (instrucciones, tiempo_maximo_minutos, tiempo_maximo_activo_horas, cantidad_maxima_accesos, cantidad_preguntas_por_bloque) VALUES
    ('LEA CADA PREGUNTA CON ATENCIÓN Y ELIJA UNA ÚNICA OPCIÓN. LAS PREGUNTAS SE PRESENTAN EN BLOQUES: DEBE RESPONDER TODAS LAS PREGUNTAS DEL BLOQUE PARA AVANZAR Y, UNA VEZ QUE AVANZA, NO PUEDE VOLVER A UN BLOQUE ANTERIOR NI MODIFICAR SUS RESPUESTAS. PUEDE SALIR Y CONTINUAR MÁS TARDE MIENTRAS NO SE SUPERE EL TIEMPO MÁXIMO NI LA CANTIDAD MÁXIMA DE INGRESOS.', 60, 168, 5, 2);

-- Las ponderaciones de las opciones de cada pregunta deben sumar 10 (CU 021)
UPDATE pregunta_opcion_respuesta SET ponderacion = 10.0 WHERE pregunta_id IN (1, 2) AND ponderacion = 1.0;

-- Parámetros de los cuestionarios ya existentes
UPDATE cuestionario SET tiempo_maximo_minutos = 60, tiempo_maximo_activo_horas = 168, cantidad_maxima_accesos = 5, cantidad_preguntas_por_bloque = 2;
UPDATE cuestionario SET fecha_inicio = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_finalizacion = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_ultimo_ingreso = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_cambio_estado = DATEADD('DAY', -1, CURRENT_TIMESTAMP), cantidad_accesos = 1 WHERE id = 3;

-- Candidatos para el orden de mérito (CU 028) y para probar el CU 026
INSERT INTO candidato (nro_candidato, tipo_documento, nro_documento, apellido, nombre, email, eliminado) VALUES
    (9, 'DNI', '35111001', 'SOSA', 'VALENTINA', NULL, FALSE),
    (10, 'DNI', '35111002', 'MOLINA', 'TOMÁS', NULL, FALSE),
    (11, 'DNI', '35111003', 'ACOSTA', 'LUCÍA', NULL, FALSE),
    (12, 'DNI', '35111004', 'RÍOS', 'JOAQUÍN', NULL, FALSE),
    (13, 'DNI', '35111005', 'CASTRO', 'PAULA', NULL, FALSE),
    (14, 'DNI', '35111006', 'VEGA', 'NICOLÁS', NULL, FALSE),
    (15, 'DNI', '35111007', 'TORRES', 'MARTINA', NULL, FALSE),
    (16, 'DNI', '35111008', 'SUÁREZ', 'DIEGO', NULL, FALSE),
    (17, 'DNI', '35111009', 'MEDINA', 'RAMIRO', NULL, FALSE),
    (18, 'DNI', '35111010', 'HERRERA', 'CLARA', NULL, FALSE);

-- Evaluación 2: BACK-01 hace 10 días (cuestionarios finalizados). Evaluación 3: BACK-01 reciente.
INSERT INTO evaluacion (puesto_id, fecha_creacion) VALUES
    (3, DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
    (3, CURRENT_TIMESTAMP);

-- id 4..10: evaluación 2 | id 11..13: evaluación 3
INSERT INTO cuestionario (candidato_id, evaluacion_id, puesto_id, estado, clave_acceso, fecha_creacion, fecha_inicio, fecha_finalizacion, fecha_ultimo_ingreso, fecha_cambio_estado, cantidad_accesos, tiempo_maximo_minutos, tiempo_maximo_activo_horas, cantidad_maxima_accesos, cantidad_preguntas_por_bloque, eliminado) VALUES
    (9, 2, 3, 'COMPLETO', 'Sosa0009', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    (10, 2, 3, 'COMPLETO', 'Moli0010', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 2, 60, 168, 5, 2, FALSE),
    (11, 2, 3, 'COMPLETO', 'Acos0011', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    (12, 2, 3, 'COMPLETO', 'Rios0012', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 3, 60, 168, 5, 2, FALSE),
    (13, 2, 3, 'INCOMPLETO', 'Cast0013', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), NULL, DATEADD('DAY', -8, CURRENT_TIMESTAMP), DATEADD('DAY', -8, CURRENT_TIMESTAMP), 5, 60, 168, 5, 2, FALSE),
    (14, 2, 3, 'SIN_CONTESTAR', 'Vega0014', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL, NULL, NULL, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0, 60, 168, 5, 2, FALSE),
    (18, 2, 3, 'SIN_CONTESTAR', 'Herrer18', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL, NULL, NULL, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0, 60, 168, 5, 2, FALSE),
    (15, 3, 3, 'EN_PROCESO', 'Torres15', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), NULL, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    (16, 3, 3, 'ACTIVO', 'Suarez16', CURRENT_TIMESTAMP, NULL, NULL, NULL, CURRENT_TIMESTAMP, 0, 60, 168, 5, 2, FALSE),
    (17, 3, 3, 'INCOMPLETO', 'Medina17', DATEADD('HOUR', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', -3, CURRENT_TIMESTAMP), NULL, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE);

INSERT INTO bloque_cuestionario (cuestionario_id, numero, completado) VALUES
    (3, 1, TRUE),
    (4, 1, TRUE),
    (4, 2, TRUE),
    (5, 1, TRUE),
    (5, 2, TRUE),
    (6, 1, TRUE),
    (6, 2, TRUE),
    (7, 1, TRUE),
    (7, 2, TRUE),
    (8, 1, TRUE),
    (8, 2, FALSE),
    (11, 1, TRUE),
    (11, 2, FALSE),
    (13, 1, TRUE),
    (13, 2, FALSE);

INSERT INTO pregunta_asignada (bloque_id, pregunta_id, nro_orden) VALUES
    (1, 3, 1),
    (1, 4, 2),
    (2, 1, 1),
    (2, 3, 2),
    (3, 4, 3),
    (3, 2, 4),
    (4, 3, 1),
    (4, 2, 2),
    (5, 1, 3),
    (5, 4, 4),
    (6, 2, 1),
    (6, 4, 2),
    (7, 3, 3),
    (7, 1, 4),
    (8, 4, 1),
    (8, 1, 2),
    (9, 2, 3),
    (9, 3, 4),
    (10, 1, 1),
    (10, 3, 2),
    (11, 2, 3),
    (11, 4, 4),
    (12, 2, 1),
    (12, 4, 2),
    (13, 1, 3),
    (13, 3, 4),
    (14, 3, 1),
    (14, 1, 2),
    (15, 4, 3),
    (15, 2, 4);

INSERT INTO respuesta_candidato (pregunta_asignada_id, opcion_id, fecha_respuesta) VALUES
    (1, 7, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (2, 9, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (3, 1, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (4, 7, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (5, 10, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (6, 3, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (7, 7, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (8, 4, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (9, 1, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (10, 9, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (11, 3, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (12, 8, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (13, 7, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (14, 1, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (15, 9, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (16, 2, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (17, 4, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (18, 6, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (19, 1, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (20, 6, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (23, 3, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)),
    (24, 10, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP)),
    (27, 6, DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
    (28, 1, DATEADD('HOUR', -3, CURRENT_TIMESTAMP));

INSERT INTO resultado (cuestionario_id, puntaje_obtenido, fecha_calculo) VALUES
    (3, 5.0, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (4, 8.5, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (5, 5.0, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (6, 6.75, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    (7, 1.5, DATEADD('DAY', -9, CURRENT_TIMESTAMP));

INSERT INTO resultado_competencia (resultado_id, caracteristica_puesto_id, total_competencia) VALUES
    (1, 1, 5.0),
    (2, 5, 10.0),
    (2, 6, 7.0),
    (3, 5, 5.0),
    (3, 6, 5.0),
    (4, 5, 10.0),
    (4, 6, 3.5),
    (5, 5, 0.0),
    (5, 6, 3.0);

INSERT INTO resultado_factor (resultado_competencia_id, factor_id, total_factor) VALUES
    (1, 1, 5.0),
    (2, 3, 10.0),
    (3, 1, 7.0),
    (4, 3, 5.0),
    (5, 1, 5.0),
    (6, 3, 10.0),
    (7, 1, 3.5),
    (8, 3, 0.0),
    (9, 1, 3.0);

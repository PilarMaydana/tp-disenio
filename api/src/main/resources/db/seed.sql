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
    ((SELECT id FROM puesto WHERE codigo = 'DEV-01'), CURRENT_TIMESTAMP);

-- Los cuestionarios de DEV-01 ya cargados más arriba quedan agrupados en esa evaluación
-- (no se vuelven a insertar: duplicarlos dejaba dos cuestionarios abiertos con la misma clave).
UPDATE cuestionario SET evaluacion_id = (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'DEV-01'))
WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'DEV-01') AND evaluacion_id IS NULL;


-- ---------------------------------------------------------------------------
-- CU 025 - Evaluar candidatos: datos adicionales para probar los flujos
-- ---------------------------------------------------------------------------
-- Preguntas para el factor COMUNICACIÓN ORAL (FAC-01): con 2 preguntas la
-- competencia COMUNICACIÓN pasa a ser evaluable.
INSERT INTO pregunta (nombre, enunciado, descripcion, factor_id, eliminado) VALUES
    ('EXPOSICIÓN EN REUNIONES', '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?', 'COMUNICACIÓN VERBAL', 1, FALSE),
    ('ESCUCHA ACTIVA', '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?', 'COMUNICACIÓN VERBAL', 1, FALSE);

INSERT INTO pregunta_opcion_respuesta (pregunta_id, nombre, descripcion, nro_orden, ponderacion) VALUES
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 'NUNCA', 'NUNCA', 1, 0.0),
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 'A VECES', 'A VECES', 2, 3.0),
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 'SIEMPRE', 'SIEMPRE', 3, 7.0),
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 'NUNCA', 'NUNCA', 1, 0.0),
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 'A VECES', 'A VECES', 2, 3.0),
    ((SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 'SIEMPRE', 'SIEMPRE', 3, 7.0);

-- Puesto evaluable: todas sus competencias tienen al menos un factor con 2 preguntas.
-- (DEV-01 y QA-01 quedan como ejemplos de puestos NO evaluables - flujo 9.A)
INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado) VALUES
    ('BACK-01', 'DESARROLLADOR BACKEND JUNIOR', 'DESARROLLO DE SERVICIOS Y APIS.', 'TECH SOLUTIONS', FALSE);

INSERT INTO caracteristica_puesto (puesto_id, competencia_id, ponderacion_requerida) VALUES
    ((SELECT id FROM puesto WHERE codigo = 'BACK-01'), (SELECT id FROM competencia WHERE codigo = 'TEC-01'), 5.0),
    ((SELECT id FROM puesto WHERE codigo = 'BACK-01'), (SELECT id FROM competencia WHERE codigo = 'COM-01'), 4.0);

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

-- Parámetros de los cuestionarios ya existentes
UPDATE cuestionario SET tiempo_maximo_minutos = 60, tiempo_maximo_activo_horas = 168, cantidad_maxima_accesos = 5, cantidad_preguntas_por_bloque = 2;
UPDATE cuestionario SET fecha_inicio = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_finalizacion = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_ultimo_ingreso = DATEADD('DAY', -1, CURRENT_TIMESTAMP), fecha_cambio_estado = DATEADD('DAY', -1, CURRENT_TIMESTAMP), cantidad_accesos = 1 WHERE clave_acceso = 'COMP1234';

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
    ((SELECT id FROM puesto WHERE codigo = 'BACK-01'), DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
    ((SELECT id FROM puesto WHERE codigo = 'BACK-01'), CURRENT_TIMESTAMP);

-- Evaluación 2 (la más antigua de BACK-01): cuestionarios finalizados. Evaluación 3 (la más reciente): en curso.
INSERT INTO cuestionario (candidato_id, evaluacion_id, puesto_id, estado, clave_acceso, fecha_creacion, fecha_inicio, fecha_finalizacion, fecha_ultimo_ingreso, fecha_cambio_estado, cantidad_accesos, tiempo_maximo_minutos, tiempo_maximo_activo_horas, cantidad_maxima_accesos, cantidad_preguntas_por_bloque, eliminado) VALUES
    ((SELECT id FROM candidato WHERE nro_candidato = 9), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'COMPLETO', 'Sosa0009', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 10), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'COMPLETO', 'Moli0010', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 2, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 11), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'COMPLETO', 'Acos0011', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 12), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'COMPLETO', 'Rios0012', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 3, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 13), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'INCOMPLETO', 'Cast0013', DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), NULL, DATEADD('DAY', -8, CURRENT_TIMESTAMP), DATEADD('DAY', -8, CURRENT_TIMESTAMP), 5, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 14), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'SIN_CONTESTAR', 'Vega0014', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL, NULL, NULL, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 18), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion ASC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'SIN_CONTESTAR', 'Herrer18', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL, NULL, NULL, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 15), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion DESC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'EN_PROCESO', 'Torres15', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), NULL, DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 16), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion DESC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'ACTIVO', 'Suarez16', CURRENT_TIMESTAMP, NULL, NULL, NULL, CURRENT_TIMESTAMP, 0, 60, 168, 5, 2, FALSE),
    ((SELECT id FROM candidato WHERE nro_candidato = 17), (SELECT id FROM evaluacion WHERE puesto_id = (SELECT id FROM puesto WHERE codigo = 'BACK-01') ORDER BY fecha_creacion DESC LIMIT 1), (SELECT id FROM puesto WHERE codigo = 'BACK-01'), 'INCOMPLETO', 'Medina17', DATEADD('HOUR', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', -3, CURRENT_TIMESTAMP), NULL, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 1, 60, 168, 5, 2, FALSE);

INSERT INTO bloque_cuestionario (cuestionario_id, numero, completado) VALUES
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'COMP1234'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Sosa0009'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Sosa0009'), 2, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Moli0010'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Moli0010'), 2, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Acos0011'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Acos0011'), 2, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Rios0012'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Rios0012'), 2, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Cast0013'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Cast0013'), 2, FALSE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Torres15'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Torres15'), 2, FALSE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Medina17'), 1, TRUE),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Medina17'), 2, FALSE);

INSERT INTO pregunta_asignada (bloque_id, pregunta_id, nro_orden) VALUES
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'COMP1234' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'COMP1234' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 4),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?'), 1),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND b.numero = 1), (SELECT id FROM pregunta WHERE enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?'), 2),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?'), 3),
    ((SELECT b.id FROM bloque_cuestionario b JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND b.numero = 2), (SELECT id FROM pregunta WHERE enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?'), 4);

INSERT INTO respuesta_candidato (pregunta_asignada_id, opcion_id, fecha_respuesta) VALUES
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'COMP1234' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'SIEMPRE'), DATEADD('DAY',-1,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'COMP1234' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'A VECES'), DATEADD('DAY',-1,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN PARADIGMA BASADO EN OBJETOS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'SIEMPRE'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND pa.nro_orden = 3), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'SIEMPRE'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Sosa0009' AND pa.nro_orden = 4), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?' AND o.descripcion = 'GARANTIZA UNA ÚNICA INSTANCIA'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'SIEMPRE'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?' AND o.descripcion = 'CREA MUCHAS INSTANCIAS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND pa.nro_orden = 3), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN PARADIGMA BASADO EN OBJETOS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Moli0010' AND pa.nro_orden = 4), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'A VECES'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?' AND o.descripcion = 'GARANTIZA UNA ÚNICA INSTANCIA'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'NUNCA'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND pa.nro_orden = 3), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'SIEMPRE'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Acos0011' AND pa.nro_orden = 4), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN PARADIGMA BASADO EN OBJETOS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'A VECES'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN LENGUAJE DE PROGRAMACIÓN'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND pa.nro_orden = 3), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?' AND o.descripcion = 'CREA MUCHAS INSTANCIAS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Rios0012' AND pa.nro_orden = 4), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'A VECES'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN PARADIGMA BASADO EN OBJETOS'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Cast0013' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'A VECES'), DATEADD('DAY',-9,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿PARA QUÉ SIRVE EL PATRÓN SINGLETON?' AND o.descripcion = 'GARANTIZA UNA ÚNICA INSTANCIA'), DATEADD('MINUTE',-10,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Torres15' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA PIDE ACLARACIONES CUANDO NO ENTIENDE UNA CONSIGNA?' AND o.descripcion = 'SIEMPRE'), DATEADD('MINUTE',-10,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND pa.nro_orden = 1), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿CON QUÉ FRECUENCIA EXPONE SUS IDEAS EN REUNIONES DE EQUIPO?' AND o.descripcion = 'A VECES'), DATEADD('HOUR',-3,CURRENT_TIMESTAMP)),
    ((SELECT pa.id FROM pregunta_asignada pa JOIN bloque_cuestionario b ON b.id = pa.bloque_id JOIN cuestionario c ON c.id = b.cuestionario_id WHERE c.clave_acceso = 'Medina17' AND pa.nro_orden = 2), (SELECT o.id FROM pregunta_opcion_respuesta o JOIN pregunta p ON p.id = o.pregunta_id WHERE p.enunciado = '¿QUÉ ES LA PROGRAMACIÓN ORIENTADA A OBJETOS (POO)?' AND o.descripcion = 'UN PARADIGMA BASADO EN OBJETOS'), DATEADD('HOUR',-3,CURRENT_TIMESTAMP));

INSERT INTO resultado (cuestionario_id, puntaje_obtenido, fecha_calculo) VALUES
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'COMP1234'), 5.0, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Sosa0009'), 7.0, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Moli0010'), 5.0, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Acos0011'), 5.25, DATEADD('DAY', -9, CURRENT_TIMESTAMP)),
    ((SELECT id FROM cuestionario WHERE clave_acceso = 'Rios0012'), 3.0, DATEADD('DAY', -9, CURRENT_TIMESTAMP));

INSERT INTO resultado_competencia (resultado_id, caracteristica_puesto_id, total_competencia) VALUES
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'COMP1234'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'DEV-01' AND co.codigo = 'COM-01'), 5.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Sosa0009'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'TEC-01'), 7.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Sosa0009'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'COM-01'), 7.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Moli0010'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'TEC-01'), 5.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Moli0010'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'COM-01'), 5.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Acos0011'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'TEC-01'), 7.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Acos0011'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'COM-01'), 3.5),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Rios0012'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'TEC-01'), 3.0),
    ((SELECT r.id FROM resultado r JOIN cuestionario c ON c.id = r.cuestionario_id WHERE c.clave_acceso = 'Rios0012'), (SELECT cp.id FROM caracteristica_puesto cp JOIN puesto pu ON pu.id = cp.puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE pu.codigo = 'BACK-01' AND co.codigo = 'COM-01'), 3.0);

INSERT INTO resultado_factor (resultado_competencia_id, factor_id, total_factor) VALUES
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'COMP1234' AND co.codigo = 'COM-01'), (SELECT id FROM factor WHERE codigo = 'FAC-01'), 5.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Sosa0009' AND co.codigo = 'TEC-01'), (SELECT id FROM factor WHERE codigo = 'FAC-03'), 7.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Sosa0009' AND co.codigo = 'COM-01'), (SELECT id FROM factor WHERE codigo = 'FAC-01'), 7.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Moli0010' AND co.codigo = 'TEC-01'), (SELECT id FROM factor WHERE codigo = 'FAC-03'), 5.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Moli0010' AND co.codigo = 'COM-01'), (SELECT id FROM factor WHERE codigo = 'FAC-01'), 5.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Acos0011' AND co.codigo = 'TEC-01'), (SELECT id FROM factor WHERE codigo = 'FAC-03'), 7.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Acos0011' AND co.codigo = 'COM-01'), (SELECT id FROM factor WHERE codigo = 'FAC-01'), 3.5),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Rios0012' AND co.codigo = 'TEC-01'), (SELECT id FROM factor WHERE codigo = 'FAC-03'), 3.0),
    ((SELECT rc.id FROM resultado_competencia rc JOIN resultado r ON r.id = rc.resultado_id JOIN cuestionario c ON c.id = r.cuestionario_id JOIN caracteristica_puesto cp ON cp.id = rc.caracteristica_puesto_id JOIN competencia co ON co.id = cp.competencia_id WHERE c.clave_acceso = 'Rios0012' AND co.codigo = 'COM-01'), (SELECT id FROM factor WHERE codigo = 'FAC-01'), 3.0);

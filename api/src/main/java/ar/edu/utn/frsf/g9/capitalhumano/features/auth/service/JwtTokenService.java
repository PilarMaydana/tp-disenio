package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para la generación y firma de tokens JWT (JSON Web Tokens) bajo algoritmo HMAC-SHA256.
 */
@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            ObjectMapper objectMapper
    ) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("La clave JWT debe tener al menos 32 caracteres");
        }
        this.secretKey = secretKey;
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera un token JWT para un usuario consultor.
     *
     * @param usuario nombre de usuario
     * @param usuarioId identificador numérico
     * @return token JWT firmado
     */
    public String generarTokenConsultor(String usuario, Long usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario);
        claims.put("rol", "CONSULTOR");
        claims.put("usuarioId", usuarioId);
        return generarToken(claims);
    }

    /**
     * Genera un token JWT para un candidato que realiza un cuestionario.
     *
     * @param nroDocumento número de documento
     * @param candidatoId identificador del candidato
     * @param cuestionarioId identificador del cuestionario
     * @return token JWT firmado
     */
    public String generarTokenCandidato(String nroDocumento, Long candidatoId, Long cuestionarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", nroDocumento);
        claims.put("rol", "CANDIDATO");
        claims.put("usuarioId", candidatoId);
        claims.put("cuestionarioId", cuestionarioId);
        return generarToken(claims);
    }

    /**
     * Construye y firma un JWT a partir del mapa de claims.
     *
     * @param claims contenido a incluir en el payload
     * @return JWT firmado en formato standard (header.payload.signature)
     */
    public String generarToken(Map<String, Object> claims) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

            String headerEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8)
            );
            String payloadEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8)
            );

            String contentToSign = headerEncoded + "." + payloadEncoded;
            String signatureEncoded = firmarHmacSha256(contentToSign, secretKey);

            return contentToSign + "." + signatureEncoded;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error al serializar claims de JWT", e);
        }
    }

    /** Verifica firma, expiración, rol y cuestionario asociado antes de aceptar respuestas. */
    public boolean esTokenValidoParaCuestionario(String token, Long cuestionarioId) {
        try {
            TokenClaims claims = validarToken(token);
            return "CANDIDATO".equals(claims.rol())
                    && claims.cuestionarioId() != null
                    && claims.cuestionarioId().equals(cuestionarioId);
        } catch (Exception e) {
            return false;
        }
    }

    /** Valida firma y expiración y devuelve los datos de identidad del JWT. */
    public TokenClaims validarToken(String token) {
    /**
     * Valida la firma y la vigencia de un token generado por este servicio y devuelve sus claims.
     *
     * @param token JWT recibido (sin el prefijo "Bearer ")
     * @return claims del token
     * @throws AutenticacionException si el token falta, está mal formado, fue alterado o expiró
     */
    public Map<String, Object> validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AutenticacionException("Debe iniciar sesión para continuar");
        }
        String[] partes = token.trim().split("[.]");
        if (partes.length != 3) {
            throw new AutenticacionException("Sesión inválida");
        }
        String firmaEsperada = firmarHmacSha256(partes[0] + "." + partes[1], secretKey);
        if (!MessageDigest.isEqual(firmaEsperada.getBytes(StandardCharsets.UTF_8),
                partes[2].getBytes(StandardCharsets.UTF_8))) {
            throw new AutenticacionException("Sesión inválida");
        }
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) return null;
            JsonNode header = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[0]));
            if (!"HS256".equals(header.path("alg").asText())) return null;

            String contenido = partes[0] + "." + partes[1];
            byte[] firmaEsperada = firmarHmacSha256(contenido, secretKey).getBytes(StandardCharsets.US_ASCII);
            byte[] firmaRecibida = partes[2].getBytes(StandardCharsets.US_ASCII);
            if (!MessageDigest.isEqual(firmaEsperada, firmaRecibida)) return null;

            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[1]));
            String rol = payload.path("rol").asText();
            String subject = payload.path("sub").asText();
            long expiracion = payload.path("exp").asLong(0L);
            if (subject.isBlank() || expiracion <= Instant.now().getEpochSecond()
                    || !("CONSULTOR".equals(rol) || "CANDIDATO".equals(rol))) return null;

            long usuarioId = payload.path("usuarioId").asLong(-1L);
            if (usuarioId < 0) return null;
            Long cuestionarioId = payload.hasNonNull("cuestionarioId")
                    ? payload.path("cuestionarioId").asLong() : null;
            return new TokenClaims(subject, rol, usuarioId, cuestionarioId);
        } catch (Exception e) {
            return null;
            byte[] json = Base64.getUrlDecoder().decode(partes[1]);
            Map<String, Object> claims = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() { });
            Object exp = claims.get("exp");
            if (exp instanceof Number n && n.longValue() < Instant.now().getEpochSecond()) {
                throw new AutenticacionException("La sesión expiró, vuelva a ingresar");
            }
            return claims;
        } catch (IllegalArgumentException | java.io.IOException e) {
            throw new AutenticacionException("Sesión inválida");
        }
    }

    public record TokenClaims(String subject, String rol, Long usuarioId, Long cuestionarioId) { }

    private String firmarHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error al firmar token JWT con algoritmo HMAC-SHA256", e);
        }
    }
}

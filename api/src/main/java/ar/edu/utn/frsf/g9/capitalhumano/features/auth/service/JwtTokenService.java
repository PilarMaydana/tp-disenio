package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import ar.edu.utn.frsf.g9.capitalhumano.features.auth.exception.AutenticacionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Servicio para generar y validar tokens JWT con firma HMAC-SHA256. */
@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secretKey;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            ObjectMapper objectMapper) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("La clave JWT debe tener al menos 32 caracteres");
        }
        this.secretKey = secretKey;
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
    }

    public String generarTokenConsultor(String usuario, Long usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario);
        claims.put("rol", "CONSULTOR");
        claims.put("usuarioId", usuarioId);
        return generarToken(claims);
    }

    public String generarTokenCandidato(String nroDocumento, Long candidatoId, Long cuestionarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", nroDocumento);
        claims.put("rol", "CANDIDATO");
        claims.put("usuarioId", candidatoId);
        claims.put("cuestionarioId", cuestionarioId);
        return generarToken(claims);
    }

    public String generarToken(Map<String, Object> claims) {
        try {
            long ahora = Instant.now().getEpochSecond();
            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("iat", ahora);
            payload.put("exp", ahora + expirationSeconds);

            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String headerEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = encoder.encodeToString(
                    objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
            String contenido = headerEncoded + "." + payloadEncoded;
            return contenido + "." + firmarHmacSha256(contenido, secretKey);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error al serializar claims de JWT", e);
        }
    }

    /** Valida la firma y vigencia del token y devuelve todos sus claims. */
    public Map<String, Object> validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AutenticacionException("Debe iniciar sesión para continuar");
        }
        String[] partes = token.trim().split("\\.");
        if (partes.length != 3) {
            throw new AutenticacionException("Sesión inválida");
        }

        try {
            JsonNode header = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[0]));
            if (!"HS256".equals(header.path("alg").asText())) {
                throw new AutenticacionException("Sesión inválida");
            }
            String firmaEsperada = firmarHmacSha256(partes[0] + "." + partes[1], secretKey);
            if (!MessageDigest.isEqual(firmaEsperada.getBytes(StandardCharsets.US_ASCII),
                    partes[2].getBytes(StandardCharsets.US_ASCII))) {
                throw new AutenticacionException("Sesión inválida");
            }

            byte[] payload = Base64.getUrlDecoder().decode(partes[1]);
            Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<>() { });
            Object expiracion = claims.get("exp");
            if (!(expiracion instanceof Number numeroExpiracion)) {
                throw new AutenticacionException("Sesión inválida");
            }
            if (numeroExpiracion.longValue() <= Instant.now().getEpochSecond()) {
                throw new AutenticacionException("La sesión expiró, vuelva a ingresar");
            }
            return claims;
        } catch (IllegalArgumentException | java.io.IOException e) {
            throw new AutenticacionException("Sesión inválida");
        }
    }

    /** Devuelve los claims esenciales tipados o null si el token no representa un usuario válido. */
    public TokenClaims validarTokenClaims(String token) {
        Map<String, Object> claims;
        try {
            claims = validarToken(token);
        } catch (AutenticacionException e) {
            return null;
        }

        Object subject = claims.get("sub");
        Object rol = claims.get("rol");
        Object usuarioId = claims.get("usuarioId");
        Object cuestionarioId = claims.get("cuestionarioId");
        if (!(subject instanceof String subjectText) || subjectText.isBlank()
                || !(rol instanceof String rolText)
                || !("CONSULTOR".equals(rolText) || "CANDIDATO".equals(rolText))
                || !(usuarioId instanceof Number usuarioIdNumber) || usuarioIdNumber.longValue() < 0) {
            return null;
        }
        Long cuestionarioIdValue = cuestionarioId instanceof Number id
                ? id.longValue() : null;
        return new TokenClaims(subjectText, rolText, usuarioIdNumber.longValue(), cuestionarioIdValue);
    }

    /** Valida firma, expiración, rol y cuestionario asociado antes de aceptar respuestas. */
    public boolean esTokenValidoParaCuestionario(String token, Long cuestionarioId) {
        TokenClaims claims = validarTokenClaims(token);
        return claims != null
                && "CANDIDATO".equals(claims.rol())
                && claims.cuestionarioId() != null
                && claims.cuestionarioId().equals(cuestionarioId);
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

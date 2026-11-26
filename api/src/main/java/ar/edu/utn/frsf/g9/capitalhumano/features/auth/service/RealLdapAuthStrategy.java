package ar.edu.utn.frsf.g9.capitalhumano.features.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.Rdn;
import java.util.Hashtable;

/** Autentica consultores realizando un bind LDAP con el DN configurado. */
@Component
@Profile("prod")
public class RealLdapAuthStrategy implements ILdapAuthStrategy {

    private static final Logger log = LoggerFactory.getLogger(RealLdapAuthStrategy.class);

    private final String ldapUrl;
    private final String userDnPattern;

    public RealLdapAuthStrategy(
            @Value("${app.ldap.url:}") String ldapUrl,
            @Value("${app.ldap.user-dn-pattern:}") String userDnPattern) {
        if (ldapUrl == null || !ldapUrl.regionMatches(true, 0, "ldaps://", 0, 8)
                || userDnPattern == null || userDnPattern.isBlank() || !userDnPattern.contains("{0}")) {
            throw new IllegalArgumentException("El perfil prod requiere LDAPS y un DN de usuario con el marcador {0}");
        }
        this.ldapUrl = ldapUrl;
        this.userDnPattern = userDnPattern;
    }

    @Override
    public boolean authenticate(String usuario, String contrasena) {
        if (usuario == null || contrasena == null || usuario.isBlank() || contrasena.isBlank()) {
            return false;
        }
        String principal = userDnPattern.replace("{0}", Rdn.escapeValue(usuario.trim()));
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapUrl);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, principal);
        environment.put(Context.SECURITY_CREDENTIALS, contrasena);
        environment.put("com.sun.jndi.ldap.connect.timeout", "5000");
        environment.put("com.sun.jndi.ldap.read.timeout", "5000");

        try {
            new InitialDirContext(environment).close();
            return true;
        } catch (AuthenticationException e) {
            return false;
        } catch (NamingException e) {
            log.error("No se pudo completar el bind contra LDAP en {}", ldapUrl, e);
            return false;
        }
    }
}

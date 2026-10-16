package ar.edu.prog3.facturacion;

import java.util.HashMap;
import java.util.Map;

public final class ConfiguracionJpa {
    private ConfiguracionJpa() { }

    /** Las propiedades no sobreescritas conservan el valor de persistence.xml. */
    public static Map<String, Object> desdeEntorno() {
        Map<String, Object> propiedades = new HashMap<>();
        copiarVariable(propiedades, "DB_URL", "javax.persistence.jdbc.url");
        copiarVariable(propiedades, "DB_USER", "javax.persistence.jdbc.user");
        copiarVariable(propiedades, "DB_PASSWORD", "javax.persistence.jdbc.password");
        return propiedades;
    }

    private static void copiarVariable(Map<String, Object> destino, String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null) {
            destino.put(propiedad, valor);
        }
    }
}

package pe.ferreteria.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de contraseñas con PBKDF2 (solo JDK, sin frameworks).
 * Formato guardado: iteraciones:sal:hash (sal y hash en Base64).
 */
public final class PasswordUtil {
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private PasswordUtil() {
    }

    public static String hashear(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        new SecureRandom().nextBytes(sal);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal) + ":"
                + Base64.getEncoder().encodeToString(derivar(clave.toCharArray(), sal, ITERACIONES));
    }

    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = derivar(clave.toCharArray(), sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado); // comparación en tiempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave, sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la clave.", e);
        }
    }

    /** Utilidad para generar el hash de una clave nueva: java PasswordUtil <clave>. */
    public static void main(String[] args) {
        System.out.println(hashear(args[0]));
    }
}

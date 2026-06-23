package util;

import java.text.DecimalFormat;

public final class FormatUtil {
    private static final DecimalFormat DINERO = new DecimalFormat("$0.00");

    private FormatUtil() {
    }

    public static String dinero(double valor) {
        return DINERO.format(valor);
    }
}

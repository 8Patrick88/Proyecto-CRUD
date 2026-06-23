package service;

import dao.MovimientoInventarioDAO;
import model.MovimientoInventario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovimientoInventarioService {
    public static final String ALTA_PRODUCTO = "Alta de producto";
    public static final String MODIFICACION = "Modificación";
    public static final String ELIMINACION = "Eliminación";
    public static final String INCREMENTO_STOCK = "Incremento de stock";
    public static final String REDUCCION_STOCK = "Reducción de stock";

    private final MovimientoInventarioDAO dao = new MovimientoInventarioDAO();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private List<MovimientoInventario> cacheMovimientos;
    private int secuenciaMovimiento = -1;

    public void registrar(String tipo, String idProducto, String nombreProducto,
                          int cantidadAnterior, int cantidadNueva, String observacion) {
        LocalDateTime ahora = LocalDateTime.now();
        String usuario = SesionUsuario.getUsuarioActual() != null
                ? SesionUsuario.getUsuarioActual().getNombre()
                : "Sistema";

        MovimientoInventario movimiento = new MovimientoInventario(
                generarSiguienteId(),
                usuario,
                ahora.format(FECHA),
                ahora.format(HORA),
                tipo,
                idProducto,
                nombreProducto,
                cantidadAnterior,
                cantidadNueva,
                observacion == null ? "" : observacion
        );
        dao.guardar(movimiento);
        invalidarCache();
    }

    public List<MovimientoInventario> listarTodos() {
        if (cacheMovimientos == null) {
            cacheMovimientos = dao.listarTodos().stream()
                    .sorted(Comparator.comparing(MovimientoInventario::getFecha).reversed()
                            .thenComparing(MovimientoInventario::getHora).reversed())
                    .collect(Collectors.toList());
        }
        return cacheMovimientos;
    }

    public List<MovimientoInventario> buscar(String criterio, String fecha) {
        String texto = criterio == null ? "" : criterio.trim().toLowerCase();
        String fechaFiltro = fecha == null ? "" : fecha.trim();

        return listarTodos().stream()
                .filter(m -> texto.isEmpty()
                        || m.getIdProducto().toLowerCase().contains(texto)
                        || m.getNombreProducto().toLowerCase().contains(texto)
                        || m.getTipo().toLowerCase().contains(texto)
                        || m.getUsuario().toLowerCase().contains(texto))
                .filter(m -> fechaFiltro.isEmpty() || m.getFecha().equals(fechaFiltro))
                .collect(Collectors.toList());
    }

    public void invalidarCache() {
        cacheMovimientos = null;
    }

    private String generarSiguienteId() {
        if (secuenciaMovimiento < 0) {
            secuenciaMovimiento = dao.obtenerMaximoNumero();
        }
        secuenciaMovimiento++;
        return String.format("MOV-%04d", secuenciaMovimiento);
    }
}

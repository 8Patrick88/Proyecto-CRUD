package dao;

public interface CrudDAO<T> {
    void guardar(T entidad);
    T buscarPorId(String id);
    boolean eliminar(String id);
}
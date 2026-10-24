package com.umg.api.reportes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Carga los datos de los desplegables de filtros directamente desde Neon. */
public class Catalogos {

    /** id == null significa "Todos" (se manda como null al reporte). */
    public record Item(Integer id, String texto) {
        @Override
        public String toString() {
            return texto;
        }
    }

    public static List<Item> conciertos() throws SQLException {
        return consultar("Todos los conciertos",
                "SELECT c.id, a.nombre || COALESCE(' — ' || c.gira, '') || ' · ' "
                + "|| to_char(c.fecha AT TIME ZONE 'America/Guatemala', 'DD/MM/YYYY') "
                + "FROM conciertos c JOIN artista a ON a.id = c.\"artistaId\" "
                + "ORDER BY c.fecha DESC");
    }

    public static List<Item> artistas() throws SQLException {
        return consultar("Todos los artistas",
                "SELECT id, nombre FROM artista ORDER BY nombre");
    }

    public static List<Item> vendedores() throws SQLException {
        return consultar("Todos los vendedores",
                "SELECT id, nombre || COALESCE(' · ' || rol, '') FROM usuarios ORDER BY nombre");
    }

    private static List<Item> consultar(String textoTodos, String sql) throws SQLException {
        List<Item> lista = new ArrayList<>();
        lista.add(new Item(null, textoTodos));
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Item(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }
}

package com.umg.api.ui;

import com.umg.api.reportes.ReportService;
import com.umg.api.reportes.Catalogos;
import com.umg.api.reportes.Catalogos.Item;
import com.umg.api.reportes.ReportService;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;

public class ReportesController{

    @FXML private ToggleButton btnConcierto, btnArtista, btnVendedor;
    @FXML private ComboBox<Item> cbConcierto, cbArtista, cbVendedor;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Button btnGenerar, btnAnterior, btnSiguiente;
    @FXML private Label lblPagina, lblEstado;
    @FXML private ImageView ivPagina;
    @FXML private ScrollPane scroll;

    private final ToggleGroup tipo = new ToggleGroup();
    private JasperPrint actual;
    private int pagina;

    // Estilo de los 3 botones de tipo de reporte (solo si el FXML no trae "tile-sm")
    private static final String ESTILO_ON =
            "-fx-background-color:rgba(255,176,32,0.12); -fx-border-color:-c-amber; -fx-border-radius:12; "
            + "-fx-background-radius:12; -fx-text-fill:-c-text; -fx-font-weight:bold; -fx-alignment:CENTER_LEFT;";
    private static final String ESTILO_OFF =
            "-fx-background-color:-c-card; -fx-border-color:-c-line2; -fx-border-radius:12; "
            + "-fx-background-radius:12; -fx-text-fill:-c-text; -fx-font-weight:bold; -fx-alignment:CENTER_LEFT;";

    @FXML
    public void initialize() {
        // Tipo de reporte
        btnConcierto.setToggleGroup(tipo);
        btnArtista.setToggleGroup(tipo);
        btnVendedor.setToggleGroup(tipo);
        btnConcierto.setUserData("reporte_concierto");
        btnArtista.setUserData("reporte_artista");
        btnVendedor.setUserData("reporte_vendedor");
        tipo.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null && a != null) a.setSelected(true);   // siempre uno seleccionado
            pintarBotones();
        });
        btnConcierto.setSelected(true);
        pintarBotones();

        // Fechas por defecto: del 1 del mes a hoy
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));

        // Vista previa ajustada al ancho del panel
        ivPagina.fitWidthProperty().bind(scroll.widthProperty().subtract(60));

        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);

        cargarCatalogos();
    }

    private void pintarBotones() {
        for (ToggleButton b : List.of(btnConcierto, btnArtista, btnVendedor)) {
            if (b.getStyleClass().contains("tile-sm")) continue;   // FXML nuevo: lo pinta styles.css
            b.setStyle(b.isSelected() ? ESTILO_ON : ESTILO_OFF);
        }
    }

    /** Llena los desplegables desde la BD sin congelar la ventana. */
    private void cargarCatalogos() {
        lblEstado.setText("Cargando filtros…");
        Task<Object[]> t = new Task<>() {
            @Override
            protected Object[] call() throws Exception {
                return new Object[]{Catalogos.conciertos(), Catalogos.artistas(), Catalogos.vendedores()};
            }
        };
        t.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked") List<Item> c = (List<Item>) t.getValue()[0];
            @SuppressWarnings("unchecked") List<Item> a = (List<Item>) t.getValue()[1];
            @SuppressWarnings("unchecked") List<Item> v = (List<Item>) t.getValue()[2];
            cbConcierto.getItems().setAll(c);
            cbArtista.getItems().setAll(a);
            cbVendedor.getItems().setAll(v);
            cbConcierto.getSelectionModel().selectFirst();
            cbArtista.getSelectionModel().selectFirst();
            cbVendedor.getSelectionModel().selectFirst();
            lblEstado.setText("");
        });
        t.setOnFailed(e -> lblEstado.setText("No se pudieron cargar los filtros: " + t.getException().getMessage()));
        hilo(t);
    }

    @FXML
    private void generar() {
        if (dpDesde.getValue() == null || dpHasta.getValue() == null) {
            lblEstado.setText("Elige las fechas Desde y Hasta.");
            return;
        }
        if (dpDesde.getValue().isAfter(dpHasta.getValue())) {
            lblEstado.setText("La fecha Desde no puede ser mayor que Hasta.");
            return;
        }

        Item c = cbConcierto.getValue(), a = cbArtista.getValue(), v = cbVendedor.getValue();

        Map<String, Object> p = new HashMap<>();
        p.put("P_DESDE", java.sql.Date.valueOf(dpDesde.getValue()));
        p.put("P_HASTA", java.sql.Date.valueOf(dpHasta.getValue()));
        p.put("P_CONCIERTO", c == null ? null : c.id());
        p.put("P_ARTISTA", a == null ? null : a.id());
        p.put("P_VENDEDOR", v == null ? null : v.id());
        p.put("P_CODIGO", "RPT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));
        p.put("P_GENERADO_POR", usuarioActual());
        p.put("P_FILTROS", resumenFiltros(c, a, v));

        String nombre = (String) tipo.getSelectedToggle().getUserData();

        btnGenerar.setDisable(true);
        lblEstado.setText("Generando reporte…");

        Task<JasperPrint> t = new Task<>() {
            @Override
            protected JasperPrint call() throws Exception {
                return ReportService.llenar(nombre, p);
            }
        };
        t.setOnSucceeded(e -> {
            actual = t.getValue();
            pagina = 0;
            mostrarPagina();
            btnGenerar.setDisable(false);
            lblEstado.setText("");
        });
        t.setOnFailed(e -> {
            btnGenerar.setDisable(false);
            Throwable ex = t.getException();
            ex.printStackTrace();
            lblEstado.setText("Error: " + ex.getMessage());
        });
        hilo(t);
    }

    private void mostrarPagina() {
        if (actual == null || actual.getPages().isEmpty()) {
            ivPagina.setImage(null);
            lblPagina.setText("Sin resultados");
            btnAnterior.setDisable(true);
            btnSiguiente.setDisable(true);
            return;
        }
        try {
            // zoom 2f = más nitidez; el ImageView lo reduce al ancho disponible
            BufferedImage img = (BufferedImage) JasperPrintManager.printPageToImage(actual, pagina, 2f);
            ivPagina.setImage(SwingFXUtils.toFXImage(img, null));
        } catch (Exception ex) {
            lblEstado.setText("No se pudo dibujar la página: " + ex.getMessage());
        }
        int total = actual.getPages().size();
        lblPagina.setText("Vista previa · Página " + (pagina + 1) + " de " + total);
        btnAnterior.setDisable(pagina == 0);
        btnSiguiente.setDisable(pagina >= total - 1);
    }

    @FXML
    private void anterior() {
        if (actual != null && pagina > 0) {
            pagina--;
            mostrarPagina();
        }
    }

    @FXML
    private void siguiente() {
        if (actual != null && pagina < actual.getPages().size() - 1) {
            pagina++;
            mostrarPagina();
        }
    }

    private String resumenFiltros(Item c, Item a, Item v) {
        List<String> f = new ArrayList<>();
        if (c != null && c.id() != null) f.add("Concierto: " + c.texto());
        if (a != null && a.id() != null) f.add("Artista: " + a.texto());
        if (v != null && v.id() != null) f.add("Vendedor: " + v.texto());
        return f.isEmpty() ? "ninguno" : String.join(" | ", f);
    }

    /** Nombre del usuario que inició sesión (el mismo que se ve abajo en el menú). */
    private String usuarioActual() {
        try {
            return com.umg.api.Sesion.getUsuario().toString();
        } catch (Exception e) {
            return "Sistema";
        }
    }

    private void hilo(Task<?> t) {
        Thread th = new Thread(t, "reportes");
        th.setDaemon(true);
        th.start();
    }
}

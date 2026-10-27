package com.umg.api.ui;

import com.umg.api.model.Artista;
import com.umg.api.model.Concierto;
import com.umg.api.model.Estadio;
import com.umg.api.model.Localidad;
import com.umg.api.model.Precio;
import com.umg.api.service.ConciertoService;
import com.umg.api.service.EstadioService;
import com.umg.api.service.LocalidadService;
import com.umg.api.service.PrecioService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConciertoFormController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Button btnCerrar;

    @FXML
    private ComboBox<Concierto> cmbConcierto;

    @FXML
    private ComboBox<Artista> cmbArtista;

    @FXML
    private TextField txtTitulo;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private TextField txtHora;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private ComboBox<Estadio> cmbRecinto;

    @FXML
    private VBox filasPrecios;

    @FXML
    private Label lblAforoTotal;

    @FXML
    private Label lblRecaudacion;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    public static Integer conciertoInicial;

    private final ConciertoService conciertoService = new ConciertoService();
    private final EstadioService estadioService = new EstadioService();
    private final LocalidadService localidadService = new LocalidadService();
    private final PrecioService precioService = new PrecioService();

    private final List<FilaPrecio> filas = new ArrayList<>();

    private static class FilaPrecio {

        Localidad localidad;
        CheckBox activa;
        TextField precio;
        TextField aforo;
        Label lblAforo;
        Button numerada;
        Button dePie;
        boolean modoDePie;
        boolean teniaPrecio;
    }

    @FXML
    private void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList("Programado", "En venta", "Agotado", "Finalizado", "Cancelado"));

        try {
            cmbArtista.setItems(FXCollections.observableArrayList(conciertoService.getArtistas()));
            cmbRecinto.setItems(FXCollections.observableArrayList(estadioService.getEstadios()));
            List<Concierto> lista = new ArrayList<>();
            lista.add(new Concierto());
            lista.addAll(conciertoService.getConciertos());
            cmbConcierto.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, "No se pudieron cargar los datos: " + ex.getMessage());
        }

        cmbConcierto.setOnAction(e -> mostrarConcierto());
        cmbRecinto.setOnAction(e -> cargarFilas());
        btnCerrar.setOnAction(e -> cerrar());
        btnCancelar.setOnAction(e -> cerrar());
        btnGuardar.setOnAction(e -> guardar());

        cmbConcierto.getSelectionModel().selectFirst();
        if (conciertoInicial != null) {
            for (Concierto c : cmbConcierto.getItems()) {
                if (c.getId() == conciertoInicial) {
                    cmbConcierto.getSelectionModel().select(c);
                }
            }
            conciertoInicial = null;
        }
        mostrarConcierto();
    }

    private Concierto elegido() {
        Concierto c = cmbConcierto.getValue();
        return c == null || c.getId() == 0 ? null : c;
    }

    private void mostrarConcierto() {
        Concierto c = elegido();
        if (c == null) {
            lblTitulo.setText("NUEVO CONCIERTO");
            cmbArtista.getSelectionModel().clearSelection();
            txtTitulo.clear();
            dpFecha.setValue(null);
            txtHora.clear();
            cmbEstado.getSelectionModel().select("Programado");
            cmbRecinto.getSelectionModel().clearSelection();
            cargarFilas();
            return;
        }

        lblTitulo.setText("EDITAR CONCIERTO");
        for (Artista a : cmbArtista.getItems()) {
            if (a.getId() == c.getArtistaId()) {
                cmbArtista.getSelectionModel().select(a);
            }
        }
        txtTitulo.setText(c.getGira());
        dpFecha.setValue(Formato.fecha(c.getFecha()));
        txtHora.setText(Formato.hora(c.getHora()));
        cmbEstado.getSelectionModel().select(c.getEstado());
        for (Estadio e : cmbRecinto.getItems()) {
            if (e.getId() == c.getEstadioId()) {
                cmbRecinto.getSelectionModel().select(e);
            }
        }
        cargarFilas();
    }

    private void cargarFilas() {
        filasPrecios.getChildren().clear();
        filas.clear();
        Estadio estadio = cmbRecinto.getValue();
        if (estadio == null) {
            calcularTotales();
            return;
        }

        Map<Integer, Precio> precios = new HashMap<>();
        List<Localidad> localidades;
        try {
            localidades = localidadService.getLocalidades(estadio.getId());
            Concierto c = elegido();
            if (c != null) {
                for (Precio p : precioService.getPrecios(c.getId())) {
                    precios.put(p.getLocalidadId(), p);
                }
            }
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, "No se pudieron cargar las localidades: " + ex.getMessage());
            return;
        }

        for (Localidad l : localidades) {
            Plantilla p = Plantilla.cargar("FilaPrecio.fxml");
            FilaPrecio fila = new FilaPrecio();
            fila.localidad = l;
            fila.activa = p.get("chkActiva");
            fila.precio = p.get("txtPrecio");
            fila.aforo = p.get("txtAforo");
            fila.lblAforo = p.get("lblAforo");
            fila.numerada = p.get("btnNumerada");
            fila.dePie = p.get("btnDePie");

            p.<Region>get("color").setStyle("-fx-background-color: " + l.getColor() + ";");
            p.<Label>get("lblZona").setText(l.getNombre());

            boolean zona = "ZONA".equals(l.getTipo());
            boolean soloNumerada = "PALCOS".equals(l.getTipo()) || "CAB".equals(l.getCodigo());
            fila.numerada.setVisible(!zona);
            fila.numerada.setManaged(!zona);
            fila.dePie.setVisible(!soloNumerada);
            fila.dePie.setManaged(!soloNumerada);
            fila.numerada.setOnAction(e -> elegirModo(fila, false));
            fila.dePie.setOnAction(e -> elegirModo(fila, true));

            Precio precio = precios.get(l.getId());
            fila.teniaPrecio = precio != null;
            fila.activa.setSelected(elegido() == null || precio != null);
            fila.precio.setText(precio == null ? "" : String.format("%.2f", precio.getPrecio()));
            if (precio != null && precio.getCupo() != null) {
                fila.aforo.setText(String.valueOf(precio.getCupo()));
            } else if (!zona) {
                fila.aforo.setText(String.valueOf(Math.round(l.getTotalAsientos() * 1.8)));
            }
            fila.activa.selectedProperty().addListener((obs, viejo, nuevo) -> mostrarFila(fila));
            fila.precio.textProperty().addListener((obs, viejo, nuevo) -> calcularTotales());
            fila.aforo.textProperty().addListener((obs, viejo, nuevo) -> calcularTotales());

            elegirModo(fila, zona || (precio != null && "DE_PIE".equals(precio.getModo())));
            filas.add(fila);
            filasPrecios.getChildren().add(p.getRaiz());
        }
        calcularTotales();
    }

    private void elegirModo(FilaPrecio fila, boolean dePie) {
        fila.modoDePie = dePie;
        fila.numerada.getStyleClass().remove("seg-on");
        fila.dePie.getStyleClass().remove("seg-on");
        (dePie ? fila.dePie : fila.numerada).getStyleClass().add("seg-on");
        mostrarFila(fila);
    }

    private void mostrarFila(FilaPrecio fila) {
        boolean activa = fila.activa.isSelected();
        fila.lblAforo.setVisible(!fila.modoDePie);
        fila.lblAforo.setManaged(!fila.modoDePie);
        fila.lblAforo.setText(fila.localidad.getTotalAsientos() + " asientos");
        fila.aforo.setVisible(fila.modoDePie);
        fila.aforo.setManaged(fila.modoDePie);
        fila.precio.setDisable(!activa);
        fila.aforo.setDisable(!activa);
        for (Node n : fila.activa.getParent().getChildrenUnmodifiable()) {
            if (n != fila.activa) {
                n.setOpacity(activa ? 1 : 0.45);
            }
        }
        calcularTotales();
    }

    private Double leerPrecio(FilaPrecio f) {
        try {
            return Double.valueOf(f.precio.getText().replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int leerAforo(FilaPrecio f) {
        if (!f.modoDePie) {
            return f.localidad.getTotalAsientos();
        }
        try {
            return Integer.parseInt(f.aforo.getText().replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void calcularTotales() {
        int aforo = 0;
        double recaudacion = 0;
        for (FilaPrecio f : filas) {
            if (f.activa.isSelected()) {
                int capacidad = leerAforo(f);
                aforo += capacidad;
                Double precio = leerPrecio(f);
                if (precio != null) {
                    recaudacion += precio * capacidad;
                }
            }
        }
        lblAforoTotal.setText(String.format("%,d", aforo) + " personas");
        lblRecaudacion.setText(Formato.dineroCorto(recaudacion));
    }

    private void guardar() {
        Artista artista = cmbArtista.getValue();
        Estadio estadio = cmbRecinto.getValue();
        LocalDate fecha = dpFecha.getValue();
        String gira = txtTitulo.getText() == null ? "" : txtTitulo.getText().trim();
        String hora = txtHora.getText() == null ? "" : txtHora.getText().trim();
        String estado = cmbEstado.getValue();

        if (artista == null || estadio == null || fecha == null || gira.isEmpty() || estado == null) {
            Barra.alerta(Alert.AlertType.WARNING, "Completa artista, título, fecha, hora, estado y recinto");
            return;
        }
        if (!hora.matches("([01]?\\d|2[0-3]):[0-5]\\d")) {
            Barra.alerta(Alert.AlertType.WARNING, "Escribe la hora como 20:00");
            return;
        }

        List<FilaPrecio> activas = new ArrayList<>();
        for (FilaPrecio f : filas) {
            if (f.activa.isSelected()) {
                Double precio = leerPrecio(f);
                if (precio == null || precio <= 0) {
                    Barra.alerta(Alert.AlertType.WARNING, "Indica el precio de " + f.localidad.getNombre() + ".");
                    return;
                }
                if (f.modoDePie && leerAforo(f) <= 0) {
                    Barra.alerta(Alert.AlertType.WARNING, "Indica el aforo de " + f.localidad.getNombre() + " (de pie).");
                    return;
                }
                activas.add(f);
            }
        }
        if ("En venta".equals(estado) && activas.isEmpty()) {
            Barra.alerta(Alert.AlertType.WARNING, "Para ponerlo En venta activa al menos una localidad con precio");
            return;
        }

        try {
            Concierto c = elegido();
            int id;
            if (c == null) {
                Map<String, Object> datos = new HashMap<>();
                datos.put("artista", artista.getNombre());
                datos.put("estadio", estadio.getNombre());
                datos.put("gira", gira);
                datos.put("fecha", fecha.toString());
                datos.put("hora", hora);
                datos.put("estado", estado);
                id = conciertoService.crearConcierto(datos).getId();
            } else {
                Map<String, Object> datos = new HashMap<>();
                datos.put("artistaId", artista.getId());
                datos.put("estadioId", estadio.getId());
                datos.put("gira", gira);
                datos.put("fecha", fecha.toString());
                datos.put("hora", hora);
                datos.put("estado", estado);
                conciertoService.actualizarConcierto(c.getId(), datos);
                id = c.getId();
            }

            for (FilaPrecio f : filas) {
                if (f.activa.isSelected()) {
                    precioService.guardarPrecio(id, f.localidad.getId(), leerPrecio(f), f.modoDePie ? "DE_PIE" : "NUMERADA", f.modoDePie ? leerAforo(f) : null);
                } else if (f.teniaPrecio) {
                    precioService.quitarPrecio(id, f.localidad.getId());
                }
            }
            cerrar();
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}

package com.umg.api.ui;

import com.umg.api.Sesion;
import com.umg.api.model.Concierto;
import com.umg.api.model.Mapa;
import com.umg.api.model.ZonaMapa;
import com.umg.api.service.AsientoService;
import com.umg.api.service.ConciertoService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CarteleraController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbArtista;

    @FXML
    private Button btnTodas;

    @FXML
    private Button btn7Dias;

    @FXML
    private Button btn30Dias;

    @FXML
    private Button btnMasAdelante;

    @FXML
    private Label lblCantidad;

    @FXML
    private VBox listaConciertos;

    private static final String TODOS = "Todos los artistas";

    private final ConciertoService conciertoService = new ConciertoService();
    private final AsientoService asientoService = new AsientoService();

    private List<Concierto> conciertos = new ArrayList<>();
    private final Map<Integer, Mapa> mapas = new ConcurrentHashMap<>();
    private Button filtroFecha;

    @FXML
    private void initialize() {
        filtroFecha = btnTodas;
        for (Button b : List.of(btnTodas, btn7Dias, btn30Dias, btnMasAdelante)) {
            b.setOnAction(e -> elegirFiltro(b));
        }
        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> mostrar());
        cmbArtista.setOnAction(e -> mostrar());

        cargar();
    }

    private void cargar() {
        lblCantidad.setText("Cargando conciertos...");
        Fondo.cargar(() -> {
            List<Concierto> lista = conciertoService.getConciertos();
            Map<Integer, Mapa> deCadaUno = new ConcurrentHashMap<>();
            lista.parallelStream().forEach(c -> {
                try {
                    deCadaUno.put(c.getId(), asientoService.getMapa(c.getId()));
                } catch (Exception ex) {
                    System.out.println("Sin mapa para el concierto " + c.getId() + ": " + ex.getMessage());
                }
            });
            mapas.clear();
            mapas.putAll(deCadaUno);
            return lista;
        }, lista -> {
            conciertos = lista;
            llenarFiltros();
        }, ex -> Barra.alerta(Alert.AlertType.ERROR, "No se pudieron cargar los conciertos: " + ex.getMessage()));
    }

    private void llenarFiltros() {
        TreeSet<String> artistas = new TreeSet<>();
        for (Concierto c : conciertos) {
            if (c.getArtista() != null) {
                artistas.add(c.getArtista());
            }
        }
        List<String> opciones = new ArrayList<>();
        opciones.add(TODOS);
        opciones.addAll(artistas);
        cmbArtista.setItems(FXCollections.observableArrayList(opciones));
        cmbArtista.getSelectionModel().select(TODOS);

        mostrar();
    }

    private void elegirFiltro(Button boton) {
        filtroFecha.getStyleClass().remove("seg-on");
        filtroFecha = boton;
        filtroFecha.getStyleClass().add("seg-on");
        mostrar();
    }

    private boolean pasaFiltros(Concierto c) {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        if (!texto.isEmpty()) {
            String todo = (c.getArtista() + " " + c.getGira() + " " + c.getEstadio()).toLowerCase();
            if (!todo.contains(texto)) {
                return false;
            }
        }

        String artista = cmbArtista.getValue();
        if (artista != null && !TODOS.equals(artista) && !artista.equals(c.getArtista())) {
            return false;
        }

        LocalDate fecha = Formato.fecha(c.getFecha());
        LocalDate hoy = LocalDate.now();
        if (fecha == null || filtroFecha == btnTodas) {
            return true;
        }
        if (filtroFecha == btn7Dias) {
            return !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(7));
        }
        if (filtroFecha == btn30Dias) {
            return !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(30));
        }
        return fecha.isAfter(hoy.plusDays(30));
    }

    private void mostrar() {
        listaConciertos.getChildren().clear();

        List<Concierto> visibles = new ArrayList<>();
        for (Concierto c : conciertos) {
            if (pasaFiltros(c)) {
                visibles.add(c);
            }
        }
        visibles.sort(Comparator.comparing(c -> c.getFecha() == null ? "" : c.getFecha()));

        for (Concierto c : visibles) {
            listaConciertos.getChildren().add(crearTarjeta(c));
        }
        lblCantidad.setText(visibles.size() + (visibles.size() == 1 ? " concierto" : " conciertos"));
    }

    private HBox crearTarjeta(Concierto c) {
        Plantilla t = Plantilla.cargar("TarjetaConcierto.fxml");
        LocalDate fecha = Formato.fecha(c.getFecha());

        t.<Label>get("lblDia").setText(Formato.dia(fecha));
        t.<Label>get("lblNumero").setText(fecha == null ? "" : String.valueOf(fecha.getDayOfMonth()));
        t.<Label>get("lblMesHora").setText(Formato.mes(fecha) + " · " + Formato.hora(c.getHora()));
        t.<Label>get("lblArtista").setText(c.getArtista() == null ? "Sin artista" : c.getArtista().toUpperCase());
        t.<Label>get("lblGira").setText(c.getGira() == null || c.getGira().isEmpty() ? "Sin gira" : c.getGira());
        t.<Label>get("lblLugar").setText(c.getEstadio() == null ? "" : c.getEstadio());
        pintarEstado(t, c.getEstado());

        Mapa mapa = mapas.get(c.getId());
        Double desde = null;
        HBox zonas = t.get("boxZonas");
        if (mapa != null) {
            int i = 0;
            for (ZonaMapa z : mapa.getLocalidades()) {
                if (z.getPrecio() == null) {
                    continue;
                }
                if (i < 4) {
                    zonas.getChildren().add(crearMedidor(z, i));
                }
                if (z.getPrecio() != null && (desde == null || z.getPrecio() < desde)) {
                    desde = z.getPrecio();
                }
                i++;
            }
        }

        Label lblDesde = t.get("lblDesde");
        Label lblDesdeTexto = t.get("lblDesdeTexto");
        if (desde != null) {
            lblDesde.setText(Formato.dineroCorto(desde));
        } else {
            lblDesde.setText("Sin precios");
            lblDesdeTexto.setVisible(false);
        }

        Button vender = t.get("btnVender");
        boolean enVenta = "En venta".equalsIgnoreCase(c.getEstado()) && desde != null;
        if (enVenta) {
            vender.setOnAction(e -> abrirMapa(c));
            t.<HBox>get("tarjeta").setOnMouseClicked(e -> abrirMapa(c));
        } else {
            vender.getStyleClass().remove("btn-primary");
            vender.getStyleClass().add("btn-ghost");
            vender.setGraphic(null);
            vender.setText(desde == null ? "Sin precios" : textoNoDisponible(c.getEstado()));
            vender.setDisable(true);
        }
        return t.get("tarjeta");
    }

    private VBox crearMedidor(ZonaMapa z, int indice) {
        Plantilla m = Plantilla.cargar("MedidorZona.fxml");
        int total = z.getCapacidad() > 0 ? z.getCapacidad() : z.getAsientos().size();
        double proporcion = total == 0 ? 0 : (double) z.getDisponibles() / total;

        m.<Label>get("lblZona").setText(z.getNombre());
        Region barra = m.get("barra");
        StackPane pista = (StackPane) barra.getParent();
        barra.prefWidthProperty().bind(pista.widthProperty().multiply(proporcion));
        barra.maxWidthProperty().bind(pista.widthProperty().multiply(proporcion));
        barra.setStyle("-fx-background-color: " + Formato.color(z.getColor(), indice) + ";");
        m.<Label>get("lblDisponibles").setText(String.format("%,d", z.getDisponibles()) + " disp.");
        return (VBox) m.getRaiz();
    }

    private void pintarEstado(Plantilla t, String estado) {
        String color = switch (estado == null ? "" : estado) {
            case "En venta" -> "91,214,138";
            case "Agotado" -> "255,92,138";
            case "Finalizado" -> "140,134,121";
            case "Cancelado" -> "255,106,85";
            default -> "143,163,199";
        };
        t.<HBox>get("pillEstado").setStyle("-fx-background-color: rgba(" + color + ",0.14);");
        t.<Region>get("dotEstado").setStyle("-fx-background-color: rgb(" + color + ");");
        Label lbl = t.get("lblEstado");
        lbl.setText(estado == null ? "Sin estado" : estado);
        lbl.setStyle("-fx-text-fill: rgb(" + color + ");");
    }

    private String textoNoDisponible(String estado) {
        if ("Programado".equalsIgnoreCase(estado)) {
            return "Aún no en venta";
        }
        return estado == null ? "No disponible" : estado;
    }

    private void abrirMapa(Concierto c) {
        Sesion.setConcierto(c);
        Sesion.setVenta(null);
        Barra.ir("MapaAsientos.fxml");
    }
}

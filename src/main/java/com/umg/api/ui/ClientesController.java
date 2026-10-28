package com.umg.api.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.umg.api.service.ClienteService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ClientesController {

    private static final double W_NIT = 130, W_COMPRAS = 90, W_BOL = 90, W_ULTIMA = 140, W_TOT = 120;
    private static final Locale ES = Locale.of("es", "GT");

    @FXML private Label lblResumen, lblTotal;
    @FXML private TextField txtBuscar;
    @FXML private HBox cabecera;
    @FXML private VBox filas;

    private JsonNode clientes;

    @FXML
    private void initialize() {
        txtBuscar.setStyle("-fx-background-color: #1F1E1B; -fx-border-color: #34322D; -fx-border-radius: 12;"
                + " -fx-background-radius: 12; -fx-text-fill: #F2EFE6; -fx-prompt-text-fill: #7C786E; -fx-padding: 0 12 0 12;");
        txtBuscar.textProperty().addListener((o, a, b) -> pintar());

        cabecera.setPadding(new Insets(14, 18, 10, 18));
        cabecera.setStyle("-fx-border-color: transparent transparent #2E2C28 transparent;");
        Label cliente = cab("CLIENTE", -1, Pos.CENTER_LEFT);
        HBox.setHgrow(cliente, Priority.ALWAYS);
        cabecera.getChildren().addAll(
                cliente,
                cab("NIT", W_NIT, Pos.CENTER_LEFT),
                cab("COMPRAS", W_COMPRAS, Pos.CENTER),
                cab("BOLETOS", W_BOL, Pos.CENTER),
                cab("ÚLTIMA COMPRA", W_ULTIMA, Pos.CENTER_LEFT),
                cab("TOTAL", W_TOT, Pos.CENTER_RIGHT));

        lblResumen.setText("Cargando…");
        Fondo.cargar(() -> new ClienteService().listar(), lista -> {
            clientes = lista;
            pintar();
        }, ex -> {
            filas.getChildren().setAll(aviso("No se pudieron cargar los clientes: " + ex.getMessage()));
            lblResumen.setText("Sin conexión");
        });
    }

    private void pintar() {
        if (clientes == null) {
            return;
        }
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        filas.getChildren().clear();
        int cuantos = 0;
        double total = 0;

        for (JsonNode c : clientes) {
            String texto = (txt(c, "nombre", "") + " " + txt(c, "nit", "") + " " + txt(c, "correo", "")).toLowerCase();
            if (!q.isEmpty() && !texto.contains(q)) {
                continue;
            }
            filas.getChildren().add(fila(c));
            cuantos++;
            total += c.path("total").asDouble();
        }

        if (cuantos == 0) {
            filas.getChildren().add(aviso("No hay clientes con esa búsqueda."));
        }
        lblResumen.setText(cuantos + (cuantos == 1 ? " cliente" : " clientes"));
        lblTotal.setText(Formato.dinero(total));
    }

    private Node fila(JsonNode c) {
        String nombre = txt(c, "nombre", "Sin nombre");

        Label iniciales = new Label(Formato.iniciales(nombre));
        iniciales.getStyleClass().addAll("s12", "w700", "c-amber");
        StackPane avatar = new StackPane(iniciales);
        avatar.getStyleClass().add("avatar");
        avatar.setMinSize(34, 34);
        avatar.setPrefSize(34, 34);
        avatar.setMaxSize(34, 34);

        Label lblNombre = new Label(nombre);
        lblNombre.getStyleClass().addAll("s13", "w700");
        Label lblCorreo = new Label(txt(c, "correo", "Sin correo"));
        lblCorreo.getStyleClass().addAll("s11", "c-dim");
        HBox cliente = new HBox(10, avatar, new VBox(2, lblNombre, lblCorreo));
        cliente.setAlignment(Pos.CENTER_LEFT);
        cliente.setMinWidth(220);
        HBox.setHgrow(cliente, Priority.ALWAYS);

        Label nit = new Label(txt(c, "nit", "—"));
        nit.getStyleClass().addAll("s12", "c-muted");
        fijar(nit, W_NIT);

        Label compras = new Label(String.valueOf(c.path("compras").asInt()));
        compras.getStyleClass().addAll("s13", "w700");
        compras.setAlignment(Pos.CENTER);
        fijar(compras, W_COMPRAS);

        Label boletos = new Label(String.valueOf(c.path("boletos").asInt()));
        boletos.getStyleClass().addAll("s13", "c-muted");
        boletos.setAlignment(Pos.CENTER);
        fijar(boletos, W_BOL);

        Label ultima = new Label(fecha(c));
        ultima.getStyleClass().addAll("s12", "c-muted");
        fijar(ultima, W_ULTIMA);

        Label tot = new Label(Formato.dinero(c.path("total").asDouble()));
        tot.getStyleClass().addAll("s13", "w700");
        tot.setAlignment(Pos.CENTER_RIGHT);
        fijar(tot, W_TOT);

        HBox r = new HBox(cliente, nit, compras, boletos, ultima, tot);
        r.setAlignment(Pos.CENTER_LEFT);
        r.setPadding(new Insets(10, 18, 10, 18));
        r.setMinHeight(56);
        r.setStyle(estiloFila(false));
        r.setOnMouseEntered(e -> r.setStyle(estiloFila(true)));
        r.setOnMouseExited(e -> r.setStyle(estiloFila(false)));
        r.setOnMouseClicked(e -> verCompras(c));
        return r;
    }

    private void verCompras(JsonNode c) {
        GestionVentasController.clienteInicial = c.get("id").asInt();
        GestionVentasController.clienteNombreInicial = txt(c, "nombre", "cliente");
        Barra.ir("GestionVentas.fxml");
    }

    private static String estiloFila(boolean hover) {
        return "-fx-cursor: hand; -fx-background-color: " + (hover ? "#252320" : "transparent") + ";"
                + " -fx-border-color: transparent transparent #2A2825 transparent;";
    }

    private static String fecha(JsonNode c) {
        if (!c.hasNonNull("ultimaCompra")) {
            return "Sin compras";
        }
        try {
            return DateTimeFormatter.ofPattern("d MMM yyyy", ES)
                    .format(Instant.parse(c.get("ultimaCompra").asText()).atZone(ZoneId.systemDefault()))
                    .replace(".", "");
        } catch (Exception e) {
            return "—";
        }
    }

    private static Label cab(String texto, double ancho, Pos pos) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("s11", "w600", "c-dim");
        l.setAlignment(pos);
        if (ancho > 0) {
            fijar(l, ancho);
        } else {
            l.setMinWidth(220);
            l.setMaxWidth(Double.MAX_VALUE);
        }
        return l;
    }

    private static Label aviso(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("s12", "c-dim");
        l.setWrapText(true);
        l.setPadding(new Insets(14, 18, 14, 18));
        return l;
    }

    private static void fijar(Region r, double ancho) {
        r.setMinWidth(ancho);
        r.setPrefWidth(ancho);
        r.setMaxWidth(ancho);
    }

    private static String txt(JsonNode n, String campo, String porDefecto) {
        return n.hasNonNull(campo) && !n.get(campo).asText().isBlank() ? n.get(campo).asText() : porDefecto;
    }
}

package com.umg.api.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.umg.api.service.PanelService;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Panel de control: pide GET /api/panel y dibuja tarjetas, barras y listas.
 * Se refresca solo cada 10 segundos mientras la pantalla esta visible.
 */
public class PanelController {

    @FXML private ScrollPane raiz;
    @FXML private Region punto;
    @FXML private Label lblFecha, lblEstado;

    @FXML private Label lblRecaudacion, lblRecaudacionSub;
    @FXML private Label lblBoletos, lblBoletosSub;
    @FXML private Label lblPromedio, lblPromedioSub;
    @FXML private Label lblAforo, lblAforoSub;

    @FXML private FlowPane leyenda;
    @FXML private VBox listaInventario;
    @FXML private VBox listaArtistas;
    @FXML private HBox barras14;
    @FXML private Label lblTotal14;
    @FXML private VBox listaActividad;

    private static final Locale ES = Locale.forLanguageTag("es-GT");
    private final DecimalFormat entero = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));

    private Timeline refresco;
    private boolean cargando;

    @FXML
    private void initialize() {
        lblFecha.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", ES))
                .replace(".", "").toUpperCase(ES));
        estado("Cargando datos…", false);

        refresco = new Timeline(new KeyFrame(Duration.seconds(10), e -> cargar()));
        refresco.setCycleCount(Animation.INDEFINITE);

        // Solo se refresca mientras la pantalla esta en una escena (visible)
        raiz.sceneProperty().addListener((obs, anterior, nueva) -> {
            if (nueva == null) {
                refresco.stop();
            } else {
                cargar();
                refresco.play();
            }
        });
        if (raiz.getScene() != null) {
            cargar();
            refresco.play();
        }
    }

    // ------------------------------------------------------------------ carga

    private void cargar() {
        if (cargando) {
            return;
        }
        cargando = true;

        Task<JsonNode> tarea = new Task<>() {
            @Override
            protected JsonNode call() throws Exception {
                return PanelService.obtener();
            }
        };
        tarea.setOnSucceeded(e -> {
            cargando = false;
            try {
                pintar(tarea.getValue());
                estado("Datos en tiempo real de todas las taquillas", false);
            } catch (Exception ex) {
                ex.printStackTrace();
                estado("No se pudo dibujar el panel: " + ex.getMessage(), true);
            }
        });
        tarea.setOnFailed(e -> {
            cargando = false;
            Throwable ex = tarea.getException();
            ex.printStackTrace();
            estado("Sin datos: " + ex.getMessage(), true);
        });

        Thread hilo = new Thread(tarea, "panel-datos");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void estado(String texto, boolean error) {
        lblEstado.setText(texto);
        lblEstado.getStyleClass().removeAll("c-red", "c-muted");
        lblEstado.getStyleClass().add(error ? "c-red" : "c-muted");
        punto.setStyle("-fx-background-color: " + (error ? "-c-red" : "-c-green") + ";");
    }

    // ----------------------------------------------------------------- dibujo

    private void pintar(JsonNode d) {
        // ---- Tarjetas de arriba ----
        JsonNode hoy = d.path("hoy");
        lblRecaudacion.setText("Q " + entero.format(hoy.path("recaudacion").asDouble()));
        lblRecaudacionSub.setText(hoy.path("transacciones").asInt() + " transacciones");
        lblBoletos.setText(entero.format(hoy.path("boletos").asInt()));
        lblBoletosSub.setText("en " + hoy.path("taquillas").asInt() + " taquillas");
        lblPromedio.setText("Q " + entero.format(hoy.path("promedio").asDouble()));
        lblPromedioSub.setText(String.format(Locale.US, "%.1f boletos por transacción",
                hoy.path("boletosPorVenta").asDouble()));

        JsonNode aforo = d.path("aforo");
        lblAforo.setText(aforo.path("porcentaje").asInt() + "%");
        lblAforoSub.setText(entero.format(aforo.path("vendidos").asInt()) + " de "
                + entero.format(aforo.path("capacidad").asInt()) + " · "
                + aforo.path("eventos").asInt() + " eventos");

        pintarInventario(d.path("inventario"));
        pintarArtistas(d.path("artistas"));
        pintarRecaudacion(d.path("recaudacion14"));
        pintarActividad(d.path("actividad"));
    }

    private void pintarInventario(JsonNode conciertos) {
        listaInventario.getChildren().clear();
        Map<String, String> colores = new LinkedHashMap<>();   // nombre localidad -> color

        boolean primera = true;
        for (JsonNode c : conciertos) {
            VBox izq = new VBox(3,
                    etiqueta(c.path("artista").asText(), "s14", "w700"),
                    etiqueta(fechaCorta(c.path("fecha").asText()) + " · " + c.path("estadio").asText(),
                            "s11", "c-dim"));
            fijarAncho(izq, 190);

            Node barra = barraSegmentada(c.path("localidades"));
            HBox.setHgrow(barra, Priority.ALWAYS);

            VBox der = new VBox(2,
                    etiqueta(c.path("porcentaje").asInt() + "%", "s15", "w800"),
                    etiqueta(entero.format(c.path("vendidos").asInt()) + " / "
                            + entero.format(c.path("capacidad").asInt()), "s11", "c-dim"));
            der.setAlignment(Pos.CENTER_RIGHT);
            fijarAncho(der, 84);

            HBox fila = new HBox(16, izq, barra, der);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(12, 0, 12, 0));
            if (!primera) {
                fila.getStyleClass().add("inv-row");
            }
            primera = false;
            listaInventario.getChildren().add(fila);

            for (JsonNode l : c.path("localidades")) {
                colores.putIfAbsent(l.path("nombre").asText(), l.path("color").asText("#888888"));
            }
        }

        leyenda.getChildren().clear();
        for (Map.Entry<String, String> e : colores.entrySet()) {
            Region cuadro = new Region();
            cuadro.getStyleClass().add("swatch");
            cuadro.setMinSize(9, 9);
            cuadro.setPrefSize(9, 9);
            cuadro.setMaxSize(9, 9);
            cuadro.setStyle("-fx-background-color: " + e.getValue() + ";");
            HBox item = new HBox(6, cuadro, etiqueta(e.getKey(), "s11", "c-muted"));
            item.setAlignment(Pos.CENTER_LEFT);
            leyenda.getChildren().add(item);
        }
    }

    /** Una barra dividida en segmentos (uno por localidad), cada uno con su color. */
    private Node barraSegmentada(JsonNode localidades) {
        HBox caja = new HBox(3);
        caja.setAlignment(Pos.CENTER_LEFT);

        double total = 0;
        int n = 0;
        for (JsonNode l : localidades) {
            total += l.path("capacidad").asDouble();
            n++;
        }
        if (total <= 0) {
            total = 1;
        }

        for (JsonNode l : localidades) {
            double capacidad = l.path("capacidad").asDouble();
            double vendidos = l.path("vendidos").asDouble();
            double parte = capacidad / total;
            double lleno = capacidad > 0 ? Math.min(1.0, vendidos / capacidad) : 0;

            StackPane celda = new StackPane();
            celda.getStyleClass().add("meter-track");
            celda.setAlignment(Pos.CENTER_LEFT);
            celda.setMinHeight(14);
            celda.setPrefHeight(14);
            celda.setMaxHeight(14);
            celda.prefWidthProperty().bind(caja.widthProperty().subtract(3.0 * Math.max(0, n - 1)).multiply(parte));

            Region relleno = new Region();
            relleno.getStyleClass().add("meter-fill");
            relleno.setStyle("-fx-background-color: " + l.path("color").asText("#888888") + ";");
            relleno.maxWidthProperty().bind(celda.widthProperty().multiply(lleno));
            StackPane.setAlignment(relleno, Pos.CENTER_LEFT);

            celda.getChildren().add(relleno);
            caja.getChildren().add(celda);
        }
        return caja;
    }

    private void pintarArtistas(JsonNode artistas) {
        listaArtistas.getChildren().clear();

        double max = 1;
        for (JsonNode a : artistas) {
            max = Math.max(max, a.path("boletos").asDouble());
        }

        int posicion = 1;
        for (JsonNode a : artistas) {
            if (posicion > 6) {
                break;
            }
            Label rango = etiqueta(String.valueOf(posicion), "disp", "s24", "c-dim");
            rango.setMinWidth(26);

            HBox nombre = new HBox(6,
                    etiqueta(a.path("artista").asText(), "s13", "w700"),
                    etiqueta(a.path("genero").asText(""), "s13", "c-muted"));
            nombre.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(nombre, Priority.ALWAYS);

            HBox arriba = new HBox(8, nombre, etiqueta(entero.format(a.path("boletos").asInt()), "s13", "w700"));
            arriba.setAlignment(Pos.CENTER_LEFT);

            StackPane medidor = barraSimple(a.path("boletos").asDouble() / max,
                    posicion == 1 ? "fill-amber" : "fill-muted");

            VBox centro = new VBox(6, arriba, medidor);
            HBox.setHgrow(centro, Priority.ALWAYS);

            HBox fila = new HBox(12, rango, centro);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(0, 0, 14, 0));
            listaArtistas.getChildren().add(fila);
            posicion++;
        }
    }

    private StackPane barraSimple(double proporcion, String claseColor) {
        StackPane pista = new StackPane();
        pista.getStyleClass().add("meter-track");
        pista.setAlignment(Pos.CENTER_LEFT);
        pista.setMinHeight(6);
        pista.setPrefHeight(6);
        pista.setMaxHeight(6);

        Region relleno = new Region();
        relleno.getStyleClass().addAll("meter-fill", claseColor);
        relleno.maxWidthProperty().bind(pista.widthProperty().multiply(Math.max(0, Math.min(1, proporcion))));
        StackPane.setAlignment(relleno, Pos.CENTER_LEFT);

        pista.getChildren().add(relleno);
        return pista;
    }

    private void pintarRecaudacion(JsonNode dias) {
        barras14.getChildren().clear();

        double max = 1;
        double suma = 0;
        int total = 0;
        for (JsonNode d : dias) {
            max = Math.max(max, d.path("total").asDouble());
            suma += d.path("total").asDouble();
            total++;
        }
        lblTotal14.setText("Q " + entero.format(suma));

        int i = 0;
        for (JsonNode d : dias) {
            double altura = Math.max(6, d.path("total").asDouble() / max * 130);

            Region barra = new Region();
            barra.getStyleClass().add("trend-bar");
            if (i == total - 1) {
                barra.getStyleClass().add("trend-today");
            }
            barra.setMinHeight(altura);
            barra.setPrefHeight(altura);
            barra.setMaxHeight(altura);

            Label dia = etiqueta(String.valueOf(LocalDate.parse(d.path("dia").asText()).getDayOfMonth()),
                    "s11", "c-dim");
            dia.setMaxWidth(Double.MAX_VALUE);
            dia.setAlignment(Pos.CENTER);

            VBox columna = new VBox(8, barra, dia);
            columna.setAlignment(Pos.BOTTOM_CENTER);
            columna.setPrefWidth(10);
            columna.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(columna, Priority.ALWAYS);

            barras14.getChildren().add(columna);
            i++;
        }
    }

    private void pintarActividad(JsonNode ventas) {
        listaActividad.getChildren().clear();
        String hoy = LocalDate.now().toString();

        for (JsonNode v : ventas) {
            boolean esHoy = hoy.equals(v.path("fecha").asText());

            Region dot = new Region();
            dot.getStyleClass().add("dot");
            dot.setMinSize(7, 7);
            dot.setPrefSize(7, 7);
            dot.setMaxSize(7, 7);
            dot.setStyle("-fx-background-color: " + (esHoy ? "-c-green" : "#57534B") + ";");

            Label hora = etiqueta(v.path("hora").asText(), "s12", "c-dim");
            hora.setMinWidth(40);

            int boletos = v.path("boletos").asInt();
            String artista = v.path("artista").asText("");
            String detalle = " · " + boletos + (boletos == 1 ? " boleto" : " boletos")
                    + (artista.isBlank() ? "" : " · " + artista);
            Label lblDetalle = etiqueta(detalle, "s13", "c-muted");
            lblDetalle.setMinWidth(0);

            HBox texto = new HBox(etiqueta(v.path("vendedor").asText(), "s13", "w700"), lblDetalle);
            texto.setMinWidth(0);
            HBox.setHgrow(texto, Priority.ALWAYS);

            Label monto = etiqueta("Q " + entero.format(v.path("total").asDouble()), "s13", "w700");
            monto.setMinWidth(Region.USE_PREF_SIZE);

            HBox fila = new HBox(10, dot, hora, texto, monto);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(0, 0, 14, 0));
            listaActividad.getChildren().add(fila);
        }
    }

    // ---------------------------------------------------------------- ayudas

    private Label etiqueta(String texto, String... clases) {
        Label l = new Label(texto);
        l.getStyleClass().addAll(clases);
        return l;
    }

    private void fijarAncho(Region r, double ancho) {
        r.setMinWidth(ancho);
        r.setPrefWidth(ancho);
        r.setMaxWidth(ancho);
    }

    /** "2026-10-02T00:00:00.000Z" -> "2 oct". Si ves un dia de menos, cambia UTC por America/Guatemala. */
    private String fechaCorta(String iso) {
        try {
            return DateTimeFormatter.ofPattern("d MMM", ES)
                    .format(Instant.parse(iso).atZone(ZoneOffset.UTC))
                    .replace(".", "");
        } catch (Exception e) {
            return iso;
        }
    }
}

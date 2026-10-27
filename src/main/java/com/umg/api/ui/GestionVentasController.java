package com.umg.api.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.umg.api.Sesion;
import com.umg.api.service.FacturaService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;

/**
 * Gestión de ventas: tabla de ventas + panel que se desliza desde la derecha con el ticket
 * (boletos con QR) arriba y la factura abajo. La factura NO se arma aquí: se baja de Cloudflare
 * (Java -> API en Render -> Cloudflare) como XML y se dibuja con Jasper.
 */
public class GestionVentasController {

    private static final String TODOS = "Todos los vendedores";
    private static final double ANCHO_PANEL = 480;
    private static final double W_NO = 96, W_FECHA = 110, W_LOC = 190, W_BOL = 70, W_MET = 100, W_VEN = 150, W_TOT = 110;
    private static final Locale ES = Locale.of("es", "GT");

    @FXML private StackPane raiz;
    @FXML private Label lblTitulo, lblCeja, lblResumen, lblTotalGeneral;
    @FXML private TextField txtBuscar;
    @FXML private HBox boxRango, cabecera;
    @FXML private ComboBox<String> cmbVendedor;
    @FXML private ScrollPane scroll;
    @FXML private VBox filas, panel;
    @FXML private Pane velo;

    private final FacturaService servicio = new FacturaService();
    private final Map<Integer, Node> filaPorVenta = new HashMap<>();
    private ToggleButton tgHoy, tg7, tgTodo;
    private JsonNode ventas;
    private boolean admin;
    private boolean ajustando;

    public static Integer usuarioInicial;
    public static String nombreInicial;
    private Integer filtroUsuario;

    // panel
    private Label pTitulo, pSub;
    private VBox cuerpo;
    private Button btnImprimir;
    private TranslateTransition animPanel;
    private boolean panelAbierto;
    private int ventaMostrada = -1;
    private Vista vistaActual;
    private String numeroActual = "factura";

    private static final class Vista {
        final JasperPrint jp;
        final List<WritableImage> paginas;

        Vista(JasperPrint jp, List<WritableImage> paginas) {
            this.jp = jp;
            this.paginas = paginas;
        }
    }

    // =====================================================================
    //  Inicio
    // =====================================================================

    @FXML
    private void initialize() {
        admin = Sesion.esAdmin();
        lblTitulo.setText(admin ? "GESTIÓN DE VENTAS" : "MIS VENTAS");
        lblCeja.setText(admin ? "TODAS LAS TAQUILLAS · VENTAS Y DETALLE" : "MI TAQUILLA · VENTAS Y DETALLE");

        String campo = "-fx-background-color: #1F1E1B; -fx-border-color: #34322D; -fx-border-radius: 12;"
                + " -fx-background-radius: 12; -fx-text-fill: #F2EFE6; -fx-prompt-text-fill: #7C786E; -fx-padding: 0 12 0 12;";
        txtBuscar.setStyle(campo);
        cmbVendedor.setStyle(campo);
        cmbVendedor.setVisible(admin);
        cmbVendedor.setManaged(admin);
        cmbVendedor.getItems().add(TODOS);
        cmbVendedor.setValue(TODOS);

        filtroUsuario = usuarioInicial;
        String nombreFiltro = nombreInicial;
        usuarioInicial = null;
        nombreInicial = null;
        if (admin && filtroUsuario != null) {
            lblTitulo.setText("VENTAS DE " + nombreFiltro.toUpperCase());
            lblCeja.setText("USUARIOS · HISTORIAL DE VENTAS");
            cmbVendedor.setVisible(false);
            cmbVendedor.setManaged(false);
        }

        // Rango: Hoy / 7 días / Todo
        ToggleGroup grupo = new ToggleGroup();
        tgHoy = botonRango("Hoy", grupo);
        tg7 = botonRango("7 días", grupo);
        tgTodo = botonRango("Todo", grupo);
        boxRango.getChildren().addAll(tgHoy, tg7, tgTodo);
        boxRango.setPadding(new Insets(4));
        boxRango.setStyle("-fx-background-color: #1F1E1B; -fx-border-color: #34322D; -fx-border-radius: 12; -fx-background-radius: 12;");
        if (admin && filtroUsuario != null) {
            tgTodo.setSelected(true);
        } else {
            tgHoy.setSelected(true);
        }
        grupo.selectedToggleProperty().addListener((o, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                cargar();
            }
        });

        txtBuscar.textProperty().addListener((o, a, b) -> pintar());
        cmbVendedor.valueProperty().addListener((o, a, b) -> {
            if (!ajustando) {
                pintar();
            }
        });

        armarCabecera();
        armarPanel();

        velo.setOnMouseClicked(e -> cerrarPanel());
        raiz.sceneProperty().addListener((o, a, escena) -> {
            if (escena != null) {
                escena.setOnKeyPressed(e -> {
                    if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE && panelAbierto) {
                        cerrarPanel();
                    }
                });
            }
        });

        cargar();
    }

    private ToggleButton botonRango(String texto, ToggleGroup grupo) {
        ToggleButton b = new ToggleButton(texto);
        b.setToggleGroup(grupo);
        b.setFocusTraversable(false);
        Runnable estilo = () -> b.setStyle(b.isSelected()
                ? "-fx-background-color: #3A3833; -fx-text-fill: #F2EFE6; -fx-background-radius: 8; -fx-padding: 7 14 7 14; -fx-font-weight: bold;"
                : "-fx-background-color: transparent; -fx-text-fill: #A9A59B; -fx-background-radius: 8; -fx-padding: 7 14 7 14;");
        b.selectedProperty().addListener((o, a, n) -> estilo.run());
        estilo.run();
        return b;
    }

    // =====================================================================
    //  Tabla
    // =====================================================================

    private void armarCabecera() {
        cabecera.setPadding(new Insets(14, 18, 10, 18));
        cabecera.setStyle("-fx-border-color: transparent transparent #2E2C28 transparent;");
        Label concierto = cab("CONCIERTO", -1, Pos.CENTER_LEFT);
        HBox.setHgrow(concierto, Priority.ALWAYS);
        cabecera.getChildren().addAll(
                cab("NO.", W_NO, Pos.CENTER_LEFT),
                cab("FECHA", W_FECHA, Pos.CENTER_LEFT),
                concierto,
                cab("LOCALIDADES", W_LOC, Pos.CENTER_LEFT),
                cab("BOLETOS", W_BOL, Pos.CENTER_RIGHT),
                cab("MÉTODO", W_MET, Pos.CENTER_LEFT),
                cab("VENDEDOR", W_VEN, Pos.CENTER_LEFT),
                cab("TOTAL", W_TOT, Pos.CENTER_RIGHT));
    }

    private Label cab(String texto, double ancho, Pos pos) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("s11", "w600", "c-dim");
        l.setAlignment(pos);
        if (ancho > 0) {
            fijar(l, ancho);
        } else {
            l.setMinWidth(180);
            l.setMaxWidth(Double.MAX_VALUE);
        }
        return l;
    }

    private void cargar() {
        String rango = tg7.isSelected() ? "7" : tgTodo.isSelected() ? "todo" : "hoy";
        Integer usuarioId = admin ? filtroUsuario : Integer.valueOf(Sesion.getUsuario().getId());
        lblResumen.setText("Cargando…");
        Fondo.cargar(() -> servicio.listarVentas(rango, usuarioId), lista -> {
            ventas = lista;
            actualizarVendedores();
            pintar();
        }, ex -> {
            filas.getChildren().setAll(aviso("No se pudieron cargar las ventas: " + ex.getMessage()));
            lblResumen.setText("Sin conexión");
        });
    }

    private void actualizarVendedores() {
        if (!admin || ventas == null) {
            return;
        }
        TreeSet<String> nombres = new TreeSet<>();
        for (JsonNode v : ventas) {
            String n = txt(v, "vendedor", "");
            if (!n.isEmpty()) {
                nombres.add(n);
            }
        }
        String elegido = cmbVendedor.getValue();
        ajustando = true;
        cmbVendedor.getItems().setAll(TODOS);
        cmbVendedor.getItems().addAll(nombres);
        cmbVendedor.setValue(elegido != null && nombres.contains(elegido) ? elegido : TODOS);
        ajustando = false;
    }

    private void pintar() {
        if (ventas == null) {
            return;
        }
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String vendedor = cmbVendedor.getValue();
        filas.getChildren().clear();
        filaPorVenta.clear();
        int cuantas = 0, boletos = 0;
        double total = 0;

        for (JsonNode v : ventas) {
            if (admin && vendedor != null && !TODOS.equals(vendedor) && !vendedor.equals(txt(v, "vendedor", ""))) {
                continue;
            }
            if (!q.isEmpty()) {
                String h = (codigo(v.get("ventaId").asInt()) + " " + txt(v, "artista", "") + " "
                        + txt(v, "cliente", "") + " " + txt(v, "vendedor", "")).toLowerCase();
                if (!h.contains(q)) {
                    continue;
                }
            }
            Node fila = fila(v);
            filaPorVenta.put(v.get("ventaId").asInt(), fila);
            filas.getChildren().add(fila);
            cuantas++;
            boletos += v.path("boletos").asInt();
            total += v.path("total").asDouble();
        }

        if (cuantas == 0) {
            filas.getChildren().add(aviso("No hay ventas con estos filtros."));
        }
        lblResumen.setText(cuantas + " ventas · " + boletos + " boletos");
        lblTotalGeneral.setText(dinero(total));
        marcarSeleccion();
    }

    private Node fila(JsonNode v) {
        int ventaId = v.get("ventaId").asInt();

        Label no = new Label(codigo(ventaId));
        no.getStyleClass().addAll("s12", "w700");
        fijar(no, W_NO);

        ZonedDateTime fecha = fecha(v.get("fecha"));
        Label lblFecha = new Label(fecha == null ? "—" : fechaTabla(fecha));
        lblFecha.getStyleClass().addAll("s12", "c-muted");
        fijar(lblFecha, W_FECHA);

        Label artista = new Label(txt(v, "artista", "—"));
        artista.getStyleClass().addAll("s13", "w700");
        ZonedDateTime fc = fecha(v.get("fechaConcierto"));
        Label sub = new Label(fc == null ? txt(v, "cliente", "") : fechaCorta(fc) + " · " + txt(v, "cliente", ""));
        sub.getStyleClass().addAll("s11", "c-dim");
        VBox concierto = new VBox(2, artista, sub);
        concierto.setMinWidth(180);
        HBox.setHgrow(concierto, Priority.ALWAYS);

        FlowPane locs = new FlowPane(4, 4);
        for (JsonNode l : v.path("localidades")) {
            locs.getChildren().add(chip(l.path("cantidad").asInt() + " " + l.path("nombre").asText()));
        }
        fijar(locs, W_LOC);

        Label bol = new Label(String.valueOf(v.path("boletos").asInt()));
        bol.getStyleClass().addAll("s13", "w700");
        bol.setAlignment(Pos.CENTER_RIGHT);
        fijar(bol, W_BOL);

        Label met = new Label(metodo(txt(v, "metodoPago", "")));
        met.getStyleClass().addAll("s12", "c-muted");
        fijar(met, W_MET);

        Label ven = new Label(txt(v, "vendedor", "—"));
        ven.getStyleClass().addAll("s12", "c-muted");
        fijar(ven, W_VEN);

        Label tot = new Label(dinero(v.path("total").asDouble()));
        tot.getStyleClass().addAll("s13", "w700");
        tot.setAlignment(Pos.CENTER_RIGHT);
        fijar(tot, W_TOT);

        HBox r = new HBox(no, lblFecha, concierto, locs, bol, met, ven, tot);
        r.setAlignment(Pos.CENTER_LEFT);
        r.setPadding(new Insets(12, 18, 12, 18));
        r.setMinHeight(56);
        r.setStyle(estiloFila(false, false));
        r.setOnMouseEntered(e -> r.setStyle(estiloFila(ventaId == ventaMostrada && panelAbierto, true)));
        r.setOnMouseExited(e -> r.setStyle(estiloFila(ventaId == ventaMostrada && panelAbierto, false)));
        r.setOnMouseClicked(e -> abrir(v));
        return r;
    }

    private static String estiloFila(boolean seleccionada, boolean hover) {
        String fondo = seleccionada ? "#2A2825" : hover ? "#252320" : "transparent";
        return "-fx-cursor: hand; -fx-background-color: " + fondo + ";"
                + " -fx-border-color: transparent transparent #2A2825 transparent;";
    }

    private void marcarSeleccion() {
        filaPorVenta.forEach((id, nodo) -> nodo.setStyle(estiloFila(id == ventaMostrada && panelAbierto, false)));
    }

    private Node chip(String texto) {
        Circle punto = new Circle(4, javafx.scene.paint.Color.web(colorLocalidad(texto)));
        Label l = new Label(texto);
        l.getStyleClass().addAll("s11", "w600");
        HBox c = new HBox(6, punto, l);
        c.setAlignment(Pos.CENTER_LEFT);
        c.setPadding(new Insets(3, 8, 3, 8));
        c.setStyle("-fx-background-color: #2A2825; -fx-background-radius: 6;");
        return c;
    }

    private static String colorLocalidad(String t) {
        String k = t.toLowerCase();
        if (k.contains("platea")) return "#3CC6E8";
        if (k.contains("balc")) return "#4ADE80";
        if (k.contains("vip")) return "#F4577A";
        String[] paleta = {"#F5B544", "#A78BFA", "#60A5FA", "#FB923C"};
        return paleta[Math.abs(k.hashCode()) % paleta.length];
    }

    // =====================================================================
    //  Panel deslizante
    // =====================================================================

    private void armarPanel() {
        panel.setStyle("-fx-background-color: #1B1A17; -fx-border-color: transparent transparent transparent #34322D;"
                + " -fx-border-width: 0 0 0 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 40, 0, -8, 0);");
        panel.setTranslateX(ANCHO_PANEL + 40);

        Label ceja = new Label("DETALLE DE VENTA");
        ceja.getStyleClass().add("eyebrow");
        pTitulo = new Label();
        pTitulo.getStyleClass().addAll("disp", "s28");
        pSub = new Label();
        pSub.getStyleClass().addAll("s12", "c-dim");
        Button cerrar = new Button("✕");
        cerrar.getStyleClass().add("btn-ghost");
        cerrar.setOnAction(e -> cerrarPanel());
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cab = new HBox(new VBox(2, ceja, pTitulo, pSub), espacio, cerrar);
        cab.setAlignment(Pos.TOP_LEFT);
        cab.setPadding(new Insets(20, 24, 16, 24));

        cuerpo = new VBox(14);
        cuerpo.setPadding(new Insets(16, 24, 20, 24));
        ScrollPane sp = new ScrollPane(cuerpo);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        btnImprimir = new Button("Imprimir factura");
        btnImprimir.getStyleClass().addAll("btn-primary", "btn-lg");
        btnImprimir.setMaxWidth(Double.MAX_VALUE);
        btnImprimir.setDisable(true);
        btnImprimir.setOnAction(e -> ventanaFactura());
        HBox pie = new HBox(btnImprimir);
        HBox.setHgrow(btnImprimir, Priority.ALWAYS);
        pie.setPadding(new Insets(14, 24, 18, 24));
        pie.setStyle("-fx-border-color: #2E2C28 transparent transparent transparent;");

        panel.getChildren().addAll(cab, linea(), sp, pie);
    }

    private static Region linea() {
        Region l = new Region();
        l.setMinHeight(1);
        l.setMaxHeight(1);
        l.setStyle("-fx-background-color: #2E2C28;");
        return l;
    }

    private void abrir(JsonNode fila) {
        int ventaId = fila.get("ventaId").asInt();
        ventaMostrada = ventaId;
        vistaActual = null;
        pTitulo.setText(codigo(ventaId));
        ZonedDateTime f = fecha(fila.get("fecha"));
        pSub.setText(f == null ? "" : fechaLarga(f));
        btnImprimir.setDisable(true);
        cuerpo.getChildren().setAll(aviso("Cargando venta…"));
        deslizar(true);
        marcarSeleccion();

        Fondo.cargar(() -> servicio.detalleVenta(ventaId), d -> {
            if (ventaMostrada == ventaId) {
                pintarDetalle(d);
            }
        }, ex -> {
            if (ventaMostrada == ventaId) {
                cuerpo.getChildren().setAll(aviso("No se pudo cargar la venta: " + ex.getMessage()));
            }
        });
    }

    private void cerrarPanel() {
        deslizar(false);
        marcarSeleccion();
    }

    private void deslizar(boolean abrir) {
        if (abrir && panelAbierto) {
            return; // ya está abierto: solo cambia el contenido, sin volver a animar
        }
        panelAbierto = abrir;
        if (animPanel != null) {
            animPanel.stop();
        }
        if (abrir) {
            velo.setVisible(true);
            panel.setVisible(true);
            velo.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(220), velo);
            ft.setToValue(1);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(220), velo);
            ft.setToValue(0);
            ft.play();
        }
        animPanel = new TranslateTransition(Duration.millis(260), panel);
        animPanel.setInterpolator(Interpolator.EASE_BOTH);
        animPanel.setToX(abrir ? 0 : ANCHO_PANEL + 40);
        if (!abrir) {
            animPanel.setOnFinished(e -> {
                panel.setVisible(false);
                velo.setVisible(false);
            });
        }
        animPanel.play();
    }

    private void pintarDetalle(JsonNode d) {
        int ventaId = d.get("ventaId").asInt();
        cuerpo.getChildren().clear();

        cuerpo.getChildren().add(seccion("TICKET"));
        for (JsonNode b : d.path("boletos")) {
            cuerpo.getChildren().add(ticket(ventaId, b));
        }

        cuerpo.getChildren().add(seccion("FACTURA"));
        VBox zona = new VBox(10);
        cuerpo.getChildren().add(zona);

        if (d.path("facturaId").isNull() || d.path("facturaId").isMissingNode()) {
            zona.getChildren().add(aviso("Esta venta es anterior al sistema de facturas y no tiene factura."));
            return;
        }
        int facturaId = d.get("facturaId").asInt();
        numeroActual = txt(d, "numeroFactura", "factura");

        if (!d.path("xmlEnNube").asBoolean()) {
            Button generar = new Button("Generar factura");
            generar.getStyleClass().addAll("btn-ghost");
            generar.setOnAction(e -> {
                generar.setDisable(true);
                zona.getChildren().setAll(aviso("Generando factura…"));
                Fondo.cargar(() -> {
                    servicio.generarYGuardar(ventaId);
                    return Boolean.TRUE;
                }, ok -> {
                    if (ventaMostrada == ventaId) {
                        cargarFactura(ventaId, facturaId, zona);
                    }
                }, ex -> zona.getChildren().setAll(aviso("No se pudo generar la factura: " + ex.getMessage())));
            });
            zona.getChildren().addAll(aviso("La factura todavía no está en la nube."), generar);
            return;
        }
        cargarFactura(ventaId, facturaId, zona);
    }

    /** Java -> API (Render) -> Cloudflare: baja el XML y lo dibuja. No se vuelve a llenar el reporte. */
    private void cargarFactura(int ventaId, int facturaId, VBox zona) {
        zona.getChildren().setAll(aviso("Descargando factura…"));
        Fondo.cargar(() -> {
            byte[] xml = servicio.bajarXml(facturaId);
            if (xml == null) {
                throw new Exception("La factura todavía no está en la nube");
            }
            JasperPrint jp = FacturaService.cargarXml(xml);
            List<WritableImage> paginas = new ArrayList<>();
            for (int i = 0; i < jp.getPages().size(); i++) {
                paginas.add(render(jp, i, 1.6f));
            }
            return new Vista(jp, paginas);
        }, vista -> {
            if (ventaMostrada != ventaId) {
                return;
            }
            vistaActual = vista;
            zona.getChildren().clear();
            for (WritableImage img : vista.paginas) {
                zona.getChildren().add(pagina(img, ANCHO_PANEL - 48));
            }
            btnImprimir.setDisable(false);
        }, ex -> {
            if (ventaMostrada == ventaId) {
                zona.getChildren().setAll(aviso("No se pudo mostrar la factura: " + ex.getMessage()));
            }
        });
    }

    private Node ticket(int ventaId, JsonNode b) {
        Label artista = new Label(txt(b, "artista", "Concierto"));
        artista.getStyleClass().addAll("s14", "w700");
        artista.setWrapText(true);

        ZonedDateTime fc = fecha(b.get("fecha"));
        Label cuando = new Label(fc == null ? "" : fechaLarga(fc));
        cuando.getStyleClass().addAll("s11", "c-dim");

        String lugar = b.path("dePie").asBoolean()
                ? "De pie · sin asiento"
                : (b.hasNonNull("bloque") ? "Bloque " + b.get("bloque").asText() + " · " : "")
                + "Fila " + b.path("fila").asText() + " · Asiento " + b.path("numero").asText();
        Label sitio = new Label(lugar);
        sitio.getStyleClass().addAll("s12", "c-muted");

        Label precio = new Label(dinero(b.path("precio").asDouble()));
        precio.getStyleClass().addAll("s14", "w700");

        VBox info = new VBox(4, chip(txt(b, "localidad", "General")), artista, cuando, sitio, precio);
        HBox.setHgrow(info, Priority.ALWAYS);

        int boletoId = b.get("id").asInt();
        ImageView qr = new ImageView(qr("SVB|V" + ventaId + "|B" + boletoId, 220));
        qr.setFitWidth(96);
        qr.setFitHeight(96);
        StackPane marco = new StackPane(qr);
        marco.setPadding(new Insets(5));
        marco.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        marco.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        HBox tarjeta = new HBox(14, info, marco);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(14));
        tarjeta.setStyle("-fx-background-color: #2A2825; -fx-border-color: #3A3833;"
                + " -fx-background-radius: 12; -fx-border-radius: 12;");
        return tarjeta;
    }

    // =====================================================================
    //  Ventana de la factura (modal: hasta cerrar con la X no se toca nada más)
    // =====================================================================

    private void ventanaFactura() {
        Vista vista = vistaActual;
        if (vista == null) {
            return;
        }
        String nombrePdf = numeroActual + ".pdf";

        VBox paginas = new VBox(14);
        paginas.setPadding(new Insets(18));
        paginas.setAlignment(Pos.TOP_CENTER);
        for (WritableImage img : vista.paginas) {
            paginas.getChildren().add(pagina(img, 640));
        }
        ScrollPane sp = new ScrollPane(paginas);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #14130F; -fx-background-color: #14130F;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        Label estado = new Label("Factura " + numeroActual);
        estado.getStyleClass().addAll("s12", "c-dim");
        Button guardar = new Button("Guardar PDF…");
        guardar.getStyleClass().add("btn-ghost");
        Button impresora = new Button("Enviar a impresora");
        impresora.getStyleClass().add("btn-primary");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(10, estado, espacio, guardar, impresora);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12, 18, 12, 18));

        VBox contenido = new VBox(barra, sp);
        contenido.getStyleClass().add("app");

        Stage ventana = new Stage();
        ventana.initOwner(raiz.getScene().getWindow());
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Factura " + numeroActual);
        Scene escena = new Scene(contenido, 720, 880);
        escena.getStylesheets().addAll(raiz.getScene().getStylesheets());
        ventana.setScene(escena);

        guardar.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName(nombrePdf);
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File destino = fc.showSaveDialog(ventana);
            if (destino == null) {
                return;
            }
            estado.setText("Generando PDF…");
            Fondo.cargar(() -> {
                Files.write(destino.toPath(), FacturaService.pdf(vista.jp));
                return destino;
            }, f -> estado.setText("PDF guardado en " + f.getName()),
                    ex -> estado.setText("No se pudo guardar: " + ex.getMessage()));
        });

        impresora.setOnAction(e -> {
            estado.setText("Enviando a la impresora…");
            Fondo.cargar(() -> {
                JasperPrintManager.printReport(vista.jp, true);
                return Boolean.TRUE;
            }, ok -> estado.setText("Listo"),
                    ex -> estado.setText("No se pudo imprimir: " + ex.getMessage()));
        });

        ventana.show();
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private static Node pagina(WritableImage img, double ancho) {
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitWidth(ancho);
        StackPane marco = new StackPane(iv);
        marco.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        marco.setStyle("-fx-background-color: white; -fx-background-radius: 6;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0, 0, 4);");
        return marco;
    }

    private static WritableImage render(JasperPrint jp, int pagina, float zoom) throws Exception {
        java.awt.Image awt = JasperPrintManager.printPageToImage(jp, pagina, zoom);
        BufferedImage bi;
        if (awt instanceof BufferedImage) {
            bi = (BufferedImage) awt;
        } else {
            bi = new BufferedImage(awt.getWidth(null), awt.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(awt, 0, 0, null);
        }
        return aFx(bi);
    }

    private static WritableImage aFx(BufferedImage bi) {
        int w = bi.getWidth(), h = bi.getHeight();
        int[] px = bi.getRGB(0, 0, w, h, null, 0, w);
        WritableImage out = new WritableImage(w, h);
        out.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), px, 0, w);
        return out;
    }

    private static WritableImage qr(String texto, int lado) {
        try {
            BitMatrix m = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, lado, lado,
                    Map.of(EncodeHintType.MARGIN, 0));
            int w = m.getWidth(), h = m.getHeight();
            int[] px = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    px[y * w + x] = m.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            WritableImage out = new WritableImage(w, h);
            out.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), px, 0, w);
            return out;
        } catch (Exception e) {
            return new WritableImage(1, 1);
        }
    }

    private static Label seccion(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("s11", "w600", "c-dim");
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
        return n.hasNonNull(campo) ? n.get(campo).asText() : porDefecto;
    }

    private static String codigo(int ventaId) {
        return "V-" + String.format("%06d", ventaId);
    }

    private static String dinero(double v) {
        return String.format(Locale.US, "Q %,.2f", v);
    }

    private static String metodo(String m) {
        String k = m.toLowerCase();
        if (k.contains("tarjeta") || k.equals("pos")) return "POS";
        if (k.contains("efectivo")) return "Efectivo";
        return m.isEmpty() ? "—" : m.substring(0, 1).toUpperCase() + m.substring(1).toLowerCase();
    }

    private static ZonedDateTime fecha(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) {
            return null;
        }
        String s = n.asText();
        ZoneId zona = ZoneId.systemDefault();
        try { return Instant.parse(s).atZone(zona); } catch (Exception e) { }
        try { return OffsetDateTime.parse(s).atZoneSameInstant(zona); } catch (Exception e) { }
        try { return LocalDateTime.parse(s).atZone(zona); } catch (Exception e) { }
        try { return LocalDate.parse(s).atStartOfDay(zona); } catch (Exception e) { }
        return null;
    }

    private static String fechaLarga(ZonedDateTime z) {
        return DateTimeFormatter.ofPattern("EEE d MMM yyyy · HH:mm", ES).format(z).replace(".", "");
    }

    private static String fechaCorta(ZonedDateTime z) {
        return DateTimeFormatter.ofPattern("d MMM yyyy", ES).format(z).replace(".", "");
    }

    private static String fechaTabla(ZonedDateTime z) {
        if (z.toLocalDate().equals(LocalDate.now())) {
            return "Hoy · " + DateTimeFormatter.ofPattern("HH:mm").format(z);
        }
        return DateTimeFormatter.ofPattern("d MMM · HH:mm", ES).format(z).replace(".", "");
    }
}

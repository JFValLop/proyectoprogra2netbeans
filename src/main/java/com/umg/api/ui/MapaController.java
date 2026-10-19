package com.umg.api.ui;

import com.umg.api.App;
import com.umg.api.Sesion;
import com.umg.api.model.AsientoMapa;
import com.umg.api.model.Concierto;
import com.umg.api.model.Mapa;
import com.umg.api.model.Orden;
import com.umg.api.model.ZonaMapa;
import com.umg.api.service.AsientoService;
import com.umg.api.service.RetencionService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MapaController {

    @FXML
    private Button btnVolver;

    @FXML
    private Label lblArtista;

    @FXML
    private Label lblGira;

    @FXML
    private Label lblDetalle;

    @FXML
    private Button btnModoA;

    @FXML
    private Button btnModoB;

    @FXML
    private Button btnModoC;

    @FXML
    private Label lblActualizado;

    @FXML
    private HBox boxLeyenda;

    @FXML
    private VBox mapaAsientos;

    @FXML
    private Label lblInfoAsiento;

    @FXML
    private Label lblInfoPrecio;

    @FXML
    private Region infoColor;

    @FXML
    private Label lblOrdenNumero;

    @FXML
    private Button btnVaciar;

    @FXML
    private VBox boxHold;

    @FXML
    private Label lblTiempo;

    @FXML
    private Region barraTiempo;

    @FXML
    private VBox lineasOrden;

    @FXML
    private Label lblBoletos;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnCobrar;

    private static final int MINUTOS_RETENCION = 5;
    private static final double ANCHO_BARRA = 236;

    private final AsientoService asientoService = new AsientoService();
    private final RetencionService retencionService = new RetencionService();

    private Concierto concierto;
    private Mapa mapa;
    private final Set<Integer> seleccion = new LinkedHashSet<>();
    private final Map<Integer, AsientoMapa> asientos = new HashMap<>();
    private final Map<Integer, ZonaMapa> zonaDe = new HashMap<>();
    private final Map<Integer, Integer> indiceZona = new HashMap<>();
    private final Map<Integer, Integer> cantidades = new LinkedHashMap<>();
    private final Map<Integer, ZonaMapa> zonasDePie = new HashMap<>();
    private final Map<Integer, Integer> indiceDePie = new HashMap<>();
    private Instant expira;
    private Timeline refresco;
    private Timeline reloj;
    private int cambios;
    private boolean primeraCarga = true;

    @FXML
    private void initialize() {
        concierto = Sesion.getConcierto();
        btnModoB.setDisable(true);
        btnModoC.setDisable(true);

        App.alSalir(() -> {
            detener();
            liberar();
        });
        btnVolver.setOnAction(e -> Barra.ir("Cartelera.fxml"));
        btnVaciar.setOnAction(e -> {
            seleccion.clear();
            cantidades.clear();
            redibujar();
            retenerEnFondo(this::cargarMapa);
        });
        btnCobrar.setOnAction(e -> cobrar());
        btnCobrar.sceneProperty().addListener((obs, vieja, escena) -> {
            if (escena != null) {
                escena.getAccelerators().put(new KeyCodeCombination(KeyCode.F9), btnCobrar::fire);
            }
        });

        lblArtista.setText(concierto.getArtista() == null ? "" : concierto.getArtista().toUpperCase());
        lblGira.setText(concierto.getGira() == null ? "" : concierto.getGira());
        lblDetalle.setText(Formato.fechaLarga(concierto.getFecha(), concierto.getHora())
                + (concierto.getEstadio() == null ? "" : " · " + concierto.getEstadio()));
        lblOrdenNumero.setText("Orden provisional");
        lblInfoAsiento.setText("Selecciona un asiento en el plano");
        lblInfoPrecio.setText("");
        infoColor.setVisible(false);

        cargarMapa();

        refresco = new Timeline(new KeyFrame(javafx.util.Duration.seconds(10), e -> cargarMapa()));
        refresco.setCycleCount(Animation.INDEFINITE);
        refresco.play();
        reloj = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> actualizarReloj()));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
    }

    private int usuarioId() {
        return Sesion.getUsuario().getId();
    }

    private void cargarMapa() {
        int version = cambios;
        Fondo.cargar(() -> asientoService.getMapa(concierto.getId()), nuevo -> {
            if (version == cambios) {
                aplicarMapa(nuevo);
            }
        }, ex -> lblActualizado.setText("Sin conexión con el servidor"));
    }

    private void redibujar() {
        cambios++;
        dibujarMapa();
        dibujarOrden();
    }

    private void aplicarMapa(Mapa nuevo) {
        mapa = nuevo;
        asientos.clear();
        zonaDe.clear();
        indiceZona.clear();
        zonasDePie.clear();
        indiceDePie.clear();
        seleccion.clear();
        int i = 0;
        for (ZonaMapa z : mapa.getLocalidades()) {
            if (z.esDePie()) {
                zonasDePie.put(z.getId(), z);
                indiceDePie.put(z.getId(), i);
            }
            for (AsientoMapa a : z.getAsientos()) {
                asientos.put(a.getId(), a);
                zonaDe.put(a.getId(), z);
                indiceZona.put(a.getId(), i);
                if ("RETENIDO".equals(a.getEstado()) && a.getRetenidoPor() != null && a.getRetenidoPor() == usuarioId()) {
                    seleccion.add(a.getId());
                }
            }
            i++;
        }
        if (seleccion.isEmpty()) {
            expira = null;
        }
        for (Integer id : new ArrayList<>(cantidades.keySet())) {
            ZonaMapa z = zonasDePie.get(id);
            int maximo = z == null || z.getPrecio() == null ? 0 : z.getDisponibles();
            if (maximo <= 0) {
                cantidades.remove(id);
            } else if (cantidades.get(id) > maximo) {
                cantidades.put(id, maximo);
            }
        }

        dibujarLeyenda();
        dibujarMapa();
        dibujarOrden();
        lblActualizado.setText("Actualizado " + LocalTime.now().withNano(0));
        if (primeraCarga) {
            primeraCarga = false;
            if (!seleccion.isEmpty()) {
                retenerEnFondo(() -> {
                });
            }
        }
    }

    private String colorZona(int asientoId) {
        ZonaMapa z = zonaDe.get(asientoId);
        return Formato.color(z.getColor(), indiceZona.get(asientoId));
    }

    private void dibujarLeyenda() {
        boxLeyenda.getChildren().clear();
        int i = 0;
        for (ZonaMapa z : mapa.getLocalidades()) {
            if (z.getPrecio() == null) {
                i++;
                continue;
            }
            Plantilla p = Plantilla.cargar("LeyendaZona.fxml");
            p.<Region>get("color").setStyle("-fx-background-color: " + Formato.color(z.getColor(), i) + ";");
            p.<Label>get("lblZona").setText(z.getNombre());
            p.<Label>get("lblPrecio").setText(z.getPrecio() == null ? "Sin precio" : Formato.dineroCorto(z.getPrecio()));
            p.<Label>get("lblDisponibles").setText(String.format("%,d", z.getDisponibles()) + " disp.");
            if (z.esDePie()) {
                p.<Button>get("boton").setOnAction(e -> agregarDePie(z));
            }
            boxLeyenda.getChildren().add(p.getRaiz());
            i++;
        }
    }

    private void dibujarMapa() {
        mapaAsientos.getChildren().remove(1, mapaAsientos.getChildren().size());
        Node escenario = mapaAsientos.getChildren().get(0);
        escenario.setVisible(false);
        escenario.setManaged(false);

        if (mapa.getLocalidades().isEmpty()) {
            Label vacio = new Label("Este recinto todavía no tiene localidades");
            vacio.getStyleClass().addAll("s13", "c-dim");
            mapaAsientos.getChildren().add(vacio);
            return;
        }

        Map<Integer, Map<String, AsientoMapa>> lugares = new HashMap<>();
        for (ZonaMapa z : mapa.getLocalidades()) {
            Map<String, AsientoMapa> deZona = new HashMap<>();
            for (AsientoMapa a : z.getAsientos()) {
                deZona.put(a.getBloque() + "|" + a.getFila() + "|" + a.getNumero(), a);
            }
            lugares.put(z.getId(), deZona);
        }

        Pane plano = PlanoRecinto.dibujar(mapa.getTipo(), mapa.getLocalidades(), new PlanoRecinto.Pintor() {
            @Override
            public void asiento(Button boton, ZonaMapa zona, String bloque, int fila, int numero) {
                AsientoMapa a = lugares.get(zona.getId()).get(bloque + "|" + PlanoRecinto.FILAS.charAt(fila) + "|" + numero);
                pintarAsiento(boton, a, zona, numero);
            }

            @Override
            public void zona(VBox caja, ZonaMapa zona, boolean compacta) {
                pintarZona(caja, zona, compacta);
            }

            @Override
            public double opacidad(ZonaMapa zona) {
                return zona.getPrecio() == null ? 0.4 : 1;
            }
        });
        double escala = Math.min(1, Math.min(700 / plano.getPrefWidth(), 530 / plano.getPrefHeight()));
        plano.setScaleX(escala);
        plano.setScaleY(escala);
        mapaAsientos.getChildren().add(new Group(plano));
    }

    private void pintarAsiento(Button b, AsientoMapa a, ZonaMapa z, int numero) {
        if (a == null || z.getPrecio() == null) {
            b.setStyle("-fx-background-color: #2A2824;");
            b.setCursor(Cursor.DEFAULT);
            return;
        }
        if (seleccion.contains(a.getId())) {
            b.getStyleClass().add("seat-sel");
            b.setText(String.valueOf(numero));
        } else if ("VENDIDO".equals(a.getEstado())) {
            b.getStyleClass().add("seat-sold");
        } else if ("RETENIDO".equals(a.getEstado())) {
            b.getStyleClass().add("seat-held");
        } else {
            b.setStyle("-fx-background-color: " + z.getColor() + ";");
        }
        b.setTooltip(new Tooltip(descripcion(a) + " · " + Formato.dineroCorto(z.getPrecio())));
        b.setOnAction(e -> clic(a, z));
    }

    private void pintarZona(VBox caja, ZonaMapa z, boolean compacta) {
        Label sub = new Label();
        sub.getStyleClass().addAll("s12", "w600", "c-text2");
        caja.getChildren().add(sub);
        if (z.getPrecio() == null) {
            sub.setText("No disponible");
            return;
        }
        int cantidad = cantidades.getOrDefault(z.getId(), 0);
        int libres = Math.max(0, z.getDisponibles() - cantidad);
        sub.setText(Formato.dineroCorto(z.getPrecio()) + " · " + String.format("%,d", libres) + (compacta ? " disp." : " disponibles")
                + (compacta && cantidad > 0 ? " · " + cantidad + " en tu orden" : ""));
        if (cantidad > 0 && !compacta) {
            Label pastilla = new Label("× " + cantidad + " en tu orden");
            pastilla.setStyle("-fx-background-color: #FFB020; -fx-text-fill: #1C1404; -fx-font-size: 12px; -fx-font-weight: 700; -fx-background-radius: 99; -fx-padding: 4 10;");
            caja.getChildren().add(pastilla);
        }
        if (!compacta) {
            Label pista = new Label("Clic para agregar un boleto");
            pista.getStyleClass().addAll("s11", "c-dim");
            caja.getChildren().add(pista);
        }
        caja.setCursor(Cursor.HAND);
        caja.setOnMouseClicked(e -> {
            agregarDePie(z);
            e.consume();
        });
    }

    private String colorDePie(int localidadId) {
        ZonaMapa z = zonasDePie.get(localidadId);
        return Formato.color(z.getColor(), indiceDePie.get(localidadId));
    }

    private void mostrarInfoDePie(ZonaMapa z, String estado) {
        lblInfoAsiento.setText(z.getNombre() + " · De pie");
        lblInfoPrecio.setText(estado + (z.getPrecio() == null ? "" : " · " + Formato.dineroCorto(z.getPrecio())));
        infoColor.setStyle("-fx-background-color: " + colorDePie(z.getId()) + ";");
        infoColor.setVisible(true);
    }

    private void agregarDePie(ZonaMapa z) {
        if (z.getPrecio() == null) {
            mostrarInfoDePie(z, "Sin precio para este concierto");
            return;
        }
        int cantidad = cantidades.getOrDefault(z.getId(), 0);
        if (cantidad >= z.getDisponibles()) {
            mostrarInfoDePie(z, "Agotado");
            return;
        }
        cantidades.put(z.getId(), cantidad + 1);
        mostrarInfoDePie(z, (cantidad + 1) + " en tu orden");
        redibujar();
    }

    private void quitarDePie(ZonaMapa z) {
        int cantidad = cantidades.getOrDefault(z.getId(), 0) - 1;
        if (cantidad <= 0) {
            cantidades.remove(z.getId());
        } else {
            cantidades.put(z.getId(), cantidad);
        }
        redibujar();
    }

    private int boletos() {
        int total = seleccion.size();
        for (int c : cantidades.values()) {
            total += c;
        }
        return total;
    }

    private String descripcion(AsientoMapa a) {
        ZonaMapa z = zonaDe.get(a.getId());
        return z.getNombre() + " · Bloque " + a.getBloque() + " · Fila " + a.getFila() + " · Asiento " + a.getNumero();
    }

    private void mostrarInfo(AsientoMapa a, String estado) {
        lblInfoAsiento.setText(descripcion(a));
        ZonaMapa z = zonaDe.get(a.getId());
        lblInfoPrecio.setText(estado + (z.getPrecio() == null ? "" : " · " + Formato.dineroCorto(z.getPrecio())));
        infoColor.setStyle("-fx-background-color: " + colorZona(a.getId()) + ";");
        infoColor.setVisible(true);
    }

    private void clic(AsientoMapa a, ZonaMapa z) {
        if (z.getPrecio() == null) {
            mostrarInfo(a, "Sin precio para este concierto");
            return;
        }
        if ("VENDIDO".equals(a.getEstado())) {
            mostrarInfo(a, "Vendido");
            return;
        }
        if ("RETENIDO".equals(a.getEstado()) && !seleccion.contains(a.getId())) {
            mostrarInfo(a, "En otra taquilla");
            return;
        }

        boolean agregar = !seleccion.contains(a.getId());
        if (agregar) {
            seleccion.add(a.getId());
        } else {
            seleccion.remove(a.getId());
        }
        mostrarInfo(a, agregar ? "En tu orden" : "Libre");
        redibujar();
        retenerEnFondo(this::cargarMapa);
    }

    private void retenerEnFondo(Runnable despues) {
        List<Integer> lista = new ArrayList<>(seleccion);
        int usuario = usuarioId();
        int conciertoId = concierto.getId();
        Fondo.cargar(() -> retencionService.retener(conciertoId, usuario, lista), hasta -> {
            expira = hasta == null ? null : Instant.parse(hasta);
            despues.run();
        }, ex -> {
            Barra.alerta(Alert.AlertType.WARNING, ex.getMessage());
            despues.run();
        });
    }

    private void liberar() {
        if (!seleccion.isEmpty()) {
            seleccion.clear();
            retenerEnFondo(() -> {
            });
        }
    }

    private double total() {
        double total = 0;
        for (int id : seleccion) {
            ZonaMapa z = zonaDe.get(id);
            if (z != null && z.getPrecio() != null) {
                total += z.getPrecio();
            }
        }
        for (Map.Entry<Integer, Integer> c : cantidades.entrySet()) {
            ZonaMapa z = zonasDePie.get(c.getKey());
            if (z != null && z.getPrecio() != null) {
                total += z.getPrecio() * c.getValue();
            }
        }
        return total;
    }

    private void dibujarOrden() {
        lineasOrden.getChildren().clear();

        Map<ZonaMapa, List<AsientoMapa>> porZona = new LinkedHashMap<>();
        for (int id : seleccion) {
            porZona.computeIfAbsent(zonaDe.get(id), z -> new ArrayList<>()).add(asientos.get(id));
        }

        for (Map.Entry<ZonaMapa, List<AsientoMapa>> e : porZona.entrySet()) {
            ZonaMapa z = e.getKey();
            List<AsientoMapa> lista = e.getValue();
            double precio = z.getPrecio() == null ? 0 : z.getPrecio();

            Plantilla linea = Plantilla.cargar("LineaOrden.fxml");
            linea.<Region>get("color").setStyle("-fx-background-color: " + colorZona(lista.get(0).getId()) + ";");
            linea.<Label>get("lblZona").setText(z.getNombre());
            linea.<Label>get("lblCantidad").setText(lista.size() + (lista.size() == 1 ? " asiento × " : " asientos × ") + Formato.dineroCorto(precio));
            linea.<Label>get("lblSubtotal").setText(Formato.dinero(precio * lista.size()));

            FlowPane chips = linea.get("chips");
            for (AsientoMapa a : lista) {
                Plantilla chip = Plantilla.cargar("ChipAsiento.fxml");
                chip.<Label>get("lblAsiento").setText(a.getBloque() + "·" + a.getFila() + a.getNumero());
                chip.<Button>get("btnQuitar").setOnAction(ev -> clic(a, z));
                chips.getChildren().add(chip.getRaiz());
            }
            lineasOrden.getChildren().add(linea.getRaiz());
        }

        for (Map.Entry<Integer, Integer> c : cantidades.entrySet()) {
            ZonaMapa z = zonasDePie.get(c.getKey());
            double precio = z.getPrecio() == null ? 0 : z.getPrecio();
            int cantidad = c.getValue();

            Plantilla linea = Plantilla.cargar("LineaOrden.fxml");
            linea.<Region>get("color").setStyle("-fx-background-color: " + colorDePie(z.getId()) + ";");
            linea.<Label>get("lblZona").setText(z.getNombre());
            linea.<Label>get("lblCantidad").setText(cantidad + (cantidad == 1 ? " boleto × " : " boletos × ") + Formato.dineroCorto(precio));
            linea.<Label>get("lblSubtotal").setText(Formato.dinero(precio * cantidad));

            Plantilla chip = Plantilla.cargar("ChipAsiento.fxml");
            chip.<Label>get("lblAsiento").setText("De pie ×" + cantidad);
            chip.<Button>get("btnQuitar").setOnAction(ev -> quitarDePie(z));
            linea.<FlowPane>get("chips").getChildren().add(chip.getRaiz());
            lineasOrden.getChildren().add(linea.getRaiz());
        }

        boolean vacia = seleccion.isEmpty() && cantidades.isEmpty();
        boxHold.setVisible(!seleccion.isEmpty());
        boxHold.setManaged(!seleccion.isEmpty());
        lblBoletos.setText(String.valueOf(boletos()));
        lblTotal.setText(Formato.dinero(total()));
        btnCobrar.setDisable(vacia);
        btnVaciar.setDisable(vacia);
        actualizarReloj();
    }

    private void actualizarReloj() {
        if (expira == null || seleccion.isEmpty()) {
            lblTiempo.setText(MINUTOS_RETENCION + ":00");
            barraTiempo.setPrefWidth(ANCHO_BARRA);
            barraTiempo.setMaxWidth(ANCHO_BARRA);
            return;
        }
        long segundos = Math.max(0, Duration.between(Instant.now(), expira).getSeconds());
        lblTiempo.setText(segundos / 60 + ":" + String.format("%02d", segundos % 60));
        double pista = ((Region) barraTiempo.getParent()).getWidth();
        double ancho = (pista > 0 ? pista : ANCHO_BARRA) * segundos / (MINUTOS_RETENCION * 60.0);
        barraTiempo.setPrefWidth(ancho);
        barraTiempo.setMaxWidth(ancho);
        if (segundos == 0) {
            expira = null;
            cargarMapa();
        }
    }

    private void cobrar() {
        if (seleccion.isEmpty() && cantidades.isEmpty()) {
            return;
        }
        List<AsientoMapa> elegidos = new ArrayList<>();
        Map<Integer, ZonaMapa> zonas = new HashMap<>();
        for (int id : seleccion) {
            elegidos.add(asientos.get(id));
            zonas.put(id, zonaDe.get(id));
        }

        Orden orden = new Orden();
        orden.setConcierto(concierto);
        orden.setAsientos(elegidos);
        orden.setZonas(zonas);
        Map<Integer, ZonaMapa> dePie = new HashMap<>();
        for (Integer id : cantidades.keySet()) {
            dePie.put(id, zonasDePie.get(id));
        }
        orden.setCantidades(new LinkedHashMap<>(cantidades));
        orden.setZonasDePie(dePie);
        orden.setTotal(total());
        Sesion.setOrden(orden);
        Sesion.setVenta(null);

        refresco.pause();
        try {
            App.abrirDialogo("Cobro.fxml", "Cobro");
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, ex.getMessage());
        }

        if (Sesion.getVenta() != null) {
            detener();
            seleccion.clear();
            Barra.ir("Confirmacion.fxml");
        } else {
            refresco.play();
            cargarMapa();
        }
    }

    private void detener() {
        if (refresco != null) {
            refresco.stop();
        }
        if (reloj != null) {
            reloj.stop();
        }
    }
}

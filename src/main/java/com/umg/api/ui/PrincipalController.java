package com.umg.api.ui;

import com.umg.api.App;
import com.umg.api.Sesion;
import com.umg.api.controller.Artistas_Controller;
import com.umg.api.model.ResumenVentas;
import com.umg.api.model.Usuario;
import com.umg.api.model.Venta;
import com.umg.api.service.Usuario_Service;
import com.umg.api.service.VentaService;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PrincipalController {

    @FXML
    private Button btnPanel;

    @FXML
    private Button btnPuntoVenta;

    @FXML
    private Button btnValidar;

    @FXML
    private Button btnHistorial;
    
    @FXML
    private VBox avisos;

    @FXML
    private Button btnReportes;

    @FXML
    private VBox grupoCatalogos;

    @FXML
    private Button btnArtistas;

    @FXML
    private Button btnConciertos;

    @FXML
    private Button btnLocalidades;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Label lblTurnoTitulo;

    @FXML
    private Label lblTurnoTotal;

    @FXML
    private Label lblTurnoDetalle;

    @FXML
    private Label lblIniciales;

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblRol;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private StackPane contenido;

    private static final String CATALOGO = "/Recursos/Artistas.fxml";

    private String actual;
    
    private Integer ultimaVenta;

    @FXML
    private void initialize() {
        Usuario u = Sesion.getUsuario();
        boolean admin = Sesion.esAdmin();
        lblIniciales.setText(Formato.iniciales(u.toString()));
        lblNombre.setText(u.toString());
        lblRol.setText(Formato.rol(u.getRol()));
        lblTurnoTitulo.setText(admin ? "HOY · TODAS" : "MI TURNO · HOY");

        btnPanel.setVisible(admin);
        btnPanel.setManaged(admin);
        btnReportes.setVisible(admin);
        btnReportes.setManaged(admin);
        grupoCatalogos.setVisible(admin);
        grupoCatalogos.setManaged(admin);
        btnHistorial.setText(admin ? "Historial de ventas" : "Mis ventas");

        btnPuntoVenta.setOnAction(e -> Barra.ir("Cartelera.fxml"));
        btnLocalidades.setOnAction(e -> Barra.ir("Recintos.fxml"));
        btnValidar.setOnAction(e -> Barra.ir("Validar.fxml"));
        btnPanel.setOnAction(e -> Barra.ir("Panel.fxml"));
        btnConciertos.setOnAction(e -> catalogo("CONCIERTOS"));
        btnHistorial.setText(admin ? "Gestión de ventas" : "Mis ventas");
        btnHistorial.setOnAction(e -> Barra.ir("GestionVentas.fxml"));        btnReportes.setOnAction(e -> Barra.ir("Reportes.fxml")); 
        btnArtistas.setOnAction(e -> vacia(btnArtistas, "CATÁLOGO", "ARTISTAS"));
        btnArtistas.setOnAction(e -> catalogo("ARTISTAS"));
        btnUsuarios.setOnAction(e -> catalogo("USUARIOS"));
        btnCerrarSesion.setOnAction(e -> Barra.cerrarSesion());

        actualizarTurno();
        
        revisarVentas();
        avisarConexion();
        Timeline revisar = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            revisarVentas();
            avisarConexion();
        }));
        revisar.setCycleCount(Animation.INDEFINITE);
        revisar.play();
    }

    public void mostrar(Node pantalla, String nombre) {
        actual = nombre;
        contenido.getChildren().setAll(pantalla);
        if (nombre.equals("Recintos.fxml")) {
            marcar(btnLocalidades);
        } 
         else if (nombre.equals("Panel.fxml")) {
        marcar(btnPanel);
        }
        else if (nombre.equals("Reportes.fxml")) {
        marcar(btnReportes);
        }
        else if (nombre.equals("GestionVentas.fxml")) {
        marcar(btnHistorial);
        }
        else if (nombre.equals("Validar.fxml")) {
        marcar(btnValidar);
        }
        
        else if (nombre.equals(CATALOGO)) {
            String vista = Artistas_Controller.vistaInicial;
            marcar(vista.equals("CONCIERTOS") ? btnConciertos : vista.equals("USUARIOS") ? btnUsuarios : btnArtistas);
        } else if (nombre.endsWith(".fxml")) {
            marcar(btnPuntoVenta);
        }
    }

    public void actualizarTurno() {
        Integer usuarioId = Sesion.esAdmin() ? null : Sesion.getUsuario().getId();
        Fondo.cargar(() -> new VentaService().getResumenHoy(usuarioId), r -> {
            lblTurnoTotal.setText(Formato.dineroCorto(r.getTotalMonto()));
            lblTurnoDetalle.setText(r.getTotalVentas() + " ventas · " + r.getTotalBoletos() + " boletos");
        }, ex -> {
            lblTurnoTotal.setText("-");
            lblTurnoDetalle.setText("Sin conexión");
        });
    }
    
        private void revisarVentas() {
        if (Sesion.getUsuario() == null) {
            return;
        }
        Fondo.cargar(() -> new VentaService().getResumenHoy(null), r -> avisarVentasNuevas(r), ex -> {
        });
    }

    private void avisarConexion() {
        if (Sesion.getUsuario() == null) {
            return;
        }
        int id = Sesion.getUsuario().getId();
        Fondo.cargar(() -> {
            new Usuario_Service().marcarConectado(id, Sesion.estaAusente());
            return true;
        }, ok -> {
        }, ex -> {
        });
    }

    private void avisarVentasNuevas(ResumenVentas resumen) {
        String miNombre = Sesion.getUsuario().toString();
        int masNueva = 0;

        for (Venta v : resumen.getVentas()) {
            boolean esNueva = ultimaVenta != null && v.getId() > ultimaVenta;
            boolean esMia = miNombre.equals(v.getVendedor());

            if (esNueva && !esMia) {
                String texto = v.getVendedor() + " vendió " + v.getBoletos().size() + " boletos · " + Formato.dinero(v.getTotal());
                notificar(texto, "info");
            }

            if (v.getId() > masNueva) {
                masNueva = v.getId();
            }
        }

        ultimaVenta = masNueva;
    }

    private void marcar(Button boton) {
        for (Button b : List.of(btnPanel, btnPuntoVenta, btnValidar, btnHistorial, btnReportes, btnArtistas, btnConciertos, btnLocalidades, btnUsuarios)) {
            b.getStyleClass().remove("nav-active");
            Node icono = b.getGraphic();
            if (icono != null) {
                icono.getStyleClass().removeAll("ic-amber", "ic-text2");
                icono.getStyleClass().add(b == boton ? "ic-amber" : "ic-text2");
            }
        }
        boton.getStyleClass().add("nav-active");
    }

    private void vacia(Button boton, String ceja, String titulo) {
        Label lblCeja = new Label(ceja);
        lblCeja.getStyleClass().add("eyebrow");
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().addAll("disp", "s34");
        VBox pantalla = new VBox(6, lblCeja, lblTitulo);
        pantalla.setPadding(new Insets(22, 28, 16, 28));
        App.mostrar(pantalla, titulo);
        marcar(boton);
    }
    
    public void notificar(String texto, String tipo) {
        Region icono = new Region();
        if (tipo.equals("ok")) {
            icono.getStyleClass().addAll("icon", "ic-check", "ic-green");
        } else if (tipo.equals("info")) {
            icono.getStyleClass().addAll("icon", "ic-info", "ic-amber");
        } else {
            icono.getStyleClass().addAll("icon", "ic-error", "ic-red");
        }
        icono.setMinSize(20, 20);
        icono.setPrefSize(20, 20);
        icono.setMaxSize(20, 20);

        Label mensaje = new Label(texto);
        mensaje.getStyleClass().addAll("s13", "w500");
        mensaje.setWrapText(true);

        HBox tarjeta = new HBox(10, icono, mensaje);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(12, 14, 12, 14));
        tarjeta.setMinWidth(280);
        tarjeta.setMaxWidth(440);
        tarjeta.setStyle("-fx-background-color: #2A2825; -fx-border-color: #3A3833;"
                + " -fx-background-radius: 12; -fx-border-radius: 12;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 32, 0, 0, 12);");

        avisos.getChildren().add(tarjeta);
        if (avisos.getChildren().size() > 4) {
            avisos.getChildren().remove(0);
        }
        FadeTransition desvanecer = new FadeTransition(Duration.millis(500), tarjeta);
        desvanecer.setFromValue(1);
        desvanecer.setToValue(0);
        desvanecer.setOnFinished(e -> avisos.getChildren().remove(tarjeta));

        PauseTransition espera = new PauseTransition(Duration.seconds(4));
        espera.setOnFinished(e -> desvanecer.play());
        espera.play();
    }

    private void catalogo(String vista) {
        Artistas_Controller.vistaInicial = vista;
        Barra.ir(CATALOGO);
    }
}

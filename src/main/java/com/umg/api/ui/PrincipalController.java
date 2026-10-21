package com.umg.api.ui;

import com.umg.api.App;
import com.umg.api.Sesion;
import com.umg.api.controller.Artistas_Controller;
import com.umg.api.model.Usuario;
import com.umg.api.service.VentaService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PrincipalController {

    @FXML
    private Button btnPanel;

    @FXML
    private Button btnPuntoVenta;

    @FXML
    private Button btnHistorial;

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
        btnConciertos.setOnAction(e -> catalogo("CONCIERTOS"));
        btnPanel.setOnAction(e -> vacia(btnPanel, "OPERACIÓN", "PANEL DE CONTROL"));
        btnHistorial.setOnAction(e -> vacia(btnHistorial, "OPERACIÓN", btnHistorial.getText().toUpperCase()));
        btnReportes.setOnAction(e -> vacia(btnReportes, "OPERACIÓN", "REPORTES"));
        btnArtistas.setOnAction(e -> catalogo("ARTISTAS"));
        btnUsuarios.setOnAction(e -> catalogo("USUARIOS"));
        btnCerrarSesion.setOnAction(e -> Barra.cerrarSesion());

        actualizarTurno();
    }

    public void mostrar(Node pantalla, String nombre) {
        actual = nombre;
        contenido.getChildren().setAll(pantalla);
        if (nombre.equals("Recintos.fxml")) {
            marcar(btnLocalidades);
        } else if (nombre.equals(CATALOGO)) {
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

    private void marcar(Button boton) {
        for (Button b : List.of(btnPanel, btnPuntoVenta, btnHistorial, btnReportes, btnArtistas, btnConciertos, btnLocalidades, btnUsuarios)) {
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

    private void catalogo(String vista) {
        Artistas_Controller.vistaInicial = vista;
        Barra.ir(CATALOGO);
    }
}

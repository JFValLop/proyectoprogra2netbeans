package com.umg.api.ui;

import com.umg.api.model.Estadio;
import com.umg.api.service.EstadioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecintoFormController {

    public static String tipoNuevo;
    public static Estadio recintoEditar;
    public static Integer guardado;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblTipo;

    @FXML
    private Button btnCerrar;

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<String> cmbPais;

    @FXML
    private TextField txtCiudad;

    @FXML
    private TextField txtDireccion;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    private final EstadioService estadioService = new EstadioService();
    private Estadio editando;
    private String tipo;

    @FXML
    private void initialize() {
        editando = recintoEditar;
        tipo = tipoNuevo;
        recintoEditar = null;
        tipoNuevo = null;
        guardado = null;

        cmbPais.setItems(FXCollections.observableArrayList(Paises.nombres()));
        cmbPais.setCellFactory(lista -> new Paises.Celda());
        cmbPais.setButtonCell(new Paises.Celda());
        cmbPais.setOnKeyTyped(e -> buscarPais(e.getCharacter()));

        if (editando != null) {
            lblTitulo.setText("EDITAR RECINTO");
            lblTipo.setText("Plano tipo " + Estadio.nombreTipo(editando.getTipo()));
            txtNombre.setText(editando.getNombre());
            cmbPais.setValue(editando.getPais() == null ? "Guatemala" : editando.getPais());
            txtCiudad.setText(editando.getCiudad());
            txtDireccion.setText(editando.getDireccion());
        } else {
            lblTitulo.setText("NUEVO RECINTO");
            lblTipo.setText("Plano tipo " + Estadio.nombreTipo(tipo));
            cmbPais.setValue("Guatemala");
        }

        btnCerrar.setOnAction(e -> cerrar());
        btnCancelar.setOnAction(e -> cerrar());
        btnGuardar.setOnAction(e -> guardar());
    }

    private void buscarPais(String letra) {
        if (letra == null || letra.isBlank()) {
            return;
        }
        for (String pais : cmbPais.getItems()) {
            if (pais.toLowerCase().startsWith(letra.toLowerCase())) {
                cmbPais.setValue(pais);
                if (cmbPais.isShowing()) {
                    cmbPais.hide();
                    cmbPais.show();
                }
                return;
            }
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String pais = cmbPais.getValue();
        String ciudad = txtCiudad.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty() || pais == null || ciudad.isEmpty() || direccion.isEmpty()) {
            lblMensaje.setText("Llena el nombre, el país, la ciudad y la dirección");
            return;
        }

        btnGuardar.setDisable(true);
        lblMensaje.setText("");
        Fondo.cargar(() -> {
            if (editando != null) {
                estadioService.actualizarEstadio(editando.getId(), nombre, pais, ciudad, direccion);
                return editando.getId();
            }
            return estadioService.crearEstadio(nombre, tipo, pais, ciudad, direccion).getId();
        }, id -> {
            guardado = id;
            cerrar();
        }, ex -> {
            btnGuardar.setDisable(false);
            lblMensaje.setText(ex.getMessage());
        });
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}

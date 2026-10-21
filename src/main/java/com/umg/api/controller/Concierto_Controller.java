
package com.umg.api.controller;

import com.umg.api.model.Artista;
import com.umg.api.model.Estadio;
import com.umg.api.service.Artista_Service;
import com.umg.api.service.Estadio_Service;
import com.umg.api.service.Concierto_Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Concierto_Controller {

    @FXML private ComboBox<Artista> cmbArtista;
    @FXML private ComboBox<Estadio> cmbEstadio;
    @FXML private ComboBox<String> cmbEstado;

    @FXML private TextField txtGira;
    @FXML private TextField txtHora;
    @FXML private DatePicker dpFecha;

    @FXML private Label Capacidad;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button cerrar;

    private final Artista_Service artistaService =
            new Artista_Service();

    private final Estadio_Service estadioService =
            new Estadio_Service();

    private final Concierto_Service conciertoService =
            new Concierto_Service();

    private Runnable alGuardar;

    @FXML
    public void initialize() {
        configurarCombos();

        btnGuardar.setOnAction(e -> guardarConcierto());
        btnCancelar.setOnAction(e -> cerrarVentana());
        cerrar.setOnAction(e -> cerrarVentana());

        cargarDatos();
    }
    

    public void setAlGuardar(Runnable alGuardar) {
        this.alGuardar = alGuardar;
    }

    private void configurarCombos() {
        cmbArtista.setCellFactory(lista -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Artista artista, boolean empty) {
                super.updateItem(artista, empty);
                setText(empty || artista == null
                        ? null : artista.getNombre());
            }
        });

        cmbArtista.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Artista artista, boolean empty) {
                super.updateItem(artista, empty);
                setText(empty || artista == null
                        ? null : artista.getNombre());
            }
        });

        cmbEstadio.setCellFactory(lista -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Estadio estadio, boolean empty) {
                super.updateItem(estadio, empty);
                setText(empty || estadio == null
                        ? null : estadio.getNombre());
            }
        });

        cmbEstadio.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Estadio estadio, boolean empty) {
                super.updateItem(estadio, empty);
                setText(empty || estadio == null
                        ? null : estadio.getNombre());
            }
        });

        cmbEstadio.valueProperty().addListener(
                (observable, anterior, actual) -> actualizarCapacidad(actual)
        );
    }

    private void cargarDatos() {
        try {
            List<Artista> artistas =
                    artistaService.obtenerArtistas();

            List<Estadio> estadios =
                    estadioService.obtenerEstadios();

            cmbArtista.setItems(
                    FXCollections.observableArrayList(artistas)
            );

            cmbEstadio.setItems(
                    FXCollections.observableArrayList(estadios)
            );

            if (!artistas.isEmpty()) {
                cmbArtista.getSelectionModel().selectFirst();
            }

            if (!estadios.isEmpty()) {
                cmbEstadio.getSelectionModel().selectFirst();
            }

            if (cmbEstado.getItems().isEmpty()) {
                cmbEstado.getItems().addAll(
                        "En venta",
                        "Programado",
                        "Agotado",
                        "Finalizado",
                        "Cancelado"
                );
            }

            cmbEstado.getSelectionModel().select("En venta");

        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al cargar datos",
                    "No se pudieron cargar los artistas o estadios.\n"
                    + e.getMessage()
            );
        }
    }

    private void actualizarCapacidad(Estadio estadio) {
        if (estadio == null) {
            Capacidad.setText("Sin seleccionar");
            return;
        }

        Capacidad.setText(
                String.format("%,d personas", estadio.getCapacidad())
        );
    }

    private void guardarConcierto() {
        String gira = txtGira.getText().trim();
        Artista artista = cmbArtista.getValue();
        Estadio estadio = cmbEstadio.getValue();
        LocalDate fecha = dpFecha.getValue();
        String hora = txtHora.getText().trim();
        String estado = cmbEstado.getValue();

        if (gira.isEmpty()
                || artista == null
                || estadio == null
                || fecha == null
                || hora.isEmpty()
                || estado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos incompletos",
                    "Completa todos los campos antes de guardar."
            );
            return;
        }

        try {
            LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Hora inválida",
                    "Escribe la hora en formato de 24 horas, por ejemplo 20:00."
            );
            return;
        }

        try {
            conciertoService.crearConcierto(
                    gira,
                    artista.getNombre(),
                    estadio.getNombre(),
                    fecha.toString(),
                    hora,
                    estado
            );

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Concierto creado",
                    "El concierto se registró correctamente."
            );

            if (alGuardar != null) {
                alGuardar.run();
            }

            cerrarVentana();

        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al crear concierto",
                    e.getMessage()
            );
        }
    }

    private void cerrarVentana() {
        Node nodo = btnCancelar;
        if (nodo == null) {
            nodo = cerrar;
        }

        if (nodo != null && nodo.getScene() != null) {
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}


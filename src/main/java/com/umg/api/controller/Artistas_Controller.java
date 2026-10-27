
package com.umg.api.controller;

import com.umg.api.App;
import com.umg.api.model.Artista;
import com.umg.api.model.Usuario;
import com.umg.api.model.Concierto;

import com.umg.api.service.Artista_Service;
import com.umg.api.service.Usuario_Service;
import com.umg.api.service.Concierto_Service;

import com.umg.api.model.Estadio;
import com.umg.api.service.Estadio_Service;
import com.umg.api.ui.Barra;
import com.umg.api.ui.ConciertoFormController;
import com.umg.api.ui.GestionVentasController;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Artistas_Controller {

    // =====================================================
    // ELEMENTOS DEL FXML
    // =====================================================

    @FXML
    private TextField txtBuscar;

    @FXML
    private VBox contenedorFilas;

    @FXML
    private GridPane gridEncabezado;

    @FXML
    private Label lblSeccion;

    @FXML
    private Label lblTitulo;

    @FXML
    private Button btnCrear_Artista;

    // =====================================================
    // SERVICIOS Y DATOS
    // =====================================================

    private Artista_Service artistaService;
    private Usuario_Service usuarioService;
    private Concierto_Service conciertoService;

    private List<Artista> artistas = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Concierto> conciertos = new ArrayList<>();

    private Artista artistaSeleccionado;

    private String vistaActual = "ARTISTAS";

    public static String vistaInicial = "ARTISTAS";
    
    private final Estadio_Service estadioService =
        new Estadio_Service();

    private Map<Integer, String> nombresArtistas =
            new HashMap<>();

    private Map<Integer, String> nombresEstadios =
            new HashMap<>();

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        artistaService = new Artista_Service();
        usuarioService = new Usuario_Service();
        conciertoService = new Concierto_Service();

        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    switch (vistaActual) {
                        case "ARTISTAS":
                            filtrarArtistas(nuevo);
                            break;

                        case "USUARIOS":
                            filtrarUsuarios(nuevo);
                            break;

                        case "CONCIERTOS":
                            filtrarConciertos(nuevo);
                            break;
                    }
                }
        );

        btnCrear_Artista.setOnAction(event -> {

            switch (vistaActual) {

                case "ARTISTAS":
                    abrirCrearArtista();
                    break;

                case "USUARIOS":
                    abrirCrearUsuario();
                    break;

                case "CONCIERTOS":
                    abrirCrearConcierto();
                    break;
            }
        });

        cambiarVista(vistaInicial);
    }

    // =====================================================
    // CAMBIAR DE VISTA
    // =====================================================

    private void cambiarVista(String vista) {

        vistaActual = vista;

        contenedorFilas.getChildren().clear();
        gridEncabezado.getChildren().clear();
        gridEncabezado.getColumnConstraints().clear();

        txtBuscar.clear();

        switch (vista) {

            case "ARTISTAS":

                lblSeccion.setText("CATÁLOGO · ARTISTAS");
                lblTitulo.setText("ARTISTAS");

                txtBuscar.setPromptText(
                        "Buscar artista, género o agencia"
                );

                configurarEncabezado(
                        "ID",
                        "NOMBRE ARTÍSTICO",
                        "GÉNERO MUSICAL",
                        "AGENCIA",
                        "ACCIONES"
                );

                btnCrear_Artista.setText("Nuevo artista");
                btnCrear_Artista.setVisible(true);
                btnCrear_Artista.setManaged(true);

                cargarArtistas();

                break;

            case "CONCIERTOS":

                lblSeccion.setText("AGENDA · CONCIERTOS");
                lblTitulo.setText("CONCIERTOS");

                txtBuscar.setPromptText(
                        "Buscar concierto, artista o recinto"
                );

                configurarEncabezado(
                        "CONCIERTO",
                        "FECHA Y HORA",
                        "RECINTO",
                        "ESTADO",
                        "OCUPACIÓN",
                        "ACCIONES"
                );

                btnCrear_Artista.setText("Programar concierto");
                btnCrear_Artista.setVisible(true);
                btnCrear_Artista.setManaged(true);

                cargarConciertos();

                break;

            case "LOCALIDADES":

                lblSeccion.setText("CATÁLOGO · LOCALIDADES");
                lblTitulo.setText("LOCALIDADES");

                txtBuscar.setPromptText("Buscar localidad");

                configurarEncabezado(
                        "ID",
                        "LOCALIDAD",
                        "ESTADIO",
                        "PRECIO",
                        "ACCIONES"
                );

                ocultarCrearArtista();

                mostrarMensajePendiente(
                        "La sección de localidades todavía "
                        + "no está conectada con su servicio."
                );

                break;

            case "USUARIOS":

                lblSeccion.setText("ADMINISTRACIÓN · USUARIOS");
                lblTitulo.setText("USUARIOS");

                txtBuscar.setPromptText("Buscar usuario");

                configurarEncabezado(
                        "USUARIO",
                        "ROL",
                        "CREADO",
                        "CONEXIÓN",
                        "ACCIONES"
                );

                btnCrear_Artista.setText("Nuevo usuario");
                btnCrear_Artista.setVisible(true);
                btnCrear_Artista.setManaged(true);

                cargarUsuarios();

                break;

            default:

                cambiarVista("ARTISTAS");

                break;
        }
    }

    // =====================================================
    // ENCABEZADOS DINÁMICOS
    // =====================================================

    private void configurarEncabezado(String... columnas) {

        gridEncabezado.getChildren().clear();
        gridEncabezado.getColumnConstraints().clear();

        boolean esUsuarios = "USUARIOS".equals(vistaActual);
        boolean esConciertos = "CONCIERTOS".equals(vistaActual);

        gridEncabezado.setHgap(12);
        gridEncabezado.setMaxWidth(Double.MAX_VALUE);
        gridEncabezado.setPadding(new Insets(11, 18, 11, 18));

        double[] porcentajesUsuarios = {34, 16, 16, 16, 18};
        double[] porcentajesArtistas = {8, 30, 22, 22, 18};
        double[] porcentajesConciertos = {27, 17, 17, 13, 18, 8};

        double[] porcentajes = esConciertos
                ? porcentajesConciertos
                : esUsuarios
                        ? porcentajesUsuarios
                        : porcentajesArtistas;

        for (int i = 0; i < columnas.length; i++) {

            ColumnConstraints columna = new ColumnConstraints();

            columna.setPercentWidth(porcentajes[i]);
            columna.setMinWidth(0);
            columna.setHgrow(Priority.ALWAYS);

            gridEncabezado.getColumnConstraints().add(columna);

            Label encabezado = new Label(columnas[i]);
            encabezado.getStyleClass().add("th");
            encabezado.setMaxWidth(Double.MAX_VALUE);
            encabezado.setWrapText(true);
            encabezado.setAlignment(Pos.CENTER_LEFT);

            gridEncabezado.add(encabezado, i, 0);
        }
    }

    private void ocultarCrearArtista() {
        btnCrear_Artista.setVisible(false);
        btnCrear_Artista.setManaged(false);
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarMensajePendiente(String mensaje) {

        Label etiqueta = new Label(mensaje);

        etiqueta.setWrapText(true);
        etiqueta.setPadding(new Insets(24));
        etiqueta.setMaxWidth(Double.MAX_VALUE);
        etiqueta.getStyleClass().add("s13");

        contenedorFilas.getChildren().add(etiqueta);
    }

    // =====================================================
    // MÉTODO AUXILIAR DE BÚSQUEDA
    // =====================================================

    private boolean contiene(String valor, String busqueda) {

        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    // =====================================================
    // ARTISTAS: CARGAR Y FILTRAR
    // =====================================================

    private void cargarArtistas() {

        try {

            List<Artista> resultado =
                    artistaService.obtenerArtistas();

            artistas = resultado != null
                    ? new ArrayList<>(resultado)
                    : new ArrayList<>();

            filtrarArtistas(txtBuscar.getText());

        } catch (Exception e) {

            System.err.println("ERROR AL CARGAR ARTISTAS:");
            e.printStackTrace();

            artistas = new ArrayList<>();
            contenedorFilas.getChildren().clear();

            mostrarMensajePendiente(
                    "No fue posible cargar los artistas. "
                    + "Comprueba la conexión con la API."
            );
        }
    }

    private void filtrarArtistas(String texto) {

        if (!"ARTISTAS".equals(vistaActual)) {
            return;
        }

        if (artistas == null) {
            artistas = new ArrayList<>();
        }

        if (texto == null || texto.isBlank()) {
            mostrarArtistas(artistas);
            return;
        }

        String busqueda = texto.toLowerCase(Locale.ROOT).trim();

        List<Artista> filtrados = artistas.stream()
                .filter(artista -> artista != null)
                .filter(artista ->
                        contiene(artista.getNombre(), busqueda)
                        || contiene(artista.getGenero(), busqueda)
                        || contiene(artista.getAgencia(), busqueda))
                .toList();

        mostrarArtistas(filtrados);
    }

    private void mostrarArtistas(List<Artista> lista) {

        contenedorFilas.getChildren().clear();

        if (lista == null || lista.isEmpty()) {

            Label mensaje = new Label("No se encontraron artistas.");
            mensaje.setPadding(new Insets(24));
            mensaje.getStyleClass().add("s13");

            contenedorFilas.getChildren().add(mensaje);
            return;
        }

        for (Artista artista : lista) {
            if (artista != null) {
                contenedorFilas.getChildren().add(crearFila(artista));
            }
        }
    }

    // =====================================================
    // ARTISTAS: CREAR FILA
    // =====================================================

    private GridPane crearFila(Artista artista) {

        GridPane fila = new GridPane();

        fila.getStyleClass().add("trow");
        fila.setPrefHeight(70);
        fila.setMinHeight(70);
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setPadding(new Insets(10, 18, 10, 18));
        fila.setHgap(10);

        double[] porcentajes = {8, 30, 22, 22, 18};

        for (double porcentaje : porcentajes) {

            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(porcentaje);
            columna.setMinWidth(10);
            columna.setHgrow(Priority.ALWAYS);

            fila.getColumnConstraints().add(columna);
        }

        Label lbId = new Label(
                "#" + String.format("%02d", artista.getId())
        );
        lbId.getStyleClass().addAll("s12", "c-dim");
        fila.add(lbId, 0, 0);

        HBox nombreBox = new HBox(10);
        nombreBox.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(36, 36);
        avatar.setMinSize(36, 36);
        avatar.setMaxSize(36, 36);
        avatar.getStyleClass().add("avatar");

        String nombre = artista.getNombre();
        String inicial = nombre == null || nombre.isBlank()
                ? "?"
                : nombre.substring(0, 1).toUpperCase(Locale.ROOT);

        Label inicialLabel = new Label(inicial);
        inicialLabel.getStyleClass().add("avatar-text");
        avatar.getChildren().add(inicialLabel);

        Label lbNombre = new Label(nombre == null ? "" : nombre);
        lbNombre.getStyleClass().addAll("s13", "w600");

        nombreBox.getChildren().addAll(avatar, lbNombre);
        fila.add(nombreBox, 1, 0);

        Label lbGenero = new Label(
                artista.getGenero() == null ? "" : artista.getGenero()
        );
        lbGenero.getStyleClass().addAll("s13", "c-text2");
        fila.add(lbGenero, 2, 0);

        Label lbAgencia = new Label(
                artista.getAgencia() == null ? "" : artista.getAgencia()
        );
        lbAgencia.getStyleClass().addAll("s13", "c-text2");
        fila.add(lbAgencia, 3, 0);

        HBox acciones = new HBox(5);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = new Button();
        btnEditar.setMnemonicParsing(false);
        btnEditar.setTooltip(new Tooltip("Editar artista"));
        btnEditar.getStyleClass().add("btn-icon");

        Region iconEditar = new Region();
        iconEditar.getStyleClass().addAll("icon", "ic-edit", "ic-muted");
        btnEditar.setGraphic(iconEditar);
        btnEditar.setOnAction(event -> abrirEditarArtista(artista));

        Button btnEliminar = new Button();
        btnEliminar.setMnemonicParsing(false);
        btnEliminar.setTooltip(new Tooltip("Eliminar artista"));
        btnEliminar.getStyleClass().addAll("btn-icon", "btn-danger");

        Region iconEliminar = new Region();
        iconEliminar.getStyleClass().addAll("icon", "ic-delete", "ic-muted");
        btnEliminar.setGraphic(iconEliminar);
        btnEliminar.setOnAction(event -> abrirEliminarArtista(artista));

        acciones.getChildren().addAll(btnEditar, btnEliminar);
        fila.add(acciones, 4, 0);

        fila.setOnMouseClicked(event -> {

            if (event.getTarget() instanceof Button
                    || event.getTarget() instanceof Region) {
                return;
            }

            artistaSeleccionado = artista;

            for (var nodo : contenedorFilas.getChildren()) {
                nodo.getStyleClass().remove("selected");
            }

            fila.getStyleClass().add("selected");
        });

        return fila;
    }

    // =====================================================
    // USUARIOS: CARGAR Y FILTRAR
    // =====================================================

    private void cargarUsuarios() {

        try {

            List<Usuario> resultado = usuarioService.obtenerUsuarios();

            usuarios = resultado != null
                    ? new ArrayList<>(resultado)
                    : new ArrayList<>();

            filtrarUsuarios(txtBuscar.getText());

        } catch (Exception e) {

            System.err.println("ERROR AL CARGAR USUARIOS:");
            e.printStackTrace();

            usuarios = new ArrayList<>();
            contenedorFilas.getChildren().clear();

            mostrarMensajePendiente(
                    "No fue posible cargar los usuarios. "
                    + "Comprueba la conexión con la API."
            );
        }
    }

    private void filtrarUsuarios(String texto) {

        if (!"USUARIOS".equals(vistaActual)) {
            return;
        }

        String busqueda = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);

        List<Usuario> filtrados = usuarios.stream()
                .filter(usuario -> usuario != null)
                .filter(usuario ->
                        busqueda.isEmpty()
                        || contiene(usuario.getUsuario(), busqueda)
                        || contiene(usuario.getNombre(), busqueda)
                        || contiene(usuario.getRol(), busqueda))
                .toList();

        mostrarUsuarios(filtrados);
    }

    private void mostrarUsuarios(List<Usuario> lista) {

        contenedorFilas.getChildren().clear();

        long activos = usuarios.stream()
                .filter(usuario -> usuario != null && usuario.isStatus())
                .count();

        lblSeccion.setText(
                "ACCESO · " + usuarios.size()
                + " USUARIOS · " + activos + " ACTIVOS"
        );

        if (lista == null || lista.isEmpty()) {
            mostrarMensajePendiente("No se encontraron usuarios.");
            return;
        }

        for (Usuario usuario : lista) {
            contenedorFilas.getChildren().add(crearFilaUsuario(usuario));
        }
    }
    
    //Obtner estadios
    private void cargarNombresRelacionados() throws Exception {

    // Obtener artistas y guardar ID -> nombre
    nombresArtistas = artistaService.obtenerArtistas()
            .stream()
            .collect(Collectors.toMap(
                    artista -> artista.getId(),
                    artista -> artista.getNombre(),
                    (nombreExistente, nuevoNombre) -> nombreExistente
            ));

    // Obtener estadios y guardar ID -> nombre
    List<Estadio> listaEstadios =
            estadioService.obtenerEstadios();

    nombresEstadios = listaEstadios.stream()
            .collect(Collectors.toMap(
                    Estadio::getId,
                    Estadio::getNombre,
                    (nombreExistente, nuevoNombre) -> nombreExistente
            ));
}

    // =====================================================
    // USUARIOS: CREAR FILA
    // =====================================================

    private String tiempoConectado(int minutos) {
        if (minutos < 1) {
            return "menos de 1 min";
        }
        if (minutos < 60) {
            return minutos + " min";
        }
        return (minutos / 60) + " h " + (minutos % 60) + " min";
    }

    private GridPane crearFilaUsuario(Usuario usuario) {

        GridPane fila = new GridPane();

        fila.getStyleClass().add("trow");
        fila.setHgap(12);
        fila.setMinHeight(60);
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setPadding(new Insets(10, 18, 10, 18));

        double[] porcentajes = {34, 16, 16, 16, 18};

        for (double porcentaje : porcentajes) {

            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(porcentaje);
            columna.setMinWidth(0);
            columna.setHgrow(Priority.ALWAYS);

            fila.getColumnConstraints().add(columna);
        }

        String nombre = usuario.getNombre() == null
                || usuario.getNombre().isBlank()
                ? usuario.getUsuario()
                : usuario.getNombre();

        String username = usuario.getUsuario() == null
                ? ""
                : usuario.getUsuario();

        String iniciales = nombre == null || nombre.isBlank()
                ? "?"
                : nombre.substring(0, 1).toUpperCase(Locale.ROOT);

        StackPane avatar = new StackPane();
        avatar.setMinSize(34, 34);
        avatar.setPrefSize(34, 34);
        avatar.setMaxSize(34, 34);
        avatar.getStyleClass().add("avatar");

        Label lbIniciales = new Label(iniciales);
        lbIniciales.getStyleClass().addAll("s12", "w700", "c-amber");
        avatar.getChildren().add(lbIniciales);

        Label lbNombre = new Label(nombre == null ? "" : nombre);
        lbNombre.getStyleClass().addAll("s13", "w600");

        Label lbUsuario = new Label(username);
        lbUsuario.getStyleClass().addAll("s12", "c-dim");

        VBox datosUsuario = new VBox(3, lbNombre, lbUsuario);
        datosUsuario.setAlignment(Pos.CENTER_LEFT);

        HBox identidad = new HBox(10, avatar, datosUsuario);
        identidad.setAlignment(Pos.CENTER_LEFT);

        fila.add(identidad, 0, 0);

        String rol = usuario.getRol() == null
                ? "Sin rol"
                : usuario.getRol();

        Label lbRol = new Label(rol);
        lbRol.getStyleClass().addAll("s11", "w700");

        HBox etiquetaRol = new HBox(lbRol);
        etiquetaRol.setAlignment(Pos.CENTER_LEFT);
        etiquetaRol.getStyleClass().add("pill");

        if ("Administrador".equalsIgnoreCase(rol)) {
            etiquetaRol.setStyle(
                    "-fx-background-color: rgba(255,176,32,0.13);"
            );
            lbRol.setStyle("-fx-text-fill: #FFB020;");
        } else {
            etiquetaRol.setStyle(
                    "-fx-background-color: rgba(60,200,224,0.13);"
            );
            lbRol.setStyle("-fx-text-fill: #3CC8E0;");
        }

        fila.add(etiquetaRol, 1, 0);

        String fecha = usuario.getCreatedAt() == null
                ? "—"
                : usuario.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                );

        Label lbCreado = new Label(fecha);
        lbCreado.getStyleClass().addAll("s12", "c-muted");

        fila.add(lbCreado, 2, 0);

        boolean activo = usuario.isStatus();
        boolean conectado = usuario.isConectado();
        boolean ausente = conectado && usuario.isAusente();

        Region punto = new Region();
        punto.setMinSize(7, 7);
        punto.setPrefSize(7, 7);
        punto.setMaxSize(7, 7);
        punto.getStyleClass().add("dot");

        Label lbEstado = new Label();
        lbEstado.getStyleClass().addAll("s13", "w600");

        if (ausente) {
            punto.setStyle("-fx-background-color: #FFB020;");
            lbEstado.setText("Ausente · " + tiempoConectado(usuario.getMinutosConectado()));
            lbEstado.getStyleClass().add("c-amber");
        } else if (conectado) {
            punto.setStyle("-fx-background-color: #5BD68A;");
            lbEstado.setText("En línea · " + tiempoConectado(usuario.getMinutosConectado()));
            lbEstado.getStyleClass().add("c-green");
        } else {
            punto.setStyle("-fx-background-color: #8C8679;");
            lbEstado.setText("Desconectado");
            lbEstado.getStyleClass().add("c-dim");
        }

        HBox estado = new HBox(6, punto, lbEstado);
        estado.setAlignment(Pos.CENTER_LEFT);

        fila.add(estado, 3, 0);

        ToggleButton btnEstado = new ToggleButton();
        btnEstado.setSelected(activo);
        btnEstado.setMnemonicParsing(false);
        btnEstado.getStyleClass().add("switch");

        btnEstado.setTooltip(new Tooltip(
                activo ? "Desactivar usuario" : "Activar usuario"
        ));

        btnEstado.setOnAction(event -> {

            btnEstado.setDisable(true);

            try {

                usuarioService.cambiarEstado(
                        usuario.getId(),
                        btnEstado.isSelected()
                );

                cargarUsuarios();

            } catch (Exception e) {

                e.printStackTrace();

                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo cambiar el estado del usuario."
                );

                btnEstado.setSelected(!btnEstado.isSelected());
                btnEstado.setDisable(false);
            }
        });

        Button btnEditar = new Button();
        btnEditar.getStyleClass().add("btn-icon");
        btnEditar.setMnemonicParsing(false);
        btnEditar.setTooltip(new Tooltip("Editar usuario"));

        Region iconoEditar = new Region();
        iconoEditar.getStyleClass().addAll(
                "icon", "ic-edit", "ic-muted"
        );

        iconoEditar.setMinSize(18, 18);
        iconoEditar.setPrefSize(18, 18);
        iconoEditar.setMaxSize(18, 18);

        btnEditar.setGraphic(iconoEditar);
        btnEditar.setOnAction(
                event -> abrirEdicionUsuario(usuario)
        );

        HBox acciones = new HBox(8, btnEstado, btnEditar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        fila.add(acciones, 4, 0);

        fila.setCursor(Cursor.HAND);
        fila.setOnMouseClicked(event -> {
            Node nodo = (Node) event.getTarget();
            while (nodo != null && nodo != fila) {
                if (nodo instanceof ButtonBase) {
                    return;
                }
                nodo = nodo.getParent();
            }
            GestionVentasController.usuarioInicial = usuario.getId();
            GestionVentasController.nombreInicial = nombre;
            Barra.ir("GestionVentas.fxml");
        });

        return fila;
    }

    // =====================================================
    // USUARIOS: CREAR Y EDITAR
    // =====================================================

    private void abrirCrearUsuario() {

        try {

            java.net.URL recurso = getClass().getResource(
                    "/Recursos/UsuarioForm.fxml"
            );

            if (recurso == null) {
                throw new IllegalStateException(
                        "No se encontró /Recursos/UsuarioForm.fxml"
                );
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Usuario_Controller controller = loader.getController();
            controller.setAlGuardar(this::cargarUsuarios);

            Stage stage = new Stage();
            stage.setTitle("Nuevo usuario");
            stage.initModality(Modality.APPLICATION_MODAL);

            if (btnCrear_Artista.getScene() != null) {
                stage.initOwner(
                        btnCrear_Artista.getScene().getWindow()
                );
            }

            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            Throwable causa = e;

            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al abrir usuario");
            alerta.setHeaderText("No se pudo cargar el formulario");

            alerta.setContentText(
                    e.getClass().getSimpleName() + ": "
                    + e.getMessage()
                    + "\n\nCausa final: "
                    + causa.getClass().getSimpleName() + ": "
                    + causa.getMessage()
            );

            alerta.showAndWait();
        }
    }

    private void abrirEdicionUsuario(Usuario usuario) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/Recursos/Editar_Usuario.fxml"
                    )
            );

            Parent contenido = loader.load();

            Usuario_Controller controller = loader.getController();
            controller.setUsuarioEditar(usuario);
            controller.setAlGuardar(this::cargarUsuarios);

            Stage ventana = new Stage();
            ventana.setTitle("Editar usuario");
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setScene(new Scene(contenido));
            ventana.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir el formulario de edición."
            );
        }
    }

    // =====================================================
    // ARTISTAS: CREAR
    // =====================================================

    private void abrirCrearArtista() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/Recursos/ArtistaForm.fxml"
                    )
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo artista");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarArtistas();

        } catch (Exception e) {

            System.err.println("ERROR AL ABRIR CREAR ARTISTA:");
            e.printStackTrace();
        }
    }

    // =====================================================
    // ARTISTAS: EDITAR
    // =====================================================

    private void abrirEditarArtista(Artista artista) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/Recursos/Editar_Artista.fxml"
                    )
            );

            Parent root = loader.load();

            ArtistaEditar_Controller controller = loader.getController();
            controller.setArtista(artista);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar artista");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarArtistas();

        } catch (Exception e) {

            System.err.println("ERROR AL ABRIR EDITAR ARTISTA:");
            e.printStackTrace();
        }
    }

    // =====================================================
    // ARTISTAS: ELIMINAR
    // =====================================================

    private void abrirEliminarArtista(Artista artista) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/Recursos/Eliminar_Artista.fxml"
                    )
            );

            Parent root = loader.load();

            ArtistaEliminar_Controller controller =
                    loader.getController();

            controller.setArtista(artista);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Eliminar artista");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarArtistas();

        } catch (Exception e) {

            System.err.println("ERROR AL ABRIR ELIMINAR ARTISTA:");
            e.printStackTrace();
        }
    }

    // =====================================================
    // CONCIERTOS: CARGAR Y FILTRAR
    // =====================================================

    private void cargarConciertos() {
    try {
        // Cargar los catálogos para resolver los ID
        cargarNombresRelacionados();

        List<Concierto> resultado =
                conciertoService.obtenerConciertos();

        conciertos = resultado != null
                ? new ArrayList<>(resultado)
                : new ArrayList<>();

        filtrarConciertos(txtBuscar.getText());

    } catch (Exception e) {
        System.err.println("ERROR AL CARGAR CONCIERTOS:");
        e.printStackTrace();

        conciertos = new ArrayList<>();
        contenedorFilas.getChildren().clear();

        mostrarMensajePendiente(
                "No fue posible cargar los conciertos. "
                + "Comprueba la conexión con la API."
        );
    }
}

    
private void filtrarConciertos(String texto) {

    if (!"CONCIERTOS".equals(vistaActual)) {
        return;
    }

    String busqueda = texto == null
            ? ""
            : texto.trim().toLowerCase(Locale.ROOT);

    List<Concierto> filtrados = conciertos.stream()
            .filter(concierto -> concierto != null)
            .filter(concierto -> {
                String nombreArtista = nombresArtistas.getOrDefault(
                        concierto.getArtistaId(), "Artista no encontrado");

                String nombreEstadio = nombresEstadios.getOrDefault(
                        concierto.getEstadioId(), "Recinto no encontrado");

                return busqueda.isEmpty()
                        || contiene(concierto.getGira(), busqueda)
                        || contiene(concierto.getEstado(), busqueda)
                        || contiene(String.valueOf(concierto.getFecha()), busqueda)
                        || contiene(String.valueOf(concierto.getHora()), busqueda)
                        || contiene(nombreArtista, busqueda)
                        || contiene(nombreEstadio, busqueda)
                        || String.valueOf(concierto.getId()).contains(busqueda)
                        || String.valueOf(concierto.getArtistaId()).contains(busqueda)
                        || String.valueOf(concierto.getEstadioId()).contains(busqueda);
            })
            .toList();

    mostrarConciertos(filtrados);
}


    private void mostrarConciertos(List<Concierto> lista) {

        contenedorFilas.getChildren().clear();

        if (lista == null || lista.isEmpty()) {
            mostrarMensajePendiente(
                    "No se encontraron conciertos."
            );
            return;
        }

        for (Concierto concierto : lista) {
            contenedorFilas.getChildren().add(
                    crearFilaConcierto(concierto)
            );
        }
    }
    
    
private void abrirEditarConcierto(Concierto concierto) {
    try {
        java.net.URL recurso =
                getClass().getResource("/Recursos/Editar_Concierto.fxml");

        if (recurso == null) {
            throw new IllegalStateException(
                    "No se encontró /Recursos/Editar_Concierto.fxml"
            );
        }

        FXMLLoader loader = new FXMLLoader(recurso);

        EditarConcierto_Controller controller =
                new EditarConcierto_Controller(
                        concierto,
                        this::cargarConciertos
                );

        loader.setController(controller);

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Editar concierto");
        stage.initModality(Modality.APPLICATION_MODAL);

        if (btnCrear_Artista.getScene() != null) {
            stage.initOwner(
                    btnCrear_Artista.getScene().getWindow()
            );
        }

        stage.setScene(new Scene(root));
        stage.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();

        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Error",
                "No fue posible abrir el formulario de edición.\n"
                + e.getMessage()
        );
    }
}


    // =====================================================
    // CONCIERTOS: CREAR FILA
    // =====================================================

   
private GridPane crearFilaConcierto(Concierto concierto) {

    GridPane fila = new GridPane();

    fila.getStyleClass().add("trow");
    fila.setMinHeight(76);
    fila.setPrefHeight(76);
    fila.setMaxWidth(Double.MAX_VALUE);
    fila.setPadding(new Insets(10, 18, 10, 18));
    fila.setHgap(12);

    double[] porcentajes = {27, 17, 17, 13, 18, 8};

    for (double porcentaje : porcentajes) {

        ColumnConstraints columna = new ColumnConstraints();
        columna.setPercentWidth(porcentaje);
        columna.setMinWidth(0);
        columna.setHgrow(Priority.ALWAYS);

        fila.getColumnConstraints().add(columna);
    }

    // 1. Nombre del concierto y nombre del artista.

    String gira = concierto.getGira() == null
            ? "Sin nombre"
            : concierto.getGira();

    Label nombre = new Label(gira);
    nombre.getStyleClass().addAll("s13", "w600");
    nombre.setWrapText(true);

    String nombreArtista = nombresArtistas.getOrDefault(
            concierto.getArtistaId(),
            "Artista no encontrado"
    );

    Label detalle = new Label(
            "#" + concierto.getId()
            + " · " + nombreArtista
    );
    detalle.getStyleClass().addAll("s12", "c-dim");
    detalle.setWrapText(true);

    VBox datos = new VBox(4, nombre, detalle);
    datos.setAlignment(Pos.CENTER_LEFT);

    fila.add(datos, 0, 0);

    // 2. Fecha y hora.

    Label fecha = new Label(
            formatearFecha(concierto.getFecha())
    );
    fecha.getStyleClass().addAll("s13", "w600");

    Label hora = new Label(
            formatearHora(concierto.getHora())
    );
    hora.getStyleClass().addAll("s12", "c-dim");

    VBox fechaHora = new VBox(4, fecha, hora);
    fechaHora.setAlignment(Pos.CENTER_LEFT);

    fila.add(fechaHora, 1, 0);

    // 3. Nombre del recinto.

    String nombreEstadio = nombresEstadios.getOrDefault(
            concierto.getEstadioId(),
            "Recinto no encontrado"
    );

    Label estadio = new Label(nombreEstadio);
    estadio.getStyleClass().addAll("s13", "c-text2");
    estadio.setWrapText(true);

    fila.add(estadio, 2, 0);

    // 4. Estado.

    String estadoOriginal = concierto.getEstado() == null
            ? "Sin estado"
            : concierto.getEstado().trim();

    String estadoNormalizado =
            estadoOriginal.toLowerCase(Locale.ROOT);

    String colorFondo;
    String colorTexto;

    switch (estadoNormalizado) {

        case "en venta":
            colorFondo = "rgba(91,214,138,0.14)";
            colorTexto = "#5BD68A";
            break;

        case "finalizado":
            colorFondo = "rgba(140,134,121,0.18)";
            colorTexto = "#A8A29A";
            break;

        case "agotado":
            colorFondo = "rgba(168,100,255,0.16)";
            colorTexto = "#B985FF";
            break;

        case "cancelado":
            colorFondo = "rgba(255,91,91,0.14)";
            colorTexto = "#FF6B6B";
            break;

        default:
            colorFondo = "rgba(140,134,121,0.14)";
            colorTexto = "#A8A29A";
            break;
    }

    Label etiquetaEstado = new Label(estadoOriginal);
    etiquetaEstado.getStyleClass().addAll("s11", "w700");
    etiquetaEstado.setStyle(
            "-fx-text-fill: " + colorTexto + ";"
    );

    HBox estado = new HBox(etiquetaEstado);
    estado.setAlignment(Pos.CENTER_LEFT);
    estado.setPadding(new Insets(5, 8, 5, 8));
    estado.setStyle(
            "-fx-background-color: " + colorFondo + ";"
            + "-fx-background-radius: 12;"
    );

    fila.add(estado, 3, 0);

    // 5. Ocupación pendiente.

    Label ocupacion = new Label("No disponible");
    ocupacion.getStyleClass().addAll("s12", "c-dim");

    fila.add(ocupacion, 4, 0);

    // 6. Acción.

    Button btnEditar = new Button();
    btnEditar.setMnemonicParsing(false);
    btnEditar.setTooltip(
            new Tooltip("Editar concierto")
    );
    btnEditar.getStyleClass().add("btn-icon");

    Region icono = new Region();
    icono.getStyleClass().addAll(
            "icon", "ic-edit", "ic-muted"
    );

    btnEditar.setGraphic(icono);

    btnEditar.setOnAction(event ->
        abrirEditarConcierto(concierto)
    );

    Button btnPrecios = new Button();
    btnPrecios.setMnemonicParsing(false);
    btnPrecios.setTooltip(
            new Tooltip("Inventario y precios")
    );
    btnPrecios.getStyleClass().add("btn-icon");

    Region iconoPrecios = new Region();
    iconoPrecios.getStyleClass().addAll(
            "icon", "ic-payments", "ic-muted"
    );

    btnPrecios.setGraphic(iconoPrecios);

    btnPrecios.setOnAction(event ->
        abrirInventario(concierto)
    );

    HBox acciones = new HBox(4, btnEditar, btnPrecios);
    acciones.setAlignment(Pos.CENTER_LEFT);

    fila.add(acciones, 5, 0);

    return fila;
}


    private void abrirInventario(Concierto concierto) {
        try {
            ConciertoFormController.conciertoInicial = concierto.getId();
            App.abrirDialogo("ConciertoForm.fxml", "Inventario y precios");
            cargarConciertos();
        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No fue posible abrir el inventario.\n"
                    + e.getMessage()
            );
        }
    }

    private String formatearFecha(Object valor) {

        if (valor == null) {
            return "Sin fecha";
        }

        String texto = valor.toString();

        try {

            LocalDate fecha = LocalDate.parse(
                    texto.substring(0, 10)
            );

            return fecha.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );

        } catch (Exception e) {
            return texto;
        }
    }

    private String formatearHora(Object valor) {

        if (valor == null) {
            return "Sin hora";
        }

        String texto = valor.toString();

        try {

            LocalTime hora = LocalTime.parse(
                    texto.length() >= 8
                            ? texto.substring(0, 8)
                            : texto
            );

            return hora.format(
                    DateTimeFormatter.ofPattern("HH:mm")
            );

        } catch (Exception e) {
            return texto;
        }
    }

    // =====================================================
    // CONCIERTOS: CREAR
    // =====================================================

   
private void abrirCrearConcierto() {
    try {
        java.net.URL recurso = getClass().getResource(
                "/Recursos/ConciertoForm.fxml"
        );

        if (recurso == null) {
            throw new IllegalStateException(
                    "No se encontró /Recursos/ConciertoForm.fxml"
            );
        }

        FXMLLoader loader = new FXMLLoader(recurso);

        Concierto_Controller controller =
                new Concierto_Controller();

        controller.setAlGuardar(this::cargarConciertos);
        loader.setController(controller);

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Nuevo concierto");
        stage.initModality(Modality.APPLICATION_MODAL);

        if (btnCrear_Artista.getScene() != null) {
            stage.initOwner(
                    btnCrear_Artista.getScene().getWindow()
            );
        }

        stage.setScene(new Scene(root));
        stage.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error al abrir formulario");
        alerta.setHeaderText("No se pudo abrir el formulario de concierto");
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}


    // =====================================================
    // ALERTAS
    // =====================================================

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

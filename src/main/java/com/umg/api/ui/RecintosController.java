package com.umg.api.ui;

import com.umg.api.model.Estadio;
import com.umg.api.model.Localidad;
import com.umg.api.model.ZonaMapa;
import com.umg.api.service.EstadioService;
import com.umg.api.service.LocalidadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class RecintosController {

    @FXML
    private Button btnTeatro;

    @FXML
    private Button btnEstadio;

    @FXML
    private Button btnExplanada;

    @FXML
    private Label lblAviso;

    @FXML
    private ComboBox<Estadio> cmbRecinto;

    @FXML
    private Button btnNuevoRecinto;

    @FXML
    private VBox listaZonas;

    @FXML
    private TextField txtNombre;

    @FXML
    private Label lblDistribucion;

    @FXML
    private HBox filaPorLado;

    @FXML
    private Button btnPorLadoMenos;

    @FXML
    private Label lblPorLado;

    @FXML
    private Label lblPorLadoMax;

    @FXML
    private Button btnPorLadoMas;

    @FXML
    private HBox filaFilas;

    @FXML
    private Label lblFilasTitulo;

    @FXML
    private Button btnFilasMenos;

    @FXML
    private Label lblFilas;

    @FXML
    private Label lblFilasMax;

    @FXML
    private Button btnFilasMas;

    @FXML
    private HBox filaAsientos;

    @FXML
    private Button btnAsientosMenos;

    @FXML
    private Label lblAsientos;

    @FXML
    private Label lblAsientosMax;

    @FXML
    private Button btnAsientosMas;

    @FXML
    private VBox notaZona;

    @FXML
    private Button color1;

    @FXML
    private Button color2;

    @FXML
    private Button color3;

    @FXML
    private Button color4;

    @FXML
    private Button color5;

    @FXML
    private Button color6;

    @FXML
    private Label lblTotalNueva;

    @FXML
    private Button btnDescartar;

    @FXML
    private Button btnGuardar;

    @FXML
    private VBox planoPrevia;

    @FXML
    private HBox leyenda;

    @FXML
    private Region swConfigurado;

    @FXML
    private Label lblConfigurado;

    @FXML
    private Region swMaximo;

    @FXML
    private Label lblMaximo;

    private static final String[] COLORES = {"#FF5C8A", "#3CC8E0", "#A18CFF", "#5BD68A", "#6C9BFF", "#E879F9"};

    private final EstadioService estadioService = new EstadioService();
    private final LocalidadService localidadService = new LocalidadService();

    private String plano = "TEATRO";
    private List<Localidad> localidades = new ArrayList<>();
    private final Map<Integer, List<Localidad>> planos = new HashMap<>();
    private List<Estadio> estadios;
    private Localidad original;
    private Localidad borrador;
    private List<Button> botonesColor;

    @FXML
    private void initialize() {
        btnTeatro.setOnAction(e -> elegirPlano("TEATRO"));
        btnEstadio.setOnAction(e -> elegirPlano("ESTADIO"));
        btnExplanada.setOnAction(e -> elegirPlano("EXPLANADA"));
        btnNuevoRecinto.setOnAction(e -> nuevoRecinto());
        cmbRecinto.valueProperty().addListener((obs, viejo, nuevo) -> elegirRecinto());

        btnPorLadoMenos.setOnAction(e -> cambiar(0, 0, -1));
        btnPorLadoMas.setOnAction(e -> cambiar(0, 0, 1));
        btnFilasMenos.setOnAction(e -> cambiar(-1, 0, 0));
        btnFilasMas.setOnAction(e -> cambiar(1, 0, 0));
        btnAsientosMenos.setOnAction(e -> cambiar(0, -1, 0));
        btnAsientosMas.setOnAction(e -> cambiar(0, 1, 0));

        botonesColor = List.of(color1, color2, color3, color4, color5, color6);
        for (int i = 0; i < botonesColor.size(); i++) {
            String c = COLORES[i];
            botonesColor.get(i).setOnAction(e -> {
                borrador.setColor(c);
                mostrar();
            });
        }

        txtNombre.textProperty().addListener((obs, viejo, nuevo) -> {
            if (borrador != null && !nuevo.equals(borrador.getNombre())) {
                borrador.setNombre(nuevo);
                mostrar();
            }
        });
        btnDescartar.setOnAction(e -> {
            borrador = copiar(original);
            mostrar();
        });
        btnGuardar.setOnAction(e -> guardar());

        elegirPlano("TEATRO");
    }

    private void elegirPlano(String nuevo) {
        plano = nuevo;
        for (Button b : List.of(btnTeatro, btnEstadio, btnExplanada)) {
            b.getStyleClass().remove("seg-on");
        }
        ("ESTADIO".equals(plano) ? btnEstadio : "EXPLANADA".equals(plano) ? btnExplanada : btnTeatro).getStyleClass().add("seg-on");
        llenarRecintos(null);
    }

    private void llenarRecintos(Integer elegir) {
        if (estadios == null) {
            lblAviso.setText("");
            Fondo.cargar(() -> estadioService.getEstadios(), lista -> {
                estadios = lista;
                llenarRecintos(elegir);
            }, ex -> lblAviso.setText("Sin conexión con el servidor"));
            return;
        }
        List<Estadio> deEsteTipo = new ArrayList<>();
        Estadio seleccionar = null;
        for (Estadio e : estadios) {
            String tipo = e.getTipo() == null ? "TEATRO" : e.getTipo();
            if (tipo.equals(plano)) {
                deEsteTipo.add(e);
                if (seleccionar == null || (elegir != null && e.getId() == elegir)) {
                    seleccionar = e;
                }
            }
        }
        lblAviso.setText(deEsteTipo.isEmpty() ? "Todavía no hay recintos de este tipo" : "");
        cmbRecinto.setItems(FXCollections.observableArrayList(deEsteTipo));
        if (seleccionar != null) {
            cmbRecinto.getSelectionModel().select(seleccionar);
        }
    }

    private void elegirRecinto() {
        Estadio recinto = cmbRecinto.getValue();
        if (recinto == null) {
            usar(new ArrayList<>(), null);
            return;
        }
        if (planos.containsKey(recinto.getId())) {
            usar(planos.get(recinto.getId()), null);
        } else {
            cargar(null);
        }
    }

    private void cargar(Integer elegir) {
        Estadio pedido = cmbRecinto.getValue();
        if (pedido == null) {
            return;
        }
        Fondo.cargar(() -> localidadService.getLocalidades(pedido.getId()), lista -> {
            planos.put(pedido.getId(), lista);
            if (pedido == cmbRecinto.getValue()) {
                usar(lista, elegir);
            }
        }, ex -> Barra.alerta(Alert.AlertType.ERROR, "No se pudieron cargar las localidades: " + ex.getMessage()));
    }

    private void usar(List<Localidad> lista, Integer elegir) {
        localidades = lista;
        original = null;
        for (Localidad l : localidades) {
            if (original == null || (elegir != null && l.getId() == elegir)) {
                original = l;
            }
        }
        borrador = original == null ? null : copiar(original);
        mostrar();
    }

    private Localidad copiar(Localidad l) {
        Localidad c = new Localidad();
        c.setId(l.getId());
        c.setCodigo(l.getCodigo());
        c.setNombre(l.getNombre());
        c.setPlano(l.getPlano());
        c.setTipo(l.getTipo());
        c.setFilas(l.getFilas());
        c.setAsientos_por_fila(l.getAsientos_por_fila());
        c.setPorLado(l.getPorLado());
        c.setColor(l.getColor());
        c.setConciertos(l.getConciertos());
        return c;
    }

    private boolean esZona(Localidad l) {
        return "ZONA".equals(l.getTipo());
    }

    private boolean esPalcos(Localidad l) {
        return "PALCOS".equals(l.getTipo());
    }

    private int[][] limites(Localidad l) {
        if (esZona(l)) {
            return null;
        }
        if (esPalcos(l)) {
            return new int[][]{{1, 3}, {2, 4}, {2, 5}};
        }
        if ("GRADA".equals(l.getTipo())) {
            if ("EXPLANADA".equals(l.getPlano())) {
                return new int[][]{{2, 5}, {6, 10}};
            }
            if ("CAB".equals(l.getCodigo())) {
                return new int[][]{{2, 5}, {10, 20}};
            }
            return new int[][]{{3, 8}, {4, 7}};
        }
        return new int[][]{{1, "PLA".equals(l.getCodigo()) ? 10 : 7}, {3, "BAL".equals(l.getCodigo()) ? 13 : 11}};
    }

    private Localidad maximo(Localidad l) {
        Localidad m = copiar(l);
        int[][] lim = limites(l);
        if (lim != null) {
            m.setFilas(lim[0][1]);
            m.setAsientos_por_fila(lim[1][1]);
            if (lim.length > 2) {
                m.setPorLado(lim[2][1]);
            }
        }
        return m;
    }

    private ZonaMapa zonaDe(Localidad l) {
        ZonaMapa z = new ZonaMapa();
        z.setId(l.getId());
        z.setCodigo(l.getCodigo());
        z.setNombre(l.getNombre());
        z.setTipo(l.getTipo());
        z.setColor(l.getColor());
        z.setFilas(l.getFilas());
        z.setAsientos_por_fila(l.getAsientos_por_fila());
        z.setPorLado(l.getPorLado());
        z.setModo(esZona(l) ? "DE_PIE" : "NUMERADA");
        return z;
    }

    private int asientos(Localidad l) {
        return PlanoRecinto.bloquesDe(zonaDe(l)).size() * l.getFilas() * l.getAsientos_por_fila();
    }

    private String distribucion(Localidad l) {
        if (esPalcos(l)) {
            return (l.getPorLado() * 2) + " palcos × " + (l.getFilas() * l.getAsientos_por_fila()) + " asientos";
        }
        if (esZona(l)) {
            return "Zona de pie · sin asiento asignado";
        }
        String inicio = "CAB".equals(l.getCodigo()) ? "2 cabeceras × " : "GRADA".equals(l.getTipo()) ? "3 secciones × " : "3 bloques × ";
        return inicio + l.getFilas() + " filas × " + l.getAsientos_por_fila() + " asientos";
    }

    private void cambiar(int filas, int porFila, int porLado) {
        int[][] lim = limites(borrador);
        if (lim == null) {
            return;
        }
        int f = borrador.getFilas() + filas;
        int a = borrador.getAsientos_por_fila() + porFila;
        int p = (borrador.getPorLado() == null ? 0 : borrador.getPorLado()) + porLado;
        if (f < lim[0][0] || f > lim[0][1] || a < lim[1][0] || a > lim[1][1]) {
            return;
        }
        if (lim.length > 2 && (p < lim[2][0] || p > lim[2][1])) {
            return;
        }
        borrador.setFilas(f);
        borrador.setAsientos_por_fila(a);
        if (lim.length > 2) {
            borrador.setPorLado(p);
        }
        mostrar();
    }

    private boolean cambio() {
        return original != null && (!borrador.getNombre().equals(original.getNombre())
                || !borrador.getColor().equalsIgnoreCase(original.getColor())
                || borrador.getFilas() != original.getFilas()
                || borrador.getAsientos_por_fila() != original.getAsientos_por_fila()
                || (esPalcos(borrador) && !borrador.getPorLado().equals(original.getPorLado())));
    }

    private void mostrar() {
        dibujarLista();
        if (borrador == null) {
            planoPrevia.getChildren().clear();
            txtNombre.setText("");
            lblTotalNueva.setText("");
            lblConfigurado.setText("");
            lblMaximo.setText("");
            btnGuardar.setOpacity(0.4);
            btnGuardar.setDisable(true);
            return;
        }

        if (!txtNombre.getText().equals(borrador.getNombre())) {
            txtNombre.setText(borrador.getNombre());
        }
        boolean zona = esZona(borrador);
        boolean palcos = esPalcos(borrador);
        int[][] lim = limites(borrador);

        if ("CAB".equals(borrador.getCodigo())) {
            lblDistribucion.setText("Distribución: Cabeceras norte y sur");
        } else if (palcos) {
            lblDistribucion.setText("Distribución: Palcos laterales");
        } else if (zona) {
            lblDistribucion.setText("Distribución: Zona general de pie");
        } else if ("GRADA".equals(borrador.getTipo())) {
            lblDistribucion.setText("Distribución: Graderío en 3 secciones");
        } else {
            lblDistribucion.setText("Distribución: Bloques en abanico (A · B · C)");
        }
        lblFilasTitulo.setText("GRADA".equals(borrador.getTipo()) ? "Filas por sección" : "Filas por bloque");

        mostrarFila(filaPorLado, palcos);
        mostrarFila(filaFilas, !zona);
        mostrarFila(filaAsientos, !zona);
        notaZona.setVisible(zona);
        notaZona.setManaged(zona);
        leyenda.setVisible(!zona);

        if (lim != null) {
            lblFilas.setText(String.valueOf(borrador.getFilas()));
            lblFilasMax.setText(" / " + lim[0][1]);
            lblAsientos.setText(String.valueOf(borrador.getAsientos_por_fila()));
            lblAsientosMax.setText(" / " + lim[1][1]);
            if (palcos) {
                lblPorLado.setText(String.valueOf(borrador.getPorLado()));
                lblPorLadoMax.setText(" / " + lim[2][1]);
            }
        }

        for (int i = 0; i < COLORES.length; i++) {
            botonesColor.get(i).getStyleClass().remove("color-on");
            if (COLORES[i].equalsIgnoreCase(borrador.getColor())) {
                botonesColor.get(i).getStyleClass().add("color-on");
            }
        }

        String capacidad = zona ? "Aforo variable" : asientos(borrador) + " asientos";
        lblTotalNueva.setText(capacidad);
        lblConfigurado.setText("Configurado · " + capacidad);
        lblMaximo.setText("Máximo posible · " + asientos(maximo(borrador)) + " asientos");
        swConfigurado.setStyle("-fx-background-color: " + borrador.getColor() + ";");
        swMaximo.setStyle("-fx-border-color: " + PlanoRecinto.rgba(borrador.getColor(), 0.55) + ";");

        boolean sucio = cambio();
        btnGuardar.setOpacity(sucio ? 1 : 0.4);
        btnGuardar.setDisable(!sucio);
        dibujarPrevia();
    }

    private void mostrarFila(HBox fila, boolean visible) {
        fila.setVisible(visible);
        fila.setManaged(visible);
    }

    private void dibujarLista() {
        listaZonas.getChildren().clear();
        for (Localidad l : localidades) {
            boolean elegida = borrador != null && l.getId() == borrador.getId();
            Plantilla p = Plantilla.cargar("TarjetaZona.fxml");
            p.<Region>get("color").setStyle("-fx-background-color: " + l.getColor() + ";");
            p.<Label>get("lblNombre").setText(l.getNombre());
            p.<Label>get("lblForma").setText(distribucion(l));
            p.<Label>get("lblTotal").setText(esZona(l) ? "De pie" : String.valueOf(asientos(l)));
            p.<Label>get("lblConciertos").setText(l.getConciertos() + (l.getConciertos() == 1 ? " concierto" : " conciertos"));
            Button boton = p.get("boton");
            if (elegida) {
                boton.setStyle("-fx-border-color: " + l.getColor() + "; -fx-background-color: " + PlanoRecinto.rgba(l.getColor(), 0.08) + ";");
            }
            boton.setOnAction(e -> {
                original = l;
                borrador = copiar(l);
                mostrar();
            });
            listaZonas.getChildren().add(boton);
        }
    }

    private void dibujarPrevia() {
        Localidad tope = maximo(borrador);
        List<ZonaMapa> zonas = new ArrayList<>();
        for (Localidad l : localidades) {
            zonas.add(zonaDe(l.getId() == borrador.getId() ? tope : l));
        }

        Pane dibujo = PlanoRecinto.dibujar(plano, zonas, new PlanoRecinto.Pintor() {
            @Override
            public void asiento(Button boton, ZonaMapa zona, String bloque, int fila, int numero) {
                boton.setMouseTransparent(true);
                boolean configurado = zona.getId() != borrador.getId()
                        || (fila < borrador.getFilas() && numero <= borrador.getAsientos_por_fila()
                        && (!esPalcos(borrador) || Integer.parseInt(bloque.substring(1)) <= borrador.getPorLado()));
                if (configurado) {
                    boton.setStyle("-fx-background-color: " + zona.getColor() + ";");
                } else {
                    boton.getStyleClass().add("seat-ghost");
                    boton.setStyle("-fx-border-color: " + PlanoRecinto.rgba(zona.getColor(), 0.55) + ";");
                }
            }

            @Override
            public void zona(VBox caja, ZonaMapa zona, boolean compacta) {
                Label sub = new Label("De pie · aforo por concierto");
                sub.getStyleClass().addAll("s12", "w600", "c-text2");
                caja.getChildren().add(sub);
            }

            @Override
            public double opacidad(ZonaMapa zona) {
                return zona.getId() == borrador.getId() ? 1 : 0.22;
            }
        });
        double escala = Math.min(0.94, Math.min(520 / dibujo.getPrefHeight(), 720 / dibujo.getPrefWidth()));
        dibujo.setScaleX(escala);
        dibujo.setScaleY(escala);
        planoPrevia.getChildren().setAll(new Group(dibujo));
    }

    private void guardar() {
        if (borrador.getNombre() == null || borrador.getNombre().isBlank()) {
            Barra.alerta(Alert.AlertType.WARNING, "El nombre de la localidad es obligatorio");
            return;
        }
        try {
            localidadService.actualizarLocalidad(borrador);
            cargar(borrador.getId());
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    private void nuevoRecinto() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nuevo recinto");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Nombre del recinto (" + Estadio.nombreTipo(plano) + "):");
        Optional<String> nombre = dialogo.showAndWait();
        if (nombre.isEmpty() || nombre.get().isBlank()) {
            return;
        }
        try {
            Estadio nuevo = estadioService.crearEstadio(nombre.get().trim(), plano);
            estadios = null;
            llenarRecintos(nuevo.getId());
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, ex.getMessage());
        }
    }
}

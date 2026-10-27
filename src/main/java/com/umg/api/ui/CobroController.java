package com.umg.api.ui;

import com.umg.api.Sesion;
import com.umg.api.model.AsientoMapa;
import com.umg.api.model.Cliente;
import com.umg.api.model.Concierto;
import com.umg.api.model.Orden;
import com.umg.api.model.Venta;
import com.umg.api.model.ZonaMapa;
import com.umg.api.service.ClienteService;
import com.umg.api.service.VentaService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CobroController {

    @FXML
    private ToggleGroup grupoMetodo;

    @FXML
    private Label lblResumen;

    @FXML
    private Label lblArtista;

    @FXML
    private Label lblDetalle;

    @FXML
    private VBox lineasResumen;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnCerrarCobro;

    @FXML
    private ToggleButton tglEfectivo;

    @FXML
    private ToggleButton tglTarjeta;

    @FXML
    private VBox panelEfectivo;

    @FXML
    private TextField txtRecibido;

    @FXML
    private Label lblCambio;

    @FXML
    private Button btnExacto;

    @FXML
    private Button btnMonto1;

    @FXML
    private Button btnMonto2;

    @FXML
    private Button btnMonto3;

    @FXML
    private VBox panelTarjeta;

    @FXML
    private CheckBox chkCorreo;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtNit;

    @FXML
    private Button btnBuscarNit;

    @FXML
    private Label lblClienteEstado;

    @FXML
    private TextField txtCliente;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnConfirmarVenta;

    private final ClienteService clienteService = new ClienteService();
    private final VentaService ventaService = new VentaService();

    private final PauseTransition espera = new PauseTransition(Duration.millis(400));

    private Orden orden;
    private Cliente cliente;
    private String nombreAutomatico;

    @FXML
    private void initialize() {
        orden = Sesion.getOrden();
        Concierto c = orden.getConcierto();

        int boletos = orden.getAsientos().size();
        if (orden.getCantidades() != null) {
            for (int cantidad : orden.getCantidades().values()) {
                boletos += cantidad;
            }
        }
        lblResumen.setText("RESUMEN · " + boletos + (boletos == 1 ? " BOLETO" : " BOLETOS"));
        lblArtista.setText(c.getArtista() == null ? "" : c.getArtista().toUpperCase());
        lblDetalle.setText(Formato.fechaLarga(c.getFecha(), c.getHora()) + (c.getEstadio() == null ? "" : " · " + c.getEstadio()));
        lblTotal.setText(Formato.dinero(orden.getTotal()));
        btnConfirmarVenta.setText("Confirmar venta · " + Formato.dinero(orden.getTotal()));
        dibujarResumen();

        grupoMetodo.selectedToggleProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) {
                grupoMetodo.selectToggle(viejo);
                return;
            }
            mostrarMetodo();
        });
        mostrarMetodo();

        txtRecibido.textProperty().addListener((obs, viejo, nuevo) -> calcularCambio());
        btnExacto.setOnAction(e -> ponerMonto(orden.getTotal()));
        double monto1 = redondear(orden.getTotal(), 100);
        double monto2 = redondear(monto1, 500);
        double monto3 = redondear(monto2, 1000);
        prepararMonto(btnMonto1, monto1);
        prepararMonto(btnMonto2, monto2);
        prepararMonto(btnMonto3, monto3);
        calcularCambio();

        chkCorreo.setSelected(false);
        txtCorreo.textProperty().addListener((obs, viejo, nuevo) -> chkCorreo.setSelected(nuevo != null && nuevo.contains("@")));

        lblClienteEstado.setText("Deja el NIT vacío para Consumidor Final");
        btnBuscarNit.setOnAction(e -> buscarNit());
        txtNit.setOnAction(e -> buscarNit());
        espera.setOnFinished(e -> buscarNit());
        txtNit.textProperty().addListener((obs, viejo, nuevo) -> nitCambio());

        btnCerrarCobro.setOnAction(e -> cerrar());
        btnCancelar.setOnAction(e -> cerrar());
        btnConfirmarVenta.setOnAction(e -> confirmar());
    }

    private void dibujarResumen() {
        lineasResumen.getChildren().clear();
        Map<ZonaMapa, List<AsientoMapa>> porZona = new LinkedHashMap<>();
        for (AsientoMapa a : orden.getAsientos()) {
            porZona.computeIfAbsent(orden.getZonas().get(a.getId()), z -> new ArrayList<>()).add(a);
        }

        int i = 0;
        for (Map.Entry<ZonaMapa, List<AsientoMapa>> e : porZona.entrySet()) {
            ZonaMapa z = e.getKey();
            List<AsientoMapa> lista = e.getValue();
            double precio = z.getPrecio() == null ? 0 : z.getPrecio();

            StringBuilder asientos = new StringBuilder();
            for (AsientoMapa a : lista) {
                asientos.append("  ").append(a.getBloque()).append("·").append(a.getFila()).append(a.getNumero());
            }

            Plantilla p = Plantilla.cargar("LineaResumen.fxml");
            p.<Region>get("color").setStyle("-fx-background-color: " + Formato.color(z.getColor(), i) + ";");
            p.<Label>get("lblZona").setText(z.getNombre());
            p.<Label>get("lblSubtotal").setText(Formato.dinero(precio * lista.size()));
            p.<Label>get("lblDetalle").setText(lista.size() + " × " + Formato.dineroCorto(precio) + " ·" + asientos);
            lineasResumen.getChildren().add(p.getRaiz());
            i++;
        }

        if (orden.getCantidades() == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> c : orden.getCantidades().entrySet()) {
            ZonaMapa z = orden.getZonasDePie().get(c.getKey());
            double precio = z.getPrecio() == null ? 0 : z.getPrecio();

            Plantilla p = Plantilla.cargar("LineaResumen.fxml");
            p.<Region>get("color").setStyle("-fx-background-color: " + Formato.color(z.getColor(), i) + ";");
            p.<Label>get("lblZona").setText(z.getNombre());
            p.<Label>get("lblSubtotal").setText(Formato.dinero(precio * c.getValue()));
            p.<Label>get("lblDetalle").setText(c.getValue() + " × " + Formato.dineroCorto(precio) + " · De pie");
            lineasResumen.getChildren().add(p.getRaiz());
            i++;
        }
    }

    private boolean esEfectivo() {
        return grupoMetodo.getSelectedToggle() == tglEfectivo;
    }

    private void mostrarMetodo() {
        boolean efectivo = esEfectivo();
        panelEfectivo.setVisible(efectivo);
        panelEfectivo.setManaged(efectivo);
        panelTarjeta.setVisible(!efectivo);
        panelTarjeta.setManaged(!efectivo);
        pintarIcono(tglEfectivo, efectivo);
        pintarIcono(tglTarjeta, !efectivo);
    }

    private void pintarIcono(ToggleButton boton, boolean activo) {
        Node icono = boton.getGraphic().lookup(".icon");
        if (icono != null) {
            icono.getStyleClass().removeAll("ic-amber", "ic-muted");
            icono.getStyleClass().add(activo ? "ic-amber" : "ic-muted");
        }
    }

    private double redondear(double monto, int multiplo) {
        double r = Math.ceil(monto / multiplo) * multiplo;
        return r == monto ? r + multiplo : r;
    }

    private void prepararMonto(Button boton, double monto) {
        boton.setText(Formato.dineroCorto(monto));
        boton.setOnAction(e -> ponerMonto(monto));
    }

    private void ponerMonto(double monto) {
        txtRecibido.setText(String.format("%.2f", monto));
    }

    private Double leerRecibido() {
        String texto = txtRecibido.getText() == null ? "" : txtRecibido.getText().replace(",", "").replace("Q", "").trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(texto);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void calcularCambio() {
        Double recibido = leerRecibido();
        lblCambio.getStyleClass().removeAll("c-green", "c-red");
        if (recibido == null) {
            lblCambio.setText("Q 0.00");
            lblCambio.getStyleClass().add("c-green");
        } else if (recibido < orden.getTotal()) {
            lblCambio.setText("Faltan " + Formato.dinero(orden.getTotal() - recibido));
            lblCambio.getStyleClass().add("c-red");
        } else {
            lblCambio.setText(Formato.dinero(recibido - orden.getTotal()));
            lblCambio.getStyleClass().add("c-green");
        }
    }

    private String nit() {
        String nit = txtNit.getText() == null ? "" : txtNit.getText().trim().toUpperCase();
        return nit.isEmpty() ? "CF" : nit;
    }

    private void nitCambio() {
        cliente = null;
        espera.stop();
        if (nombreAutomatico != null && nombreAutomatico.equals(txtCliente.getText())) {
            txtCliente.clear();
            nombreAutomatico = null;
        }

        String nit = limpiar(txtNit.getText());
        if (nit.isEmpty()) {
            lblClienteEstado.setText("Deja el NIT vacío para Consumidor Final");
        } else if (nit.equals("CF") || nitValido(nit)) {
            lblClienteEstado.setText("Buscando...");
            espera.playFromStart();
        } else if (nit.length() < 7) {
            lblClienteEstado.setText("Escribe el NIT completo");
        } else {
            lblClienteEstado.setText("NIT no válido · revisa el dígito verificador");
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.toUpperCase().replaceAll("[^0-9A-Z]", "");
    }

    private boolean nitValido(String nit) {
        if (nit.length() == 13 && nit.chars().allMatch(Character::isDigit)) {
            return true;
        }
        if (nit.length() < 2) {
            return false;
        }
        String cuerpo = nit.substring(0, nit.length() - 1);
        if (!cuerpo.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < cuerpo.length(); i++) {
            suma += (cuerpo.charAt(i) - '0') * (cuerpo.length() + 1 - i);
        }
        int resto = (11 - suma % 11) % 11;
        char esperado = resto == 10 ? 'K' : (char) ('0' + resto);
        return nit.charAt(nit.length() - 1) == esperado;
    }

    private void buscarNit() {
        espera.stop();
        String nit = nit();
        lblClienteEstado.setText("Buscando...");
        Fondo.cargar(() -> clienteService.buscarPorNit(nit), encontrado -> {
            if (!nit.equals(nit())) {
                return;
            }
            cliente = encontrado;
            if (cliente == null) {
                lblClienteEstado.setText("NIT no encontrado · escribe el nombre y se registrará al confirmar");
                return;
            }
            txtCliente.setText(cliente.getNombre());
            nombreAutomatico = cliente.getNombre();
            if (cliente.getCorreo() != null) {
                txtCorreo.setText(cliente.getCorreo());
            }
            lblClienteEstado.setText("Cliente encontrado · NIT " + cliente.getNit());
        }, ex -> {
            if (nit.equals(nit())) {
                lblClienteEstado.setText("No se pudo buscar: " + ex.getMessage());
            }
        });
    }

    private Cliente obtenerCliente() throws Exception {
        if (cliente != null) {
            return cliente;
        }
        Cliente encontrado = clienteService.buscarPorNit(nit());
        if (encontrado != null) {
            return encontrado;
        }
        String nombre = txtCliente.getText() == null ? "" : txtCliente.getText().trim();
        if (nombre.isEmpty()) {
            throw new Exception("El NIT " + nit() + " no está registrado, escribe el nombre del cliente");
        }
        return clienteService.crearCliente(nombre, nit(), txtCorreo.getText() == null ? "" : txtCorreo.getText().trim());
    }

    private void confirmar() {
        Map<String, Object> datos = new HashMap<>();
        if (esEfectivo()) {
            Double recibido = leerRecibido();
            if (recibido == null || recibido < orden.getTotal()) {
                Barra.alerta(Alert.AlertType.WARNING, "El monto recibido debe cubrir el total de " + Formato.dinero(orden.getTotal()));
                return;
            }
            datos.put("metodoPago", "EFECTIVO");
            datos.put("montoRecibido", recibido);
        } else {
            datos.put("metodoPago", "TARJETA");
        }

        List<Integer> asientos = new ArrayList<>();
        for (AsientoMapa a : orden.getAsientos()) {
            asientos.add(a.getId());
        }

        btnConfirmarVenta.setDisable(true);
        try {
            Cliente c = obtenerCliente();
            datos.put("usuarioId", Sesion.getUsuario().getId());
            if (chkCorreo.isSelected() && txtCorreo.getText().contains("@")) {
                datos.put("correo", txtCorreo.getText().trim());
            }
            datos.put("clienteId", c.getId());
            datos.put("conciertoId", orden.getConcierto().getId());
            datos.put("asientos", asientos);
            List<Map<String, Object>> cantidades = new ArrayList<>();
            if (orden.getCantidades() != null) {
                for (Map.Entry<Integer, Integer> pedido : orden.getCantidades().entrySet()) {
                    cantidades.add(Map.of("localidadId", pedido.getKey(), "cantidad", pedido.getValue()));
                }
            }
            datos.put("cantidades", cantidades);

            Venta venta = ventaService.crearVenta(datos);
            Sesion.setVenta(venta);
            cerrar();
        } catch (Exception ex) {
            Barra.alerta(Alert.AlertType.ERROR, ex.getMessage());
            btnConfirmarVenta.setDisable(false);
        }
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}

package com.umg.api.ui;

import com.umg.api.Sesion;
import com.umg.api.model.Boleto;
import com.umg.api.model.Concierto;
import com.umg.api.model.Orden;
import com.umg.api.model.Venta;
import com.umg.api.model.ZonaMapa;
import com.umg.api.service.VentaService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class ConfirmacionController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblDetalle;

    @FXML
    private Button btnImprimir;

    @FXML
    private Button btnReenviar;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblMetodo;

    @FXML
    private VBox statRecibido;

    @FXML
    private Label lblRecibido;

    @FXML
    private VBox statCambio;

    @FXML
    private Label lblCambio;

    @FXML
    private Label lblCantidad;

    @FXML
    private Label lblEntrega;

    @FXML
    private GridPane gridBoletos;

    @FXML
    private Button btnOtraVenta;

    @FXML
    private Button btnNuevaVenta;

    @FXML
    private void initialize() {
        Venta venta = Sesion.getVenta();
        Orden orden = Sesion.getOrden();
        Concierto concierto = orden.getConcierto();

        Barra.actualizarTurno();
        Barra.notificar("Venta " + venta.getNumero() + " registrada · " + venta.getBoletos().size() + " boletos · " + Formato.dinero(venta.getTotal()), "ok");
        btnReenviar.setOnAction(e -> reenviarCorreo(venta));
        btnNuevaVenta.setOnAction(e -> Barra.ir("Cartelera.fxml"));
        btnOtraVenta.setOnAction(e -> {
            Sesion.setVenta(null);
            Barra.ir("MapaAsientos.fxml");
        });
        btnImprimir.setOnAction(e -> imprimir());
        btnNuevaVenta.sceneProperty().addListener((obs, vieja, escena) -> {
            if (escena != null) {
                escena.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), btnNuevaVenta::fire);
            }
        });

        lblTitulo.setText("VENTA " + venta.getNumero() + " REGISTRADA");
        String cliente = venta.getCliente() == null ? "" : " · Cliente: " + venta.getCliente().getNombre();
        lblDetalle.setText(Formato.momento(venta.getFecha()) + " · " + venta.getVendedor() + cliente);

        lblTotal.setText(Formato.dinero(venta.getTotal()));
        boolean efectivo = "EFECTIVO".equals(venta.getMetodoPago());
        lblMetodo.setText(efectivo ? "Efectivo" : "Tarjeta (POS) · aut. " + venta.getAutorizacion());
        statRecibido.setVisible(efectivo);
        statRecibido.setManaged(efectivo);
        statCambio.setVisible(efectivo);
        statCambio.setManaged(efectivo);
        if (efectivo) {
            lblRecibido.setText(Formato.dinero(venta.getMontoRecibido() == null ? 0 : venta.getMontoRecibido()));
            lblCambio.setText(Formato.dinero(venta.getCambio() == null ? 0 : venta.getCambio()));
        }

        int cantidad = venta.getBoletos().size();
        lblCantidad.setText(cantidad + (cantidad == 1 ? " boleto generado" : " boletos generados"));
        mostrarEntrega(venta.getCorreo());

        Map<String, String> colores = new HashMap<>();
        int i = 0;
        for (ZonaMapa z : orden.getZonas().values()) {
            colores.putIfAbsent(z.getNombre(), Formato.color(z.getColor(), i));
            i++;
        }
        if (orden.getZonasDePie() != null) {
            for (ZonaMapa z : orden.getZonasDePie().values()) {
                colores.putIfAbsent(z.getNombre(), Formato.color(z.getColor(), i));
                i++;
            }
        }

        String fecha = Formato.fechaCorta(concierto.getFecha(), concierto.getHora())
                + (concierto.getEstadio() == null ? "" : " · " + concierto.getEstadio());
        int n = 0;
        for (Boleto b : venta.getBoletos()) {
            Plantilla p = Plantilla.cargar("Boleto.fxml");
            p.<Label>get("lblArtista").setText(concierto.getArtista() == null ? "" : concierto.getArtista().toUpperCase());
            p.<Label>get("lblFecha").setText(fecha);
            Label zona = p.get("lblZona");
            zona.setText(b.getLocalidad());
            zona.setStyle("-fx-background-color: " + colores.getOrDefault(b.getLocalidad(), Formato.color(null, n)) + ";");
            p.<Label>get("lblPrecio").setText(Formato.dineroCorto(b.getPrecio()));
            p.<Label>get("lblAsiento").setText(b.isDePie()
                    ? "Acceso general · De pie"
                    : "Bloque " + b.getBloque() + " · Fila " + b.getFila() + " · Asiento " + b.getNumero());
            p.<SVGPath>get("qr").setContent(Qr.svg(b.getCodigo(), 84));
            p.<Label>get("lblCodigo").setText(b.getCodigo());
            gridBoletos.add(p.getRaiz(), n % 3, n / 3);
            n++;
        }
    }

    private void mostrarEntrega(String correo) {
        if (correo == null || correo.isEmpty()) {
            lblEntrega.setText("Para imprimir en taquilla · cada boleto lleva un QR único para validar el acceso");
        } else {
            lblEntrega.setText("Impresos en taquilla · Enviados a " + correo + " · cada boleto lleva un QR único para validar el acceso");
        }
    }

    private void reenviarCorreo(Venta venta) {
        TextInputDialog dialogo = new TextInputDialog(venta.getCorreo() == null ? "" : venta.getCorreo());
        dialogo.setTitle("Enviar boletos por correo");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Correo del cliente:");
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || !respuesta.get().contains("@")) {
            return;
        }

        String correo = respuesta.get().trim();
        btnReenviar.setDisable(true);
        Fondo.cargar(() -> new VentaService().reenviar(venta.getId(), correo), mensaje -> {
            venta.setCorreo(correo);
            mostrarEntrega(correo);
            Barra.notificar(mensaje, "ok");
            btnReenviar.setDisable(false);
        }, ex -> {
            Barra.notificar(ex.getMessage(), "error");
            btnReenviar.setDisable(false);
        });
    }

    private void imprimir() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Barra.alerta(Alert.AlertType.WARNING, "No hay ninguna impresora configurada");
            return;
        }
        if (job.showPrintDialog(gridBoletos.getScene().getWindow()) && job.printPage(gridBoletos)) {
            job.endJob();
        }
    }
}

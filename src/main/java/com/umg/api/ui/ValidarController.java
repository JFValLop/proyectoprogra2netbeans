/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.umg.api.App;
import com.umg.api.service.BoletoService;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class ValidarController {

    @FXML
    private Label lblCamara;

    @FXML
    private StackPane cajaCamara;

    @FXML
    private ImageView imgCamara;

    @FXML
    private StackPane marco;

    @FXML
    private Region esquina1, esquina2, esquina3, esquina4;

    @FXML
    private Label lblGuia;

    private static final double RECUADRO = 0.6;

    @FXML
    private Button btnCamara;

    @FXML
    private VBox cajaResultado;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblDetalle;

    @FXML
    private TextField txtCodigo;

    @FXML
    private Button btnValidar;

    @FXML
    private Label lblContador;

    private static int validados = 0;

    private VideoCapture camara;
    private boolean encendida = false;
    private boolean ocupado = false;
    private String ultimoCodigo;

    @FXML
    private void initialize() {
        lblContador.setText(String.valueOf(validados));
        imgCamara.fitWidthProperty().bind(cajaCamara.widthProperty().subtract(20));
        imgCamara.fitHeightProperty().bind(cajaCamara.heightProperty().subtract(20));
        imgCamara.boundsInParentProperty().addListener((o, a, b) -> {
            double lado = Math.min(b.getWidth(), b.getHeight()) * RECUADRO;
            marco.setPrefSize(lado, lado);
        });
        colorMarco("#FFB020");

        btnCamara.setOnAction(e -> {
            if (encendida) {
                apagarCamara();
            } else {
                encenderCamara();
            }
        });
        btnValidar.setOnAction(e -> validarManual());
        txtCodigo.setOnAction(e -> validarManual());

        App.alSalir(() -> apagarCamara());
        Platform.runLater(() -> txtCodigo.requestFocus());
    }

    private void encenderCamara() {
        btnCamara.setDisable(true);
        lblCamara.setText("Encendiendo cámara...");

        Thread hilo = new Thread(() -> {
            try {
                OpenCV.loadLocally();
                camara = new VideoCapture(0);
            } catch (Throwable ex) {
                camara = null;
            }

            if (camara == null || !camara.isOpened()) {
                Platform.runLater(() -> {
                    lblCamara.setText("No se pudo abrir la cámara. Revisa que la app tenga permiso de cámara y vuelve a intentar.");
                    btnCamara.setDisable(false);
                });
                return;
            }

            encendida = true;
            Platform.runLater(() -> {
                lblCamara.setText("");
                marco.setVisible(true);
                lblGuia.setVisible(true);
                btnCamara.setText("Apagar cámara");
                btnCamara.setDisable(false);
            });

            Mat cuadro = new Mat();
            while (encendida) {
                if (camara.read(cuadro) && !cuadro.empty()) {
                    Image imagen = convertir(cuadro);
                    Platform.runLater(() -> imgCamara.setImage(imagen));

                    String codigo = leerQr(cuadro);
                    if (codigo != null && !codigo.equals(ultimoCodigo)) {
                        ultimoCodigo = codigo;
                        Platform.runLater(() -> validar(codigo));
                    }
                }
            }
            camara.release();
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    private void apagarCamara() {
        encendida = false;
        imgCamara.setImage(null);
        marco.setVisible(false);
        lblGuia.setVisible(false);
        lblCamara.setText("La cámara está apagada");
        btnCamara.setText("Encender cámara");
    }

    private Image convertir(Mat cuadro) {
        MatOfByte bytes = new MatOfByte();
        Imgcodecs.imencode(".bmp", cuadro, bytes);
        return new Image(new ByteArrayInputStream(bytes.toArray()));
    }

    private String leerQr(Mat cuadro) {
        try {
            Mat gris = new Mat();
            Imgproc.cvtColor(cuadro, gris, Imgproc.COLOR_BGR2GRAY);
            byte[] datos = new byte[(int) gris.total()];
            gris.get(0, 0, datos);

            int ancho = gris.cols();
            int alto = gris.rows();
            int lado = (int) (Math.min(ancho, alto) * RECUADRO);
            String codigo = decodificar(datos, ancho, alto, (ancho - lado) / 2, (alto - lado) / 2, lado, lado);
            if (codigo == null) {
                codigo = decodificar(datos, ancho, alto, 0, 0, ancho, alto);
            }
            return codigo;
        } catch (Exception ex) {
            return null;
        }
    }

    private String decodificar(byte[] datos, int ancho, int alto, int x, int y, int w, int h) {
        try {
            LuminanceSource fuente = new PlanarYUVLuminanceSource(datos, ancho, alto, x, y, w, h, false);
            Result resultado = new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(fuente)));
            return resultado.getText();
        } catch (Exception ex) {
            return null;
        }
    }

    private void colorMarco(String color) {
        esquina1.setStyle("-fx-border-color: " + color + "; -fx-border-width: 4 0 0 4; -fx-border-radius: 12 0 0 0;");
        esquina2.setStyle("-fx-border-color: " + color + "; -fx-border-width: 4 4 0 0; -fx-border-radius: 0 12 0 0;");
        esquina3.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 4 4; -fx-border-radius: 0 0 0 12;");
        esquina4.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 4 4 0; -fx-border-radius: 0 0 12 0;");
    }

    private void validarManual() {
        String codigo = txtCodigo.getText().trim();
        if (!codigo.isEmpty()) {
            validar(codigo);
            txtCodigo.clear();
        }
    }

    private void validar(String codigo) {
        if (ocupado) {
            return;
        }
        ocupado = true;
        mostrar("", "Validando...", codigo);

        Fondo.cargar(() -> new BoletoService().validar(codigo), datos -> {
            String estado = datos.path("estado").asText();
            String info = armarInfo(datos);

            if (estado.equals("VALIDO")) {
                validados++;
                lblContador.setText(String.valueOf(validados));
                mostrar("valido", "✔ PUEDE ENTRAR", info);
            } else if (estado.equals("USADO")) {
                mostrar("usado", "⚠ YA SE USÓ", "Entró el " + hora(datos.path("usadoEn").asText()) + "\n" + info);
            } else {
                mostrar("no-existe", "✖ NO VÁLIDO", datos.path("message").asText("Este código no existe"));
            }
            esperar();
        }, ex -> {
            mostrar("no-existe", "Sin conexión", "No se pudo hablar con la API");
            esperar();
        });
    }

    private String armarInfo(JsonNode datos) {
        String info = datos.path("artista").asText("");
        if (!datos.path("gira").asText("").isEmpty()) {
            info += " · " + datos.path("gira").asText();
        }
        String localidad = datos.path("localidad").asText("");
        info += "\n" + (localidad.isEmpty() ? "" : localidad + " · ") + datos.path("lugar").asText("");
        info += "\n" + datos.path("codigo").asText("") + " · " + datos.path("venta").asText("");
        return info;
    }

    private String hora(String fecha) {
        try {
            OffsetDateTime f = OffsetDateTime.parse(fecha);
            return f.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception ex) {
            return fecha;
        }
    }

    private void esperar() {
        PauseTransition pausa = new PauseTransition(Duration.seconds(3));
        pausa.setOnFinished(e -> ocupado = false);
        pausa.play();
    }

    private void mostrar(String tipo, String titulo, String detalle) {
        lblEstado.setText(titulo);
        lblDetalle.setText(detalle);
        lblEstado.getStyleClass().removeAll("c-green", "c-amber", "c-red");

        if (tipo.equals("valido")) {
            lblEstado.getStyleClass().add("c-green");
            cajaResultado.setStyle("-fx-background-color: rgba(91,214,138,0.15); -fx-border-color: #5BD68A;");
            colorMarco("#5BD68A");
        } else if (tipo.equals("usado")) {
            lblEstado.getStyleClass().add("c-amber");
            cajaResultado.setStyle("-fx-background-color: rgba(255,176,32,0.15); -fx-border-color: #FFB020;");
            colorMarco("#FFB020");
        } else if (tipo.equals("no-existe")) {
            lblEstado.getStyleClass().add("c-red");
            cajaResultado.setStyle("-fx-background-color: rgba(255,106,85,0.15); -fx-border-color: #FF6A55;");
            colorMarco("#FF6A55");
        } else {
            cajaResultado.setStyle("");
            colorMarco("#FFB020");
        }
    }
}

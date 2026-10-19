/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import com.umg.api.model.ZonaMapa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class PlanoRecinto {

    public interface Pintor {

        void asiento(Button boton, ZonaMapa zona, String bloque, int fila, int numero);

        void zona(VBox caja, ZonaMapa zona, boolean compacta);

        double opacidad(ZonaMapa zona);
    }

    public static final String FILAS = "ABCDEFGHIJKLMNOP";
    private static final double W = 750;
    private static final double CX = 375;

    private final Map<String, ZonaMapa> zonas = new HashMap<>();
    private final Pintor pintor;
    private final List<Node> fondo = new ArrayList<>();
    private final List<Node> encima = new ArrayList<>();
    private final List<Node> areas = new ArrayList<>();
    private final List<Node> bloques = new ArrayList<>();
    private Node escenario;
    private double ancho = W;
    private double alto;

    private PlanoRecinto(List<ZonaMapa> lista, Pintor pintor) {
        for (ZonaMapa z : lista) {
            if (z.getCodigo() != null) {
                zonas.put(z.getCodigo(), z);
            }
        }
        this.pintor = pintor;
    }

    public static Pane dibujar(String plano, List<ZonaMapa> lista, Pintor pintor) {
        PlanoRecinto p = new PlanoRecinto(lista, pintor);
        if ("ESTADIO".equals(plano)) {
            p.estadio();
        } else if ("EXPLANADA".equals(plano)) {
            p.explanada();
        } else {
            p.teatro();
        }
        Pane pane = new Pane();
        pane.getChildren().addAll(p.fondo);
        pane.getChildren().addAll(p.areas);
        pane.getChildren().addAll(p.encima);
        pane.getChildren().addAll(p.bloques);
        if (p.escenario != null) {
            pane.getChildren().add(p.escenario);
        }
        pane.setMinSize(p.ancho, p.alto);
        pane.setPrefSize(p.ancho, p.alto);
        pane.setMaxSize(p.ancho, p.alto);
        return pane;
    }

    public static List<String> bloquesDe(ZonaMapa z) {
        List<String> lista = new ArrayList<>();
        if ("ZONA".equals(z.getTipo())) {
            return lista;
        }
        if ("CAB".equals(z.getCodigo())) {
            lista.add("N");
            lista.add("S");
            return lista;
        }
        if ("PALCOS".equals(z.getTipo())) {
            int porLado = z.getPorLado() == null ? 0 : z.getPorLado();
            for (int i = 1; i <= porLado; i++) {
                lista.add("I" + i);
            }
            for (int i = 1; i <= porLado; i++) {
                lista.add("D" + i);
            }
            return lista;
        }
        lista.add("A");
        lista.add("B");
        lista.add("C");
        return lista;
    }

    public static String rgba(String hex, double alfa) {
        Color c = Color.web(hex);
        return "rgba(" + Math.round(c.getRed() * 255) + "," + Math.round(c.getGreen() * 255) + "," + Math.round(c.getBlue() * 255) + "," + alfa + ")";
    }

    private static double span(int n) {
        return n * 15 + (n - 1) * 4;
    }

    private boolean comoDePie(ZonaMapa z) {
        return !"ZONA".equals(z.getTipo()) && z.esDePie() && z.getPrecio() != null;
    }

    private void teatro() {
        String[] codigos = {"VIP", "PLA", "BAL"};
        double[][] geo = {{24, -6, 7}, {26, -16, 9}, {20, -26, 12}};
        Map<String, double[]> pos = new HashMap<>();
        double y = 70;
        Double divisor = null;
        for (String id : codigos) {
            ZonaMapa l = zonas.get(id);
            if (l == null) {
                continue;
            }
            double w = span(l.getAsientos_por_fila());
            double h = 14 + span(l.getFilas());
            if (id.equals("BAL")) {
                divisor = y + 2;
                y += 22;
            }
            pos.put(id, new double[]{y, w, h});
            y += h + 18;
        }

        List<Object[]> items = new ArrayList<>();
        for (int k = 0; k < codigos.length; k++) {
            ZonaMapa l = zonas.get(codigos[k]);
            if (l == null) {
                continue;
            }
            double[] g = geo[k];
            double[] p = pos.get(codigos[k]);
            if (comoDePie(l)) {
                items.add(new Object[]{l, null, "", new double[]{CX - 1.5 * p[1] - g[0], p[0] + g[1], 0, 3 * p[1] + 2 * g[0], p[2] - g[1]}, "18 18 30 30"});
                continue;
            }
            String[] letras = {"A", "B", "C"};
            for (int bi = 0; bi < 3; bi++) {
                String lado = bi == 0 ? "L" : bi == 2 ? "R" : "";
                double cx = CX + (bi - 1) * (p[1] + g[0]);
                double left = cx - p[1] / 2 - (lado.equals("L") ? 14 : 0);
                double top = p[0] + (lado.isEmpty() ? 0 : g[1]);
                double rot = lado.equals("L") ? g[2] : lado.equals("R") ? -g[2] : 0;
                items.add(new Object[]{l, letras[bi], lado, new double[]{left, top, rot, p[1] + (lado.isEmpty() ? 0 : 14), p[2]}, null});
            }
        }

        ZonaMapa pl = zonas.get("PAL");
        int porLado = pl == null || pl.getPorLado() == null ? 0 : pl.getPorLado();
        double bw = pl == null ? 0 : span(pl.getAsientos_por_fila()) + 10;
        double bh = pl == null ? 0 : 24 + span(pl.getFilas());
        double vipTop = pos.containsKey("VIP") ? pos.get("VIP")[0] : 70;

        double necesita = 0;
        for (int k = 0; k < porLado; k++) {
            double[] pb = caja(new double[]{12, vipTop + 2 + k * (bh + 10), 14, bw, bh});
            for (Object[] it : items) {
                double[] fb = caja((double[]) it[3]);
                if (fb[2] < pb[3] + 4 && fb[3] > pb[2] - 4 && pb[1] + 10 > fb[0]) {
                    necesita = Math.max(necesita, pb[1] + 10 - fb[0]);
                }
            }
        }
        double ex = Math.ceil(necesita);
        ancho = W + 2 * ex;

        for (Object[] it : items) {
            double[] g = (double[]) it[3];
            g[0] += ex;
            ZonaMapa l = (ZonaMapa) it[0];
            if (it[1] == null) {
                areas.add(area(l, g, (String) it[4], false));
            } else {
                bloques.add(bloque(l, (String) it[1], (String) it[2], g, false));
            }
        }
        for (int i = 0; i < porLado * 2; i++) {
            boolean izquierda = i < porLado;
            int k = i % porLado;
            double[] g = {izquierda ? 12 : ancho - 12 - bw, vipTop + 2 + k * (bh + 10), izquierda ? 14 : -14, bw, bh};
            bloques.add(bloque(pl, (izquierda ? "I" : "D") + (k + 1), "", g, false));
        }

        alto = Math.max(y - 18 + 14, vipTop + 2 + porLado * (bh + 10) + 10);
        escenario = escenario(215 + ex, 8, 320, 44, "8 8 160 160 / 8 8 36 36");
        if (divisor != null) {
            Region linea = new Region();
            linea.getStyleClass().add("divider-dash");
            linea.setLayoutX(40 + ex);
            linea.setLayoutY(divisor);
            medir(linea, W - 80, 1);
            fondo.add(linea);
            Label nivel = new Label("NIVEL 2 · " + zonas.get("BAL").getNombre().toUpperCase());
            nivel.getStyleClass().add("divider-lbl");
            nivel.setAlignment(Pos.CENTER);
            nivel.setLayoutX((ancho - 120) / 2);
            nivel.setLayoutY(divisor - 7);
            nivel.setPrefWidth(120);
            fondo.add(nivel);
        }
    }

    private void estadio() {
        ZonaMapa tri = zonas.get("TRI");
        ZonaMapa pre = zonas.get("PRE");
        ZonaMapa cab = zonas.get("CAB");
        double gap = 8;
        double dT = tri == null ? 0 : 14 + span(tri.getFilas());
        double dP = pre == null ? 0 : 14 + span(pre.getFilas());
        double lS = Math.max(tri == null ? 0 : span(tri.getAsientos_por_fila()), pre == null ? 0 : span(pre.getAsientos_por_fila()));
        double cW = cab == null ? 0 : span(cab.getAsientos_por_fila());
        double cH = cab == null ? 0 : 14 + span(cab.getFilas());
        double pT = 24 + cH + 12;
        double pL = 24 + dT + 12;
        double pR = W - 24 - dP - 12;
        double pW = pR - pL;
        double pH = Math.max(3 * lS + 2 * gap, 300);
        double sT = pT + pH + 12;
        alto = sT + cH + 24;
        double midY = pT + pH / 2;
        double fL = pL + 14;
        double fT = pT + 14;
        double fW = pW - 28;
        double fH = pH - 28;

        fondo.add(decoracion(10, 8, W - 20, alto - 16, "120", "#1A1917", "#2A2825", false, null));
        fondo.add(decoracion(pL, pT, pW, pH, "34", "#211F1B", "#34322D", false, null));
        fondo.add(decoracion(fL, fT, fW, fH, "20", "rgba(91,214,138,0.045)", "rgba(91,214,138,0.16)", false, null));
        escenario = escenario(CX - 65, midY - 35, 130, 70, "14");

        double gW = Math.min(280, fW - 110);
        double gH = Math.min(190, fH - 110);
        if (zonas.containsKey("CAN")) {
            areas.add(area(zonas.get("CAN"), new double[]{fL + 12, fT + 12, 0, fW - 24, fH - 24}, "14", true));
        }
        if (zonas.containsKey("GLD")) {
            areas.add(area(zonas.get("GLD"), new double[]{CX - gW / 2, midY - gH / 2, 0, gW, gH}, "16", true));
        }
        if (tri != null) {
            lado(tri, pL - 12 - dT, dT, 90, new String[]{"A", "B", "C"}, midY, gap);
        }
        if (pre != null) {
            lado(pre, pR + 12, dP, -90, new String[]{"C", "B", "A"}, midY, gap);
        }
        if (cab != null) {
            bloques.add(bloque(cab, "N", "", new double[]{CX - cW / 2, 24, 0, cW, cH}, true));
            bloques.add(bloque(cab, "S", "", new double[]{CX - cW / 2, sT, 0, cW, cH}, false));
        }
    }

    private void lado(ZonaMapa l, double vx, double fondoLado, double rot, String[] orden, double midY, double gap) {
        double largo = span(l.getAsientos_por_fila());
        double total = 3 * largo + 2 * gap;
        double top0 = midY - total / 2;
        if (comoDePie(l)) {
            areas.add(area(l, new double[]{vx, top0, 0, fondoLado, total}, "12", false));
            return;
        }
        for (int i = 0; i < orden.length; i++) {
            double vy = top0 + i * (largo + gap);
            bloques.add(bloque(l, orden[i], "", new double[]{vx + fondoLado / 2 - largo / 2, vy + largo / 2 - fondoLado / 2, rot, largo, fondoLado}, false));
        }
    }

    private void explanada() {
        ZonaMapa gv = zonas.get("GVP");
        double bw = gv == null ? 0 : span(gv.getAsientos_por_fila());
        double bh = gv == null ? 0 : 14 + span(gv.getFilas());
        double gvT = 432;
        alto = gvT + bh + 48;
        String rayas = "linear-gradient(from 0px 0px to 5.66px 5.66px, repeat, #26241F 0%, #26241F 50%, #1F1D1A 50%, #1F1D1A 100%)";

        fondo.add(decoracion(20, 18, 710, alto - 36, "28", "#1A1917", "#34322D", true, null));
        fondo.add(decoracion(142, 44, 72, 26, "4", "#232220", "#3A3833", false, "PANTALLA"));
        fondo.add(decoracion(536, 44, 72, 26, "4", "#232220", "#3A3833", false, "PANTALLA"));
        fondo.add(decoracion(38, 250, 40, 120, "6", rayas, "#33312C", false, "BAR"));
        fondo.add(decoracion(672, 250, 40, 120, "6", rayas, "#33312C", false, "BAR"));
        encima.add(decoracion(345, 372, 60, 26, "4", "#2A2825", "#46433D", false, "FOH"));
        fondo.add(decoracion(315, gvT + bh + 10, 120, 18, "4", "transparent", "transparent", false, "▼ ACCESO"));
        escenario = escenario(225, 30, 300, 48, "8 8 150 150 / 8 8 30 30");

        if (zonas.containsKey("FRS")) {
            areas.add(area(zonas.get("FRS"), new double[]{180, 96, 0, 390, 100}, "16 16 50 50 / 16 16 24 24", false));
        }
        if (zonas.containsKey("GEN")) {
            areas.add(area(zonas.get("GEN"), new double[]{92, 208, 0, 566, 212}, "20 20 40 40", false));
        }
        if (gv == null) {
            return;
        }
        if (comoDePie(gv)) {
            areas.add(area(gv, new double[]{62, gvT, 0, 626, bh}, "12", false));
            return;
        }
        String[] letras = {"A", "B", "C"};
        for (int bi = 0; bi < 3; bi++) {
            String lado = bi == 0 ? "L" : bi == 2 ? "R" : "";
            double cx = CX + (bi - 1) * (bw + 26);
            double[] g = {cx - bw / 2 - (lado.equals("L") ? 14 : 0), gvT + (lado.isEmpty() ? 0 : -6), lado.equals("L") ? 5 : lado.equals("R") ? -5 : 0, bw + (lado.isEmpty() ? 0 : 14), bh};
            bloques.add(bloque(gv, letras[bi], lado, g, false));
        }
    }

    private static double[] caja(double[] g) {
        double r = Math.toRadians(Math.abs(g[2]));
        double cs = Math.cos(r);
        double sn = Math.sin(r);
        double hw = g[3] / 2 * cs + g[4] / 2 * sn;
        double hh = g[3] / 2 * sn + g[4] / 2 * cs;
        double mx = g[0] + g[3] / 2;
        double my = g[1] + g[4] / 2;
        return new double[]{mx - hw, mx + hw, my - hh, my + hh};
    }

    private static void medir(Region r, double w, double h) {
        r.setMinSize(w, h);
        r.setPrefSize(w, h);
        r.setMaxSize(w, h);
    }

    private Label escenario(double left, double top, double w, double h, String radio) {
        Label l = new Label("ESCENARIO");
        l.getStyleClass().add("stage");
        l.setAlignment(Pos.CENTER);
        l.setLayoutX(left);
        l.setLayoutY(top);
        medir(l, w, h);
        l.setStyle("-fx-background-radius: " + radio + "; -fx-border-radius: " + radio + ";");
        return l;
    }

    private Label decoracion(double left, double top, double w, double h, String radio, String fondoColor, String borde, boolean punteado, String texto) {
        Label l = new Label(texto == null ? "" : texto);
        l.setAlignment(Pos.CENTER);
        l.getStyleClass().addAll("s9", "w700", "c-muted");
        l.setLayoutX(left);
        l.setLayoutY(top);
        medir(l, w, h);
        l.setMouseTransparent(true);
        l.setStyle("-fx-background-color: " + fondoColor + "; -fx-background-radius: " + radio + "; -fx-border-color: " + borde
                + "; -fx-border-radius: " + radio + ";" + (punteado ? " -fx-border-style: segments(6, 5);" : ""));
        return l;
    }

    private VBox area(ZonaMapa l, double[] g, String radio, boolean banda) {
        boolean compacta = banda || g[4] < 90 || g[3] < 200;
        VBox caja = new VBox(compacta ? 3 : 6);
        caja.getStyleClass().add("zona");
        caja.setLayoutX(g[0]);
        caja.setLayoutY(g[1]);
        medir(caja, g[3], g[4]);
        caja.setAlignment(banda ? Pos.TOP_CENTER : Pos.CENTER);
        caja.setPadding(new Insets(banda ? 8 : 0, 0, 0, 0));
        caja.setStyle("-fx-background-color: linear-gradient(from 0px 0px to 14.14px 14.14px, repeat, "
                + rgba(l.getColor(), 0.2) + " 0%, " + rgba(l.getColor(), 0.2) + " 50%, " + rgba(l.getColor(), 0.08) + " 50%, " + rgba(l.getColor(), 0.08) + " 100%);"
                + " -fx-background-radius: " + radio + "; -fx-border-color: " + l.getColor() + "; -fx-border-width: 1.5;"
                + " -fx-border-style: segments(6, 4); -fx-border-radius: " + radio + ";");

        Label nombre = new Label(l.getNombre().toUpperCase() + ("ZONA".equals(l.getTipo()) ? "" : " · DE PIE"));
        nombre.getStyleClass().add("disp");
        nombre.setStyle("-fx-font-size: " + (banda ? 20 : compacta ? 17 : 24) + "px; -fx-text-fill: " + l.getColor() + ";");
        caja.getChildren().add(nombre);
        caja.setOpacity(pintor.opacidad(l));
        pintor.zona(caja, l, compacta);
        return caja;
    }

    private VBox bloque(ZonaMapa l, String b, String lado, double[] g, boolean invertido) {
        boolean palco = "PALCOS".equals(l.getTipo());
        VBox caja = new VBox(4);
        caja.setLayoutX(g[0]);
        caja.setLayoutY(g[1]);
        caja.setRotate(g[2]);
        caja.setAlignment(lado.equals("L") ? Pos.TOP_RIGHT : lado.equals("R") ? Pos.TOP_LEFT : Pos.TOP_CENTER);
        caja.setOpacity(pintor.opacidad(l));
        if (palco) {
            caja.setPadding(new Insets(4));
            caja.setStyle("-fx-background-color: " + rgba(l.getColor(), 0.07) + "; -fx-border-color: " + rgba(l.getColor(), 0.3)
                    + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        }

        String titulo;
        if (palco) {
            titulo = "PALCO " + b;
        } else if ("CAB".equals(l.getCodigo())) {
            titulo = "CABECERA " + (b.equals("N") ? "NORTE" : "SUR");
        } else {
            titulo = l.getNombre().toUpperCase() + " " + b;
        }
        Label caption = new Label(titulo);
        caption.getStyleClass().add("map-caption");
        caption.setStyle("-fx-text-fill: " + l.getColor() + ";");

        List<Node> filas = new ArrayList<>();
        for (int r = 0; r < l.getFilas(); r++) {
            HBox fila = new HBox(4);
            fila.setAlignment(Pos.CENTER_LEFT);
            String letra = String.valueOf(FILAS.charAt(r));
            if (lado.equals("L")) {
                fila.getChildren().add(etiqueta(letra, Pos.CENTER_RIGHT));
            }
            for (int n = 1; n <= l.getAsientos_por_fila(); n++) {
                Button asiento = new Button();
                asiento.getStyleClass().add("seat");
                asiento.setMnemonicParsing(false);
                asiento.setFocusTraversable(false);
                pintor.asiento(asiento, l, b, r, n);
                fila.getChildren().add(asiento);
            }
            if (lado.equals("R")) {
                fila.getChildren().add(etiqueta(letra, Pos.CENTER_LEFT));
            }
            filas.add(fila);
        }

        if (invertido) {
            caja.getChildren().addAll(filas);
            caja.getChildren().add(caption);
        } else {
            caja.getChildren().add(caption);
            caja.getChildren().addAll(filas);
        }
        return caja;
    }

    private static Label etiqueta(String letra, Pos alineacion) {
        Label l = new Label(letra);
        l.getStyleClass().add("row-lbl");
        l.setMinWidth(10);
        l.setPrefWidth(10);
        l.setAlignment(alineacion);
        return l;
    }
}

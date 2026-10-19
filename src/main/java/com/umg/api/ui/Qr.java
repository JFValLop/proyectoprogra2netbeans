/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Map;

/**
 *
 * @author juanfranciscovaldezlopez
 */
public class Qr {

    public static String svg(String texto, double tamano) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 0, 0, Map.of(EncodeHintType.MARGIN, 0));
            double modulo = tamano / matriz.getWidth();
            StringBuilder ruta = new StringBuilder();
            for (int y = 0; y < matriz.getHeight(); y++) {
                for (int x = 0; x < matriz.getWidth(); x++) {
                    if (matriz.get(x, y)) {
                        ruta.append(String.format(java.util.Locale.US, "M%.2f %.2fh%.2fv%.2fh-%.2fz", x * modulo, y * modulo, modulo, modulo, modulo));
                    }
                }
            }
            return ruta.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}

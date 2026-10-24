/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.api.reportes;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 *
 * @author Misael
 */
public class Db {
    private static final Properties CFG = new Properties();

    static {
        try (InputStream in = Db.class.getResourceAsStream("/com/umg/api/reportes/db.properties")) {
            if (in == null) throw new IllegalStateException("Falta db.properties");
            CFG.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                CFG.getProperty("url"), CFG.getProperty("user"), CFG.getProperty("password"));
    }
}

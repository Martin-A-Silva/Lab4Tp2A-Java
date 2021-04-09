package com.msilva;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Main {

    public static void main(String[] args) {
        try {
            //Creamos la conexión a la bbdd
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db_lab4tp2", "root", "sasa");
            Statement stmt = con.createStatement();
            ResultSet rs;
            JSONObject json;
            for (int codigoPais = 1; codigoPais < 301; codigoPais++) {
                try {

                    //Usando las bibliotecas de apache commons io y org.json traigo de la URL el json correspondiente
                    json = new JSONArray(
                            IOUtils.toString(new URL("https://restcountries.eu/rest/v2/callingcode/" + codigoPais),
                                    StandardCharsets.UTF_8))
                            .getJSONObject(0);


                    //Si el código existe en restcountreis:

                    rs = stmt.executeQuery("select count(*) as conteo from pais where codigoPais = " + codigoPais);
                    rs.next();  //Si se borra esta linea, la siguiente da error
                    if (rs.getInt("conteo") == 1) {
                        //Si el código existe en restcountries y en la bbdd, actualizar:
                        System.out.println("Código " + codigoPais + " existe en la bbdd, actualizando...");
                        stmt.executeUpdate(String.format("update pais set " +
                                        "nombrePais = \"%s\"," +
                                        "capitalPais = \"%s\", " +
                                        "region = '%s', " +
                                        "poblacion = '%s', " +
                                        "latitud = '%s', " +
                                        "longitud = '%s' " +
                                        "where codigoPais = '%s'",
                                json.get("name"),
                                json.get("capital"),
                                json.get("region"),
                                json.get("population"),
                                json.get("latlng").toString().split(",")[0].substring(1),
                                json.get("latlng").toString().split(",")[1].split("]")[0],
                                codigoPais

                        ));

                    } else {
                        //Si código existe en restcountries pero no en la bbdd, insertar:
                        System.out.println("Código " + codigoPais + " no existe en la bbdd, insertando...");
                        stmt.executeUpdate("insert into pais " +
                                "(codigoPais, nombrePais, capitalPais, region, poblacion, latitud, longitud)" +
                                String.format("values ('%s', \"%s\", \"%s\", '%s', '%s', '%s', '%s')",
                                        codigoPais,
                                        json.get("name"),
                                        json.get("capital"),
                                        json.get("region"),
                                        json.get("population"),
                                        json.get("latlng").toString().split(",")[0].substring(1),
                                        json.get("latlng").toString().split(",")[1].split("]")[0]));
                    }
                } catch (Exception e) {
                    //Si el código no corresponde a un país en restcountries
                    if (e.getClass().equals(FileNotFoundException.class)) {
                        System.out.println("Codigo " + codigoPais + " no existe en restcountries");
                    } else {
                        System.out.println(e);
                    }
                }
            }

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

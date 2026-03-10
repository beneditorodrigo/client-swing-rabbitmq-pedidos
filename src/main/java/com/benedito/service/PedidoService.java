package com.benedito.service;

import com.benedito.model.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class PedidoService {

    private static final String API_URL = "http://localhost:8080/api/pedidos";

    private ObjectMapper mapper = new ObjectMapper();

    public UUID enviarPedido(Pedido pedido) throws Exception {

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = mapper.writeValueAsString(pedido);

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();

        int responseCode = conn.getResponseCode();

        if (responseCode == 202) {

            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return UUID.fromString(response.replace("\"", ""));
        }

        throw new RuntimeException("Erro ao enviar pedido");
    }

    public String consultarStatus(UUID id) throws Exception {

        URL url = new URL(API_URL + "/status/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        Scanner scanner = new Scanner(conn.getInputStream());
        String status = scanner.useDelimiter("\\A").next();
        scanner.close();

        return status.replace("\"", "");
    }
}

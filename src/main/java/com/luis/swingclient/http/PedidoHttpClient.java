package com.luis.swingclient.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luis.swingclient.model.Pedido;
import com.luis.swingclient.model.StatusResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PedidoHttpClient {

    private final ObjectMapper objectMapper;
    private final String baseUrl = "http://localhost:8080/api/pedidos";

    public PedidoHttpClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void enviarPedido(Pedido pedido) throws Exception {
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String json = objectMapper.writeValueAsString(pedido);

        OutputStream os = connection.getOutputStream();
        os.write(json.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();

        if (responseCode != 202) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = br.readLine()) != null) {
                resposta.append(linha);
            }
            br.close();
            throw new RuntimeException("Erro ao enviar pedido: " + resposta.toString());
        }

        connection.disconnect();
    }

    public StatusResponse consultarStatus(UUID id) throws Exception {
        URL url = new URL(baseUrl + "/status/" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == 404) {
            connection.disconnect();
            return null;
        }

        if (responseCode != 200) {
            throw new RuntimeException("Erro ao consultar status. HTTP: " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder resposta = new StringBuilder();
        String linha;
        while ((linha = br.readLine()) != null) {
            resposta.append(linha);
        }
        br.close();

        connection.disconnect();

        return objectMapper.readValue(resposta.toString(), StatusResponse.class);
    }
}

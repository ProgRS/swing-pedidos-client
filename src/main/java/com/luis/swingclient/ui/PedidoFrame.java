package com.luis.swingclient.ui;

import com.luis.swingclient.http.PedidoHttpClient;
import com.luis.swingclient.model.Pedido;
import com.luis.swingclient.model.StatusResponse;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PedidoFrame extends JFrame {

    private final JTextField produtoField = new JTextField(20);
    private final JTextField quantidadeField = new JTextField(5);
    private final JButton enviarButton = new JButton("Enviar Pedido");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Produto", "Quantidade", "Status", "Erro"}, 0
    );

    private final JTable tabela = new JTable(tableModel);
    private final PedidoHttpClient httpClient = new PedidoHttpClient();

    private final Map<UUID, Integer> linhasPorPedido = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> pendentes = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    public PedidoFrame() {
        setTitle("Sistema de Pedidos");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formPanel = new JPanel();
        formPanel.add(new JLabel("Produto:"));
        formPanel.add(produtoField);
        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(quantidadeField);
        formPanel.add(enviarButton);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        enviarButton.addActionListener(e -> enviarPedido());

        iniciarPolling();
    }

    private void enviarPedido() {
        String produto = produtoField.getText().trim();
        String quantidadeTexto = quantidadeField.getText().trim();

        if (produto.isEmpty() || quantidadeTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha produto e quantidade.");
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(quantidadeTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser numérica.");
            return;
        }

        final Pedido pedido = new Pedido(
                UUID.randomUUID(),
                produto,
                quantidade,
                LocalDateTime.now()
        );

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                httpClient.enviarPedido(pedido);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    int row = tableModel.getRowCount();
                    tableModel.addRow(new Object[]{
                            pedido.getId(),
                            pedido.getProduto(),
                            pedido.getQuantidade(),
                            "ENVIADO, AGUARDANDO PROCESSO",
                            ""
                    });

                    linhasPorPedido.put(pedido.getId(), row);
                    pendentes.add(pedido.getId());

                    produtoField.setText("");
                    quantidadeField.setText("");

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PedidoFrame.this,
                            "Erro ao enviar pedido: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void iniciarPolling() {
        Timer timer = new Timer(4000, e -> {
            List<UUID> ids = new ArrayList<UUID>(pendentes);

            for (UUID id : ids) {
                SwingWorker<StatusResponse, Void> worker = new SwingWorker<StatusResponse, Void>() {
                    @Override
                    protected StatusResponse doInBackground() throws Exception {
                        return httpClient.consultarStatus(id);
                    }

                    @Override
                    protected void done() {
                        try {
                            StatusResponse status = get();
                            if (status == null) {
                                return;
                            }

                            SwingUtilities.invokeLater(() -> atualizarLinha(id, status));

                            if ("SUCESSO".equals(status.getStatus()) || "FALHA".equals(status.getStatus())) {
                                pendentes.remove(id);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                };

                worker.execute();
            }
        });

        timer.start();
    }

    private void atualizarLinha(UUID id, StatusResponse status) {
        Integer row = linhasPorPedido.get(id);

        if (row != null) {
            tableModel.setValueAt(status.getStatus(), row, 3);
            tableModel.setValueAt(status.getMensagemErro() != null ? status.getMensagemErro() : "", row, 4);
        }
    }
}

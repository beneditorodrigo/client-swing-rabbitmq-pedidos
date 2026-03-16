package com.benedito.view;

import com.benedito.model.Pedido;
import com.benedito.service.PedidoService;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PedidoFrame extends JFrame {

    private JTextField produtoField;
    private JTextField quantidadeField;
    private JButton enviarButton;
    private JTextArea statusArea;

    private PedidoService pedidoService = new PedidoService();
    private Map<UUID, String> pedidos = new ConcurrentHashMap<>();
    private Timer pollingTimer;

    public PedidoFrame() {

        setTitle("Cliente de Pedidos");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        iniciarPolling();
    }

    private void initComponents() {

        produtoField = new JTextField(20);
        quantidadeField = new JTextField(5);
        enviarButton = new JButton("Enviar Pedido");
        statusArea = new JTextArea();

        statusArea.setEditable(false);

        JPanel formPanel = new JPanel();
        formPanel.add(new JLabel("Produto:"));
        formPanel.add(produtoField);

        formPanel.add(new JLabel("Quantidade:"));
        formPanel.add(quantidadeField);

        formPanel.add(enviarButton);

        JScrollPane scroll = new JScrollPane(statusArea);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        enviarButton.addActionListener(e -> enviarPedido());
    }

    private void iniciarPolling() {

        pollingTimer = new Timer(5000, e -> verificarStatusPedidos());

        pollingTimer.start();
    }

    private void verificarStatusPedidos() {

        for (UUID id : pedidos.keySet()) {

            String statusAtual = pedidos.get(id);

            if ("AGUARDANDO PROCESSO".equals(statusAtual) || "PROCESSANDO".equals(statusAtual)) {

                try {
                    String novoStatus = pedidoService.consultarStatus(id);

                    if (!novoStatus.equals(statusAtual)) {

                        pedidos.put(id, novoStatus);

                        SwingUtilities.invokeLater(() ->
                            statusArea.append("Pedido " + id + " → " + novoStatus + "\n")
                        );

                    }

                } catch (Exception ignored) {
                }
            }
        }
    }

    private void enviarPedido() {

        try {
            String produto = produtoField.getText();

            if (produto.isEmpty()){
                JOptionPane.showMessageDialog(this, "Informe o produto.");
                return;
            }

            int quantidade = Integer.parseInt(quantidadeField.getText());

            if (quantidade <= 0) {
                JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero.");
                return;
            }

            Pedido pedido = new Pedido(
                    UUID.randomUUID(),
                    produto,
                    quantidade,
                    LocalDateTime.now()
            );

            UUID id = pedidoService.enviarPedido(pedido);

            pedidos.put(id, "AGUARDANDO PROCESSO");
            statusArea.append("Pedido " + id + " → ENVIADO, AGUARDANDO PROCESSO\n");

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Quantidade inválida.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {

            String mensagem = "Erro ao enviar pedido.";

            if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
                mensagem = "Servidor backend não está rodando.";
            }

            JOptionPane.showMessageDialog(
                    this,
                    mensagem,
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

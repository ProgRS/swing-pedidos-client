# Swing Pedidos Client - Teste Prático Java

Cliente desktop desenvolvido em **Java 8** com **Swing** para envio de pedidos ao backend e acompanhamento assíncrono do status via polling.

## Tecnologias utilizadas
- Java 8
- Swing
- Maven
- Jackson

## Funcionalidades implementadas
- Interface gráfica com:
    - campo de produto
    - campo de quantidade
    - botão para envio
    - tabela de acompanhamento de pedidos
- Geração de `UUID` para cada pedido
- Envio de pedido para o backend via HTTP
- Exibição inicial do pedido enviado
- Polling assíncrono para atualização de status
- Atualização da interface sem bloqueio da EDT

## Estrutura principal
- `model` - modelos usados no cliente
- `http` - comunicação HTTP com o backend
- `ui` - interface gráfica Swing
- `SwingClientApplication` - classe principal

## Como executar
### 1. Clonar o projeto
```bash
git clone https://github.com/ProgRS/swing-pedidos-client.git

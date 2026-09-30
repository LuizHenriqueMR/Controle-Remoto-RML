# Aplicação 1 - Controle Remoto em RMI

## Descrição

Emula a analogia do controle remoto de TV vista em sala de aula: o cliente
("controle remoto") chama métodos remotos que executam de verdade em um
objeto "TV" no servidor, como se estivesse apertando botões reais.

## Funções implementadas (6, mínimo exigido era 5)

1. `ligar()`
2. `desligar()`
3. `aumentarVolume()`
4. `diminuirVolume()`
5. `mudarCanal(int canal)`
6. `obterStatus()` — devolve o estado atual (ligada/desligada, canal, volume)

## Arquivos

- `ControleRemotoTV.java` — Interface Remota (o contrato)
- `TelevisaoImpl.java` — Objeto Remoto (a "TV" de verdade, com o estado)
- `ServidorTV.java` — registra a TV no `rmiregistry`
- `ClienteControleRemoto.java` — menu interativo que chama os métodos remotos

## Como executar (mesma máquina)

```
javac *.java
java ServidorTV
```

Em outro terminal (com o servidor rodando):

```
java ClienteControleRemoto
```

Use o menu numérico para controlar a TV remotamente.

## Como executar entre duas máquinas (para a apresentação)

1. Na máquina do **servidor**: `javac *.java` e depois `java ServidorTV`.
   Descubra o IPv4 dessa máquina com `ipconfig`.
2. Na máquina do **cliente**: copie todos os `.java`, compile
   (`javac *.java`) e rode passando o IP do servidor:
   ```
   java ClienteControleRemoto 192.168.1.10
   ```
3. Libere a porta `1099` no firewall da máquina do servidor.

## Avaliação

- 50% código no GitHub
- 50% apresentação executando ao vivo para o professor (não precisa de
  vídeo para esta aplicação — apenas a Aplicação 2 exige vídeo)

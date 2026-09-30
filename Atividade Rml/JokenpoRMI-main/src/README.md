# Aplicação 2 - Pedra, Papel, Tesoura, Lagarto, Spock (multiplayer via RMI)

## Descrição

Versão distribuída, via RMI, do jogo criado por Sam Kass e Karen Bryla
(1998). Um servidor central coordena rodadas entre 2 a 5 clientes
conectados simultaneamente.

## Regras do jogo

- Tesoura corta Papel
- Papel cobre Pedra
- Pedra esmaga Lagarto
- Lagarto envenena Spock
- Spock quebra Tesoura
- Tesoura decapita Lagarto
- Lagarto come Papel
- Papel desmente Spock
- Spock vaporiza Pedra
- Pedra quebra Tesoura

## Como o sistema funciona

### Servidor (`ServidorPrincipal` + `ServidorJogo`)

- Aceita de 2 a 5 jogadores conectados.
- A cada rodada: avisa todos os clientes que a rodada começou e que eles
  têm **5 segundos** para enviar a jogada.
- Quem não enviar a jogada dentro do prazo é **desclassificado naquela
  rodada** (mas continua no jogo para as próximas rodadas).
- Calcula o vencedor: um jogador vence a rodada se a jogada dele não
  perde para nenhuma outra jogada presente. Se não houver um vencedor
  único (todos jogaram igual, ou há um empate entre duas jogadas que não
  se batem), a rodada é declarada **empate**, e todos os jogadores voltam
  a jogar na rodada seguinte.
- Após resolver a rodada (vitória ou empate), transmite o **placar
  atualizado** (pontuação de cada jogador + total de empates) para todos.
- Aguarda **10 segundos** e inicia a rodada seguinte, em loop contínuo.

### Cliente (`ClienteJogador`)

- Cada cliente **também é um objeto remoto** (implementa a interface
  `Jogador` e estende `UnicastRemoteObject`), o que permite que o
  servidor — e até outros clientes — chamem métodos nele.
- Ao ganhar uma rodada, o **próprio cliente vencedor** (não o servidor)
  chama diretamente cada cliente perdedor, enviando a mensagem
  `"Perdeu Loser... Tente na próxima."` — exatamente como pede o
  enunciado. Isso é feito passando os *stubs* (referências remotas) dos
  perdedores como parâmetro do método `notificarVitoria`, uma técnica
  avançada de RMI (objetos remotos como parâmetros de chamadas remotas).

## Arquivos

- `Jogo.java` — Interface Remota do **servidor** (o que o cliente pode
  chamar: `conectar` e `jogar`)
- `Jogador.java` — Interface Remota do **cliente/callback** (o que o
  servidor e outros clientes podem chamar: `iniciarRodada`,
  `resultadoRodada`, `atualizarPlacar`, `receberMensagem`,
  `notificarVitoria`)
- `ServidorJogo.java` — implementação do servidor: lógica das regras,
  laço de rodadas, cálculo de vencedor, placar
- `ServidorPrincipal.java` — `main` do servidor: registra no
  `rmiregistry` e inicia o jogo
- `ClienteJogador.java` — implementação do cliente: interface de
  console, leitura de jogada com prazo de 5 segundos, callback de
  vitória/derrota

## Como executar

### Compilar

```
javac *.java
```

### Rodar o servidor

```
java ServidorPrincipal
```

Deixe essa janela aberta — o `rmiregistry` roda dentro do próprio
processo do servidor.

### Rodar os clientes (mínimo 2, máximo 5), cada um em um terminal

```
java ClienteJogador Ana
java ClienteJogador Bruno
java ClienteJogador Carlos
```

(o nome do jogador é obrigatório; se quiser conectar a outra máquina,
passe o IP como segundo argumento — veja abaixo)

Assim que houver pelo menos 2 jogadores conectados, o servidor inicia a
primeira rodada automaticamente. Cada cliente mostra:

```
----- RODADA 1 -----
Voce tem 5 segundos para jogar!
Opcoes: pedra, papel, tesoura, lagarto, spock
Sua jogada: _
```

Digite uma das 5 opções e pressione Enter dentro do prazo.

### Executar entre máquinas diferentes (para a apresentação)

1. Na máquina do **servidor**: `java ServidorPrincipal`. Descubra o
   IPv4 com `ipconfig`.
2. Em cada máquina **cliente**: copie todos os `.java`, compile, e rode
   passando o IP do servidor como segundo argumento:
   ```
   java ClienteJogador Ana 192.168.1.10
   ```
3. Libere a porta `1099` no firewall da máquina do servidor.

### Modo automático (apenas para testes rápidos, sem digitar)

Cada cliente aceita um parâmetro opcional `--auto=` com uma lista de
jogadas pré-definidas, uma por rodada — útil para testar rapidamente sem
precisar digitar em tempo real:

```
java ClienteJogador Ana localhost --auto=pedra,papel,spock
java ClienteJogador Bruno localhost --auto=tesoura,papel,spock
```

Esse modo foi usado apenas para validar o funcionamento do sistema
durante o desenvolvimento; na apresentação e no vídeo, o uso normal é o
modo interativo (digitando a jogada no teclado).

## Testado e validado

O sistema foi testado com múltiplos cenários:
- 3 jogadores com um vencedor único (jogada que vence as outras duas)
- Empate por jogadas iguais entre todos
- Empate por "não vencedor único" (duas jogadas que não se batem entre si)
- Desclassificação por não jogar dentro do prazo de 5 segundos
- Mensagem de provocação enviada diretamente do vencedor para os
  perdedores (comunicação cliente-a-cliente via RMI)

## Entregáveis

- Código no GitHub
- Vídeo de 5 a 10 minutos explicando o código e mostrando um exemplo de
  execução (**apenas esta aplicação exige vídeo**)
- Apresentação ao vivo em sala de aula

Carrinho controlado por Bluetooth com Arduino Uno
Projeto de robótica educacional: um celular Android envia comandos por Bluetooth para movimentar um carrinho com dois motores. O Arduino Uno recebe esses comandos por meio do módulo HC-05 e aciona uma ponte H.

1. O que o projeto faz?
O objetivo é controlar os movimentos do carrinho diretamente pelo aplicativo ControlSCar instalado em um celular Android. São cinco ações: andar para frente, andar para trás, virar à esquerda, virar à direita e parar.

O caminho de um comando é:
[Celular Android / ControlSCar]
               |
      Bluetooth clássico (SPP)
               |
        [Módulo HC-05]
               |
      Comunicação serial (TX/RX)
               |
          [Arduino Uno]
               |
       Sinais digitais IN1–IN4
               |
            [Ponte H]
               |
      [Motor A]       [Motor B]
Exemplo: ao pressionar a seta para frente no celular, o aplicativo envia a letra F. O HC-05 recebe essa letra e a repassa ao Arduino. O programa reconhece F, configura os pinos da ponte H e os motores giram para movimentar o carrinho.

Este projeto tem duas possibilidades de controle:
- Versão principal: celular Android + Bluetooth HC-05.
- Versão opcional: celular Android + Bluetooth HC-05 ou joystick analógico conectado fisicamente ao Arduino.
2. Componentes utilizados
Componente	Quantidade	Para que serve
Arduino Uno	1	Executa o programa e controla os sinais enviados à ponte H.
Módulo Bluetooth HC-05, com placa adaptadora	1	Recebe os comandos enviados pelo celular.
Ponte H de duas vias, com entradas IN1, IN2, IN3 e IN4	1	Permite inverter o sentido dos dois motores.
Motores DC	2	Movimentam as rodas do carrinho.
Rodas e base/chassi	Conforme a montagem	Sustentam a estrutura.
Fonte ou bateria adequada aos motores e à ponte H	1	Alimenta os motores sem depender dos pinos do Arduino.
Resistores de 1 kΩ e 2 kΩ	1 de cada	Formam um divisor de tensão para proteger o RXD do HC-05.
Jumpers/fios de conexão	Conforme necessário	Fazem as ligações entre os componentes.
Protoboard (opcional)	1	Facilita a montagem do divisor de tensão.
Celular Android	1	Executa o aplicativo de controle.
Cabo USB	1	Permite gravar o código no Arduino.
Joystick analógico com VRx, VRy e SW (opcional)	1	Permite controlar o carrinho sem usar os botões do celular.


Atenção à ponte H: o projeto usa quatro entradas de direção. Se a sua placa também tiver ENA e ENB (como acontece em muitos módulos L298N), essas entradas precisam estar habilitadas, por exemplo com os jumpers próprios da placa. O programa básico não controla ENA/ENB nem regula a velocidade.

3. Montagem do circuito
3.1. Ponte H → Arduino Uno
Conecte os terminais de controle da ponte H aos seguintes pinos digitais:
Ponte H	Arduino	Função
IN1	D4	Sentido do motor A — entrada 1.
IN2	D3	Sentido do motor A — entrada 2.
IN3	D5	Sentido do motor B — entrada 1.
IN4	D6	Sentido do motor B — entrada 2.


1. Desligue a alimentação antes de encaixar ou alterar fios.
2. Ligue o motor A na saída correspondente ao primeiro canal da ponte H e o motor B na saída do segundo canal. Os nomes das saídas podem variar (OUT1/OUT2 e OUT3/OUT4, por exemplo).
3. Faça as quatro ligações IN1 a IN4 conforme a tabela.
4. Conecte a fonte/bateria própria dos motores nos terminais de alimentação da ponte H, observando a tensão e a polaridade exigidas pelo seu modelo.
5. Ligue o GND da ponte H ao GND do Arduino. Sem esse terra em comum, os sinais digitais podem não ser reconhecidos corretamente.
6. Se existirem ENA e ENB, confira se ambos estão habilitados.
Não ligue os motores diretamente aos pinos do Arduino, nem use o pino 5V do Uno como fonte dos motores.
3.2. HC-05 → Arduino Uno
HC-05	Arduino Uno	Observação
VCC	5V	Somente para a placa adaptadora da foto, identificada para alimentação de aproximadamente 3,6–6 V.
GND	GND	Terra comum.
TXD	D10	O Bluetooth transmite; o Arduino recebe.
RXD	D11	Usar divisor de tensão.
EN	Não conectado	Não é necessário no modo normal de uso.
STATE	Não conectado	Não é utilizado neste projeto.


O RXD do módulo trabalha com nível lógico próximo de 3,3 V. Para reduzir os 5 V do sinal enviado pelo Arduino, monte o divisor:
Arduino D11 ---- [1 kΩ] ----+---- HC-05 RXD
                            |
                          [2 kΩ]
                            |
Arduino GND ----------------+---- HC-05 GND
O TXD do HC-05 pode ir diretamente para D10 do Arduino Uno. Não confunda o par TX/RX: aqui o D10 é a entrada serial do Arduino e o D11 é a saída serial.
3.3. Conferência antes de ligar
- [ ] IN1 está no D4, IN2 no D3, IN3 no D5 e IN4 no D6.
- [ ] Os dois motores estão conectados às saídas da ponte H.
- [ ] A ponte H possui alimentação adequada para os motores.
- [ ] GND do Arduino, GND da ponte H e GND do HC-05 estão em comum.
- [ ] HC-05: TXD → D10; RXD ← D11 por divisor de tensão.
- [ ] ENA/ENB, caso existam, estão habilitados.
- [ ] Não existem fios desencapados se tocando nem inversões de polaridade.
# Carrinho Bluetooth com Arduino Uno

Projeto de robótica para controlar um carrinho de dois motores pelo celular, usando **Arduino Uno**, **Bluetooth HC-05** e **ponte H**. O aplicativo envia uma letra; o Arduino interpreta o comando e aciona os motores.

**Tutorial ilustrado e passo a passo:** [Universo Robótico](https://universorobotico.vercel.app/)

## Componentes

- 1 Arduino Uno e 1 cabo USB;
- 1 módulo Bluetooth HC-05 com placa adaptadora;
- 1 ponte H de dois canais (entradas IN1, IN2, IN3 e IN4);
- 2 motores DC, rodas e chassi;
- Bateria/fonte adequada aos motores e à ponte H;
- Jumpers e resistores de **1 kΩ** e **2 kΩ** (proteção do RXD do HC-05);
- 1 celular Android com o aplicativo **ControlSCar**.

**Opcional:** joystick analógico com VRx, VRy e SW.

## 1. Circuito e conexões

| Ponte H | Arduino Uno |
|---|---|
| IN1 | D4 |
| IN2 | D3 |
| IN3 | D5 |
| IN4 | D6 |

| HC-05 | Arduino Uno |
|---|---|
| VCC | 5V (placa adaptadora compatível) |
| GND | GND |
| TXD | D10 |
| RXD | D11 **por divisor de tensão** |

Conecte cada motor a um canal de saída da ponte H. A bateria dos motores deve ser ligada à alimentação própria da ponte H. **Una os GNDs** do Arduino, do HC-05 e da ponte H. Se a ponte H tiver ENA/ENB, habilite essas entradas.

Para proteger o RXD: **D11 → resistor de 1 kΩ → RXD**, com um **resistor de 2 kΩ entre RXD e GND**. Faça as ligações com tudo desligado.

> **[INSERIR IMAGEM 1 — DEMONSTRAÇÃO DO CIRCUITO]**  
> Adicione sua foto em `imagens/demonstracao-circuito.png` e substitua este bloco por:  
> `![Demonstração do circuito montado](imagens/demonstracao-circuito.png)`

**Tutorial da montagem:** [Ver circuito e ligações](https://universorobotico.vercel.app/circuito.html).

## 2. Código do Arduino

Arquivo: [`arduino/CarrinhoBluetoothHC05.ino`](arduino/CarrinhoBluetoothHC05.ino).

O programa usa `SoftwareSerial(10, 11)` para receber comandos do HC-05 a **9600 baud**. No `setup()`, configura os pinos da ponte H como saída e começa com os motores parados. No `loop()`, lê os caracteres recebidos e chama uma função de movimento.

```cpp
// Pinos da ponte H
const byte IN1 = 4;
const byte IN2 = 3;
const byte IN3 = 5;
const byte IN4 = 6;

// Exemplo: mover para frente
void frente() {
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}
```

`HIGH` e `LOW` definem o sentido de cada motor. O código também **para o carrinho após 600 ms sem receber novos comandos de movimento**, como proteção em caso de perda de comunicação.

Para gravar: abra o `.ino` na **Arduino IDE**, selecione a placa **Arduino Uno** e a porta correspondente e clique em **Carregar**.

> **[INSERIR IMAGEM 2 — CÓDIGO NA ARDUINO IDE]**  
> Adicione sua captura em `imagens/codigo-arduino-ide.png` e substitua este bloco por:  
> `![Código do carrinho aberto na Arduino IDE](imagens/codigo-arduino-ide.png)`

**Tutorial da programação:** [Entender o código e as funções](https://universorobotico.vercel.app/codigo.html).

## 3. Aplicativo Android e comandos

O **ControlSCar** se conecta ao HC-05 por Bluetooth clássico e envia caracteres para o Arduino. Primeiro, pareie o celular com o **HC-05** nas configurações do Android (PIN comum: `1234` ou `0000`). Depois, abra o aplicativo e selecione o módulo.

| Comando enviado | Ação no carrinho |
|---|---|
| `F` | Frente |
| `B` | Trás |
| `L` | Esquerda |
| `R` | Direita |
| `S` | Parar |

O aplicativo foi configurado para **repetir o comando enquanto a seta estiver pressionada** e enviar **`S` ao soltar**. O Arduino reconhece as letras e controla a ponte H.

> **[INSERIR IMAGEM 3 — APLICATIVO]**  
> Adicione sua captura em `imagens/aplicativo-controlscar.png` e substitua este bloco por:  
> `![Aplicativo ControlSCar conectado ao carrinho](imagens/aplicativo-controlscar.png)`

**Tutorial do aplicativo:** [Instalação, pareamento e teste](https://universorobotico.vercel.app/aplicativo.html).

## Controle alternativo: joystick

O projeto também inclui o arquivo [`arduino/CarrinhoBluetoothJoystick.ino`](arduino/CarrinhoBluetoothJoystick.ino), para acrescentar um joystick físico ligado a **A0 (VRx)**, **A1 (VRy)** e **D7 (SW)**. Veja o [tutorial do joystick](https://universorobotico.vercel.app/joystick.html).

> **Antes do primeiro teste:** deixe as rodas suspensas, confira a alimentação dos motores, o GND comum e o sentido de rotação. Não alimente os motores diretamente pelo pino 5V do Arduino.

#include <SoftwareSerial.h>

// HC-05: TXD -> pino 10 e RXD -> pino 11 (divisor de tensao).
SoftwareSerial bluetooth(10, 11);  // RX, TX

// Pinos da ponte H conforme a montagem.
const byte IN1 = 4;
const byte IN2 = 3;
const byte IN3 = 5;
const byte IN4 = 6;

// O aplicativo repete os comandos durante o movimento.
// Se a comunicacao parar, os motores sao desligados.
const unsigned long TEMPO_LIMITE_MS = 600;

unsigned long instanteUltimoComando = 0;
bool estaMovimentando = false;

void configurarMotores() {
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
}

void frente() {
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}

void tras() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH);
}

void esquerda() {
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH);
}

void direita() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}

void parar() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
  estaMovimentando = false;
}

void executarComando(char comando) {
  switch (comando) {
    case 'F': case 'f':
      frente();
      break;
    case 'B': case 'b':
      tras();
      break;
    case 'L': case 'l':
      esquerda();
      break;
    case 'R': case 'r':
      direita();
      break;
    case 'S': case 's':
      parar();
      return;
    default:
      return;
  }
  instanteUltimoComando = millis();
  estaMovimentando = true;
}

void setup() {
  Serial.begin(9600);
  bluetooth.begin(9600);
  configurarMotores();
  parar();
  Serial.println("Carrinho Bluetooth HC-05 pronto!");
}

void loop() {
  while (bluetooth.available()) {
    char comando = bluetooth.read();
    Serial.print("Comando recebido: ");
    Serial.println(comando);
    executarComando(comando);
  }

  if (estaMovimentando &&
      millis() - instanteUltimoComando > TEMPO_LIMITE_MS) {
    parar();
    Serial.println("Parada automatica por falta de comandos.");
  }
}

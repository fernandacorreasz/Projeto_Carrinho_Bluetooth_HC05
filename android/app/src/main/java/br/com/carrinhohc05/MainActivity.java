package br.com.carrinhohc05;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Aplicativo Android nativo para Arduino Uno + Bluetooth HC-05 (Bluetooth Classic SPP).
 * Protocolo: F (frente), B (ré), L (esquerda), R (direita), S (parar).
 * Pressione e mantenha pressionado para mover; solte para parar.
 */
public class MainActivity extends Activity {

    private static final int CODIGO_PERMISSAO_BLUETOOTH = 100;
    private static final int INTERVALO_REPETICAO_MS = 140;
    private static final UUID IDENTIFICADOR_SPP = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB");

    private static final int COR_FUNDO = Color.rgb(10, 17, 33);
    private static final int COR_CARTAO = Color.rgb(24, 35, 58);
    private static final int COR_AZUL = Color.rgb(60, 131, 246);
    private static final int COR_TEXTO = Color.WHITE;
    private static final int COR_SECUNDARIA = Color.rgb(170, 187, 210);

    private BluetoothAdapter adaptadorBluetooth;
    private volatile BluetoothSocket conexaoBluetooth;
    private volatile OutputStream saidaBluetooth;

    private final ExecutorService filaComunicacao = Executors.newSingleThreadExecutor();
    private final Handler manipulador = new Handler(Looper.getMainLooper());
    private volatile boolean conectado = false;
    private boolean conectando = false;
    private char comandoPressionado = 'S';

    private TextView textoStatus;
    private TextView textoMovimento;
    private Button botaoConectar;
    private Button botaoDesconectar;
    private final List<Button> botoesMovimento = new ArrayList<>();

    private final Runnable repetidorComando = new Runnable() {
        @Override
        public void run() {
            if (comandoPressionado == 'S' || !conectado) return;
            enviarComando(comandoPressionado);
            manipulador.postDelayed(this, INTERVALO_REPETICAO_MS);
        }
    };

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        BluetoothManager gerenciador = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adaptadorBluetooth = gerenciador == null ? null : gerenciador.getAdapter();
        construirTela();
        atualizarConexao(false, "Desconectado");
    }

    private int dp(int valor) {
        return (int) (getResources().getDisplayMetrics().density * valor + 0.5f);
    }

    private GradientDrawable fundoArredondado(int cor, int raio) {
        GradientDrawable formato = new GradientDrawable();
        formato.setColor(cor);
        formato.setCornerRadius(dp(raio));
        return formato;
    }

    private TextView criarTexto(String valor, int tamanho, boolean negrito, int cor) {
        TextView texto = new TextView(this);
        texto.setText(valor);
        texto.setTextColor(cor);
        texto.setTextSize(tamanho);
        if (negrito) texto.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return texto;
    }

    private void adicionarEspaco(LinearLayout container, int altura) {
        View espaco = new View(this);
        container.addView(espaco, new LinearLayout.LayoutParams(1, dp(altura)));
    }

    private Button criarBotao(String rotulo, int corFundo) {
        Button botao = new Button(this);
        botao.setText(rotulo);
        botao.setTextSize(16);
        botao.setTextColor(COR_TEXTO);
        botao.setAllCaps(false);
        botao.setBackground(fundoArredondado(corFundo, 16));
        botao.setPadding(dp(12), dp(12), dp(12), dp(12));
        return botao;
    }

    private void construirTela() {
        ScrollView rolagem = new ScrollView(this);
        rolagem.setBackgroundColor(COR_FUNDO);
        rolagem.setFillViewport(true);

        LinearLayout coluna = new LinearLayout(this);
        coluna.setOrientation(LinearLayout.VERTICAL);
        coluna.setGravity(Gravity.CENTER_HORIZONTAL);
        coluna.setPadding(dp(20), dp(30), dp(20), dp(30));
        rolagem.addView(coluna);
        setContentView(rolagem);

        TextView titulo = criarTexto("CARRINHO  /  HC-05", 25, true, COR_TEXTO);
        titulo.setGravity(Gravity.CENTER);
        coluna.addView(titulo);

        TextView subtitulo = criarTexto("Controle Bluetooth para Arduino Uno", 14, false, COR_SECUNDARIA);
        subtitulo.setGravity(Gravity.CENTER);
        coluna.addView(subtitulo);
        adicionarEspaco(coluna, 26);

        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(dp(18), dp(18), dp(18), dp(18));
        cartao.setBackground(fundoArredondado(COR_CARTAO, 20));
        coluna.addView(cartao, new LinearLayout.LayoutParams(-1, -2));

        cartao.addView(criarTexto("CONEXÃO BLUETOOTH", 12, true, COR_SECUNDARIA));
        adicionarEspaco(cartao, 8);
        textoStatus = criarTexto("Desconectado", 19, true, COR_TEXTO);
        cartao.addView(textoStatus);
        adicionarEspaco(cartao, 14);

        botaoConectar = criarBotao("Conectar ao HC-05", COR_AZUL);
        cartao.addView(botaoConectar, new LinearLayout.LayoutParams(-1, dp(54)));
        botaoConectar.setOnClickListener(v -> selecionarDispositivo());
        adicionarEspaco(cartao, 9);

        botaoDesconectar = criarBotao("Desconectar", Color.rgb(60, 72, 96));
        cartao.addView(botaoDesconectar, new LinearLayout.LayoutParams(-1, dp(48)));
        botaoDesconectar.setOnClickListener(v -> desconectar());

        adicionarEspaco(coluna, 27);
        TextView tituloControle = criarTexto("CONTROLE DE MOVIMENTO", 15, true, COR_SECUNDARIA);
        tituloControle.setGravity(Gravity.CENTER);
        coluna.addView(tituloControle);
        adicionarEspaco(coluna, 14);

        LinearLayout linhaSuperior = criarLinha();
        coluna.addView(linhaSuperior);
        adicionarEspacadorHorizontal(linhaSuperior);
        linhaSuperior.addView(criarBotaoMovimento("▲\nFRENTE", 'F', COR_AZUL));
        adicionarEspacadorHorizontal(linhaSuperior);
        adicionarEspaco(coluna, 10);

        LinearLayout linhaCentral = criarLinha();
        coluna.addView(linhaCentral);
        linhaCentral.addView(criarBotaoMovimento("◀\nESQUERDA", 'L', COR_AZUL));
        linhaCentral.addView(criarBotaoParar());
        linhaCentral.addView(criarBotaoMovimento("DIREITA\n▶", 'R', COR_AZUL));
        adicionarEspaco(coluna, 10);

        LinearLayout linhaInferior = criarLinha();
        coluna.addView(linhaInferior);
        adicionarEspacadorHorizontal(linhaInferior);
        linhaInferior.addView(criarBotaoMovimento("▼\nRÉ", 'B', COR_AZUL));
        adicionarEspacadorHorizontal(linhaInferior);
        adicionarEspaco(coluna, 24);

        textoMovimento = criarTexto("Pronto para conectar", 16, true, COR_TEXTO);
        textoMovimento.setGravity(Gravity.CENTER);
        coluna.addView(textoMovimento);
        adicionarEspaco(coluna, 10);

        TextView instrucoes = criarTexto(
                "Segure uma seta para movimentar. Solte para parar.\n" +
                "Primeiro pareie o HC-05 nas configurações do Android.\n" +
                "Teste com as rodas suspensas.", 13, false, COR_SECUNDARIA);
        instrucoes.setGravity(Gravity.CENTER);
        coluna.addView(instrucoes);
    }

    private LinearLayout criarLinha() {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER);
        return linha;
    }

    private void adicionarEspacadorHorizontal(LinearLayout linha) {
        View espaco = new View(this);
        linha.addView(espaco, new LinearLayout.LayoutParams(0, dp(1), 1f));
    }

    private Button criarBotaoMovimento(String titulo, char comando, int cor) {
        Button botao = criarBotao(titulo, cor);
        botao.setTextSize(12);
        botao.setLayoutParams(new LinearLayout.LayoutParams(0, dp(88), 1f));
        botao.setOnTouchListener((view, evento) -> {
            switch (evento.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    iniciarMovimento(comando);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    pararMovimento();
                    return true;
                default:
                    return true;
            }
        });
        botoesMovimento.add(botao);
        return botao;
    }

    private Button criarBotaoParar() {
        Button botao = criarBotao("■\nPARAR", Color.rgb(207, 64, 84));
        botao.setTextSize(13);
        botao.setLayoutParams(new LinearLayout.LayoutParams(0, dp(88), 1f));
        botao.setOnClickListener(v -> pararMovimento());
        botoesMovimento.add(botao);
        return botao;
    }

    private boolean possuiPermissao() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void selecionarDispositivo() {
        if (adaptadorBluetooth == null) {
            mostrarAviso("Este celular não suporta Bluetooth.");
            return;
        }
        if (!possuiPermissao()) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    CODIGO_PERMISSAO_BLUETOOTH);
            return;
        }
        if (!adaptadorBluetooth.isEnabled()) {
            mostrarAviso("Ative o Bluetooth e pareie o HC-05 antes de conectar.");
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }
        Set<BluetoothDevice> pareados = adaptadorBluetooth.getBondedDevices();
        if (pareados == null || pareados.isEmpty()) {
            mostrarAviso("Pareie primeiro o HC-05 nas configurações Bluetooth do Android.");
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }
        List<BluetoothDevice> dispositivos = new ArrayList<>(pareados);
        Collections.sort(dispositivos, (a, b) -> {
            String nomeA = a.getName() == null ? "" : a.getName();
            String nomeB = b.getName() == null ? "" : b.getName();
            return nomeA.compareToIgnoreCase(nomeB);
        });
        String[] opcoes = new String[dispositivos.size()];
        for (int i = 0; i < dispositivos.size(); i++) {
            BluetoothDevice dispositivo = dispositivos.get(i);
            String nome = dispositivo.getName();
            opcoes[i] = (nome == null ? "Dispositivo sem nome" : nome)
                    + "\n" + dispositivo.getAddress();
        }
        new AlertDialog.Builder(this)
                .setTitle("Selecione seu módulo Bluetooth")
                .setItems(opcoes, (dialogo, indice) -> conectar(dispositivos.get(indice)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void conectar(BluetoothDevice dispositivo) {
        if (conectando || conectado) return;
        conectando = true;
        atualizarConexao(false, "Conectando...");
        botaoConectar.setEnabled(false);

        filaComunicacao.execute(() -> {
            BluetoothSocket temporaria = null;
            try {
                if (!possuiPermissao()) throw new SecurityException("Permissão Bluetooth negada");
                temporaria = dispositivo.createRfcommSocketToServiceRecord(IDENTIFICADOR_SPP);
                conexaoBluetooth = temporaria;
                temporaria.connect();
                saidaBluetooth = temporaria.getOutputStream();
                conectado = true;
                runOnUiThread(() -> {
                    conectando = false;
                    atualizarConexao(true, "Conectado: " + dispositivo.getName());
                });
            } catch (IOException | SecurityException erro) {
                if (temporaria != null) {
                    try { temporaria.close(); } catch (IOException ignorado) { }
                }
                conexaoBluetooth = null;
                saidaBluetooth = null;
                conectado = false;
                runOnUiThread(() -> {
                    conectando = false;
                    atualizarConexao(false, "Falha na conexão");
                    mostrarAviso("Não foi possível conectar: " + erro.getMessage());
                });
            }
        });
    }

    private void atualizarConexao(boolean estaConectado, String status) {
        textoStatus.setText(status);
        textoStatus.setTextColor(estaConectado ? Color.rgb(80, 221, 161) : COR_TEXTO);
        botaoConectar.setEnabled(!estaConectado && !conectando);
        botaoDesconectar.setEnabled(estaConectado);
        for (Button botao : botoesMovimento) botao.setEnabled(estaConectado);
        if (!estaConectado) textoMovimento.setText("Carrinho parado");
    }

    private void enviarComando(char comando) {
        if (!conectado) return;
        filaComunicacao.execute(() -> {
            OutputStream saida = saidaBluetooth;
            if (saida == null) return;
            try {
                saida.write((byte) comando);
                saida.flush();
            } catch (IOException erro) {
                runOnUiThread(this::desconectar);
            }
        });
    }

    private void iniciarMovimento(char comando) {
        if (!conectado) return;
        manipulador.removeCallbacks(repetidorComando);
        comandoPressionado = comando;
        switch (comando) {
            case 'F': textoMovimento.setText("Indo para frente"); break;
            case 'B': textoMovimento.setText("Indo para trás"); break;
            case 'L': textoMovimento.setText("Virando à esquerda"); break;
            case 'R': textoMovimento.setText("Virando à direita"); break;
            default: textoMovimento.setText("Em movimento");
        }
        enviarComando(comando);
        manipulador.postDelayed(repetidorComando, INTERVALO_REPETICAO_MS);
    }

    private void pararMovimento() {
        manipulador.removeCallbacks(repetidorComando);
        comandoPressionado = 'S';
        textoMovimento.setText("Parado");
        enviarComando('S');
    }

    private void desconectar() {
        pararMovimento();
        conectado = false;
        conectando = false;
        BluetoothSocket anterior = conexaoBluetooth;
        OutputStream saidaAnterior = saidaBluetooth;
        conexaoBluetooth = null;
        saidaBluetooth = null;
        if (anterior != null) {
            // Mantém a parada na fila de comunicação antes de fechar o socket.
            filaComunicacao.execute(() -> {
                try {
                    if (saidaAnterior != null) {
                        saidaAnterior.write((byte) 'S');
                        saidaAnterior.flush();
                    }
                } catch (IOException ignorado) { }
                try { anterior.close(); } catch (IOException ignorado) { }
            });
        }
        atualizarConexao(false, "Desconectado");
    }

    private void mostrarAviso(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int codigo, String[] permissoes, int[] resultados) {
        super.onRequestPermissionsResult(codigo, permissoes, resultados);
        if (codigo == CODIGO_PERMISSAO_BLUETOOTH && resultados.length > 0 &&
                resultados[0] == PackageManager.PERMISSION_GRANTED) {
            selecionarDispositivo();
        } else if (codigo == CODIGO_PERMISSAO_BLUETOOTH) {
            mostrarAviso("Autorize a permissão 'Dispositivos próximos' para conectar.");
        }
    }

    @Override
    protected void onPause() {
        pararMovimento();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        desconectar();
        filaComunicacao.shutdown();
        super.onDestroy();
    }
}

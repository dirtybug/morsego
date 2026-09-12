# MorseGO 📻

**MorseGO** é uma aplicação Android pura em **JAVA** desenhada para aprender, praticar e dominar a telegrafia (Código Morse / CW) através da **Árvore Binária de Morse** (*Morse Binary Tree*), com suporte e compatibilidade nativa para o manipulador físico de pás duplas USB Type-C / 3.5mm:
> **CW Keyer Automatic Trainer PCB 4 ND Magnet Bases Doble Paddle** ([Ver na Amazon](https://www.amazon.es/-/pt/gp/product/B0F666MVG6/ref=ox_sc_act_title_1?smid=A2N32YIZWO8AUG&psc=1))

---

## 🌳 Progressão pela Árvore Binária de Morse (Binary Tree)

A aprendizagem segue rigorosamente a estrutura hierárquica da **Árvore Binária de Morse**:
* Na árvore, mover para a **Esquerda** corresponde a um **Ponto / DIT (`.`)**.
* Mover para a **Direita** corresponde a um **Traço / DAH (`-`)**.

### Como funcionam os Níveis:
Cada nível **adiciona exatamente 2 letras novas** descendo pelos ramos da árvore e **inclui todas as letras anteriores** no conjunto de treino:

* **Nível 1 (A Raiz da Árvore):**
  * Novas: **E** (`.`) e **T** (`-`)
  * Em jogo: `[E, T]`
* **Nível 2 (Ramo do E):**
  * Novas: **I** (`..`) e **A** (`.-`)
  * Em jogo: `[E, T, I, A]`
* **Nível 3 (Ramo do T):**
  * Novas: **N** (`-.`) e **M** (`--`)
  * Em jogo: `[E, T, I, A, N, M]` *(Todas as letras de profundidade 1 e 2 completas!)*
* **Nível 4 (Sub-ramo do I):**
  * Novas: **S** (`...`) e **U** (`..-`)
  * Em jogo: `[E, T, I, A, N, M, S, U]`
* **Nível 5 (Sub-ramo do A):**
  * Novas: **R** (`.-.`) e **W** (`.--`)
  * Em jogo: `[E, T, I, A, N, M, S, U, R, W]`
* **Nível 6 (Sub-ramo do N):**
  * Novas: **D** (`-..`) e **K** (`-.-`)
  * Em jogo: `[E, T, I, A, N, M, S, U, R, W, D, K]`
* **Nível 7 (Sub-ramo do M):**
  * Novas: **G** (`--.`) e **O** (`---`)
  * Em jogo: `[E, T, I, A, N, M, S, U, R, W, D, K, G, O]` *(Todas as letras de profundidade 3 completas!)*
* **Níveis 8 a 13:** Adicionam em pares as letras de profundidade 4: **H** & **V**, **F** & **L**, **P** & **J**, **B** & **X**, **C** & **Y**, **Z** & **Q** (completando as 26 letras do alfabeto).
* **Níveis 14 a 18:** Números de 5 elementos em pares: **5** & **4**, **3** & **2**, **1** & **6**, **7** & **8**, **9** & **0**.
* **Níveis 19 a 21:** Pontuação e sinais de serviço (*prosigns*): **.** & **,**, **?** & **/**, **SOS** & **AR**.

---

## 🔌 Compatibilidade com o Manipulador USB da Amazon

O hardware da Amazon é um circuito PCB com base de 4 ímanes Nd, entrada USB Type-C e tomada Jack 3.5mm:
1. **Modo 2 (Recomendado - LED a piscar / VBand):**
   * Emula teclado USB HID enviando `Ctrl Esquerdo` (Pá DIT) e `Ctrl Direito` (Pá DAH).
   * O MorseGO captura estes eventos diretamente no `MainActivity.dispatchKeyEvent()`.
2. **Modo 1 (LED contínuo / Emulação de Rato):**
   * Emula cliques de rato (Primary Click e Secondary Click), capturados em `dispatchGenericMotionEvent()`.
3. **Calibração em 1 Toque:**
   * No separador **Hardware CW**, toque em *"Calibrar Dit"* ou *"Calibrar Dah"* e prima a respetiva pá física para associar imediatamente qualquer comando.
4. **Inversão de Pás:**
   * Alterne livremente entre destro e canhoto com o botão de inversão.

---

## 💻 Arquitetura 100% Java

O projeto foi construído inteiramente em **Java** com o framework oficial Android e Material Components:
* **UI Custom View:** [`MorseTreeView.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/main/java/com/morsego/app/ui/view/MorseTreeView.java) desenha a árvore binária em 2D de forma interativa.
* **Motor de Áudio:** [`MorseAudioSynthesizer.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/main/java/com/morsego/app/audio/MorseAudioSynthesizer.java) gera tom PCM puro sem ruídos nem estalidos através de `AudioTrack`.
* **Motor de Pá Dupla:** [`IambicKeyerEngine.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/main/java/com/morsego/app/keyer/IambicKeyerEngine.java) suporta Iambic B (Curtis), Iambic A e Straight Key manual.
* **Descodificador:** [`MorseDecoder.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/main/java/com/morsego/app/keyer/MorseDecoder.java) traduz a cadência das pás para texto em tempo real.
* **Temporização PARIS:** [`MorseTiming.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/main/java/com/morsego/app/keyer/MorseTiming.java) calcula durações de ponto, traço e pausas segundo a velocidade WPM.

---

## 📱 Como Executar no Android Studio

1. Abra a pasta `C:\Users\JúlioAndrade\morseGo` no **Android Studio**.
2. Deixe o Gradle sincronizar os ficheiros `build.gradle` e `settings.gradle`.
3. Conecte o dispositivo Android ou inicie um emulador.
4. Pressione **Run ▶**.
5. Ligue o manipulador USB Type-C ao telemóvel e explore os ramos da Árvore de Morse!

---

## 🐳 Executar Testes em Container Docker

> 📖 **Guia Completo de Build e Testes (Inglês):** Para detalhes completos sobre todos os pré-requisitos de software (Docker vs. Nativo com OpenJDK 17 e Android SDK), comandos e resolução de problemas, consulte o documento [`BUILD_AND_TEST_GUIDE.md`](file:///C:/Users/JúlioAndrade/morseGo/BUILD_AND_TEST_GUIDE.md).

Para garantir que o código e todos os testes correm num ambiente idêntico, isolado e reprodutível (com OpenJDK 17 e Android SDK 34), o projeto inclui suporte Docker completo:

### 0. Ambiente Interativo de Desenvolvimento (Dev Container):
Para programar e compilar interativamente dentro do Docker sem precisar de Java ou Android SDK instalados na máquina:
```cmd
# Windows CMD
run-docker-dev.bat

# Windows PowerShell
.\run-docker-dev.ps1

# Linux / macOS
./run-docker-dev.sh
```
Abre uma consola interativa (`bash`) com JDK 17, Android SDK 34 e Gradle Daemon ativado. Atalhos úteis dentro do container:
* `test` — Executa todos os testes unitários.
* `./gradlew test --continuous` — Retesta automaticamente ao editar ficheiros no seu editor no Windows.
* `build` — Compila o APK de Debug.
* `release` — Compila o APK de Release assinado.

### 🖥️ Executar o Android Studio com Interface Gráfica (GUI) no Docker:
Pode correr o **Android Studio completo com interface gráfica** diretamente dentro de um container Docker, com carregamento automático do projeto `morseGo` e acesso imediato pelo seu navegador web (via noVNC) ou cliente VNC:

```cmd
# Windows CMD (inicia e abre automaticamente o navegador)
run-docker-android-studio.bat

# Windows PowerShell
.\run-docker-android-studio.ps1

# Linux / macOS
./run-docker-android-studio.sh
```

* **Acesso pelo Navegador (Web noVNC):** `http://localhost:6080/vnc.html?autoconnect=true&resize=remote`
* **Cliente VNC Nativo:** `localhost:5900` (sem senha)
* **Projeto Carregado Automaticamente:** O projeto `morseGo` é montado em `/workspace` e aberto diretamente pelo Android Studio com o SDK Android 34 pré-configurado.
* **Comandos Úteis:**
  * `run-docker-android-studio.bat stop` — Para o container.
  * `run-docker-android-studio.bat logs` — Exibe os logs em tempo real.
  * `run-docker-android-studio.bat restart` — Reinicia o container e reabre o browser.

* `lint` — Analisa o código com o Android Lint.
* **VS Code Dev Containers (Android Studio GUI automático)**: Abra o projeto no VS Code e prima `Ctrl+Shift+P` -> *"Dev Containers: Reopen in Container"*. O container do Android Studio é iniciado automaticamente, abrindo o browser em `http://localhost:6080` com a IDE gráfica carregada e o projeto pronto! Também pode premir `Ctrl+Shift+B` no VS Code para executar a tarefa *"Launch Android Studio in Docker"*.

### 1. Testes Unitários (Rápido, ~3 segundos):
No Windows (Prompt de Comando):
```cmd
run-docker-tests.bat unit
```
No Windows (PowerShell):
```powershell
.\run-docker-tests.ps1 unit
```
Ou diretamente com Docker Compose:
```bash
docker compose run --rm test-unit
```
Executa todos os testes de lógica de negócio:
* [`MorseTimingTest.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/test/java/com/morsego/app/keyer/MorseTimingTest.java): Validação da temporização PARIS, limites mín/máx de pausas entre elementos ($0.45\times - 2.0\times$) e entre letras ($0.60\times - 2.2\times$).
* [`MorseBinaryTreeTest.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/test/java/com/morsego/app/tree/MorseBinaryTreeTest.java): Validação dos 21 níveis, nós e travessia da árvore.
* [`MorseWordGeneratorTest.java`](file:///C:/Users/JúlioAndrade/morseGo/app/src/test/java/com/morsego/app/tree/MorseWordGeneratorTest.java): Validação da geração dinâmica de palavras, cobertura de letras desbloqueadas e penalizações.

Os relatórios HTML ficam acessíveis em `./reports/unit-tests/index.html`.

### 2. Compilar APKs de Debug e Teste:
```cmd
run-docker-tests.bat build
```
Os binários compilados são guardados em `./build-apks/`.

### 3. Testes Instrumentados de Comportamento com Screenshots:
Com o emulador ou telemóvel conectado no host (`adb tcpip 5555`):
```cmd
run-docker-tests.bat instrumented
```
Executa os testes de interface, envio de palavras de rádio (CQ, 73, SOS, QSO) e extrai automaticamente os screenshots para `./screenshots/`.

### 4. Compilar Release APK (Google Play Store):
```cmd
run-docker-tests.bat release
```
Compila o APK de produção (`morseGO-release.apk`) em `./build-apks/`.

### 5. Executar Suite Completa (Testes + Compilação):
```cmd
run-docker-tests.bat all
```

---

## 🚀 Pipeline de CI/CD Automatizado (GitHub Actions)

O processo de **Release Oficial** é totalmente automatizado via [`.github/workflows/ci.yml`](file:///C:/Users/JúlioAndrade/morseGo/.github/workflows/ci.yml):

1. **Gatilho por Tag:** Basta criar e enviar uma tag de versão:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **Execução Obrigatória de Testes:**
   - Executa os 79 testes unitários no OpenJDK 17.
   - Dispara um emulador Android (Pixel 6 / API 30) e corre os 49 testes de comportamento instrumentados, capturando screenshots do ecrã real.
3. **Build em Docker:**
   - Constrói o container Docker oficial e gera os APKs (`Release` e `Debug`).
4. **Publicação do Release:**
   - Se e apenas se **todos os testes passarem a 100%**, o GitHub Actions:
     - Organiza os artefatos nas pastas versionadas `releases/v1.0.0/` e `tests/v1.0.0/`.
     - Gera os checksums `SHA256SUMS.txt` e o `release-manifest.json`.
     - Publica o GitHub Release oficial com download direto do APK e do arquivo zip dos relatórios.
   - Se algum teste falhar, o release é automaticamente abortado e nada é publicado.


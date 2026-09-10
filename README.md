# MorseGO 📻

**MorseGO** é uma aplicação Android moderna e completa, desenvolvida em **Kotlin** e **Jetpack Compose**, desenhada para aprender, praticar e aperfeiçoar a telegrafia (Código Morse / CW) — com suporte nativo e compatibilidade total para o manipulador físico de pás duplas USB Type-C / 3.5mm:
> **CW Keyer Automatic Trainer PCB 4 ND Magnet Bases Doble Paddle** ([Ver na Amazon](https://www.amazon.es/-/pt/gp/product/B0F666MVG6/ref=ox_sc_act_title_1?smid=A2N32YIZWO8AUG&psc=1))

---

## ⚡ Compatibilidade com o Hardware da Amazon (CW Keyer Trainer)

O dispositivo em questão é um manipulador de pás duplas (*Iambic Double Paddle Keyer*) montado em PCB com base magnética de 4 ímanes de neodímio, porta **USB Type-C** e ficha **Jack 3.5mm**.

### Como funciona no MorseGO:
1. **Ligação Plug-and-Play USB-C (OTG):**
   * Basta ligar o cabo USB Type-C incluído diretamente à porta do telemóvel ou tablet Android.
2. **Modo 2 (Recomendado - LED a piscar / VBand):**
   * Neste modo, o hardware emula um teclado USB HID padrão, enviando:
     * **Pá Esquerda (DIT •):** `Ctrl Esquerdo` (`KEYCODE_CTRL_LEFT`)
     * **Pá Direita (DAH —):** `Ctrl Direito` (`KEYCODE_CTRL_RIGHT`)
   * O `MorseGO` interceta estes eventos diretamente no `MainActivity.dispatchKeyEvent()`, sem interferir com os atalhos do sistema operativo.
3. **Modo 1 (LED fixo / Emulação de Rato):**
   * O dispositivo emula cliques de rato (Botão Primário e Secundário). O `MorseGO` inclui interceção via `dispatchGenericMotionEvent()` para suportar também este modo.
4. **Calibração Automática em 1 Toque:**
   * No separador **Hardware CW**, existe o botão de calibração dinâmica: clique em "Calibrar Dit", toque na sua pá física, e o código é automaticamente gravado!
5. **Inversão de Pás:**
   * Alterne instantaneamente se prefere Dit na esquerda ou na direita (ótimo para canhotos ou destros).

---

## 🚀 Funcionalidades Principais

### 1. 🎓 Método Koch (40 Lições Progressivas)
* O método com maior taxa de sucesso no mundo dos radioamadores.
* Começa com as letras **K** e **M** a velocidade real de CW (15 a 20 WPM), introduzindo novos caracteres gradualmente.
* Exercícios de **Escuta** (ritmo sonoro) e **Transmissão** com o seu manipulador físico ou pás virtuais no ecrã.

### 2. 🎯 Prática & Testes Interativos
* **Treino de Escuta:** A aplicação reproduz áudio de CW e apresenta opções de escolha múltipla para identificação imediata.
* **Treino de Transmissão:** A aplicação pede uma sequência de caracteres e avalia a precisão do seu envio em tempo real.

### 3. 📻 Manipulador Livre & Descodificador em Tempo Real (*Free Keyer*)
* Utilize o seu manipulador como numa estação de rádio real.
* Tradução instantânea de Dits e Dahs para texto em português/alfabeto internacional.
* Botões para copiar o texto descodificado, apagar ou limpar.
* Controlo de velocidade de 5 a 40 WPM.

### 4. 📖 Dicionário Morse & Soundboard
* Tabela completa com:
  * Todas as letras (**A-Z**) e números (**0-9**).
  * Pontuação oficial (`.`, `,`, `?`, `/`, `=`, `-`, `@`, `!`).
  * Sinais de serviço / Prosigns de rádio amador (**SOS**, **AR**, **SK**, **BT**, **AS**, **KN**).
* Clique em qualquer cartão para ouvir o som reproduzido fielmente.

### 5. 🎛️ Motor de Áudio de Baixa Latência & Síntese
* Síntese de onda sinusoidal pura com envelope suave anti-ruído (elimina cliques e estalidos ao manipular rapidamente).
* Ajuste de tom (*pitch*) de **400 Hz** a **1000 Hz** (padrão 700 Hz).
* Modos do Manipulador: **Iambic B** (Curtis com memória), **Iambic A** e **Straight Key** (manual).
* Feedback háptico/vibração opcional a cada toque.

---

## 🛠️ Estrutura do Projeto

```
morseGo/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/morsego/app/
│   │   │   ├── MainActivity.kt               # Ponto de entrada e interceção de hardware USB/OTG
│   │   │   ├── MorseGoApp.kt                 # Classe Application
│   │   │   ├── audio/
│   │   │   │   └── MorseAudioSynthesizer.kt  # Síntese de áudio PCM de baixa latência
│   │   │   ├── keyer/
│   │   │   │   ├── KeyerInputManager.kt      # Gestão de eventos USB, rato e calibração
│   │   │   │   ├── IambicKeyerEngine.kt      # Motor Iambic A/B e Straight Key
│   │   │   │   ├── MorseDecoder.kt           # Descodificador Morse em tempo real
│   │   │   │   └── MorseTiming.kt            # Padrão PARIS e temporizações WPM
│   │   │   ├── model/
│   │   │   │   ├── KeyerSettings.kt          # Configurações do manipulador
│   │   │   │   ├── KochLesson.kt             # Lições do Método Koch
│   │   │   │   ├── MorseDictionary.kt        # Dicionário e fonética internacional
│   │   │   │   └── UserProgress.kt           # Progresso do utilizador
│   │   │   ├── ui/
│   │   │   │   ├── components/               # Pás virtuais, barra de sinal RF, controlo WPM
│   │   │   │   ├── navigation/               # Rotas e destinos
│   │   │   │   ├── screens/                  # 6 Ecrãs da app
│   │   │   │   └── theme/                    # Tema Cyber Dark Ham Radio
│   │   │   └── viewmodel/
│   │   │       └── MorseViewModel.kt         # Gestão de estado reativo
│   │   └── res/                              # Recursos, ícones adaptativos, strings e estilos
├── gradle/
│   ├── libs.versions.toml                    # Version Catalog
│   └── wrapper/
│       └── gradle-wrapper.properties         # Gradle 8.6
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

---

## 📱 Como Executar

1. Abra o projeto no **Android Studio** (versão Iguana, Jellyfish, Koala ou superior).
2. O Gradle irá sincronizar automaticamente as dependências (`libs.versions.toml`).
3. Conecte o seu dispositivo Android (com depuração USB ativada) ou inicie um emulador.
4. Pressione **Run ▶** no Android Studio.
5. Conecte o seu manipulador USB Type-C ao telemóvel e comece a transmitir!

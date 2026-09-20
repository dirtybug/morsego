# 📦 Google Play Store Complete Listing & Submission Guide - MorseGO

This document is the complete, all-in-one reference for publishing and maintaining **MorseGO** on the **Google Play Console**.

---

## 1. Main Store Listing (Ficha Principal da Loja)

### 🏷️ 1.1 App Title (Nome da Aplicação)
*(Maximum 30 characters)*

```text
MorseGO: CW Keyer & Trainer
```
*(Character count: 27 / 30)*

---

### 📝 1.2 Short Description (Breve Descrição)
*(Maximum 80 characters)*

```text
Learn Morse code with the interactive binary tree & physical USB paddle keyers!
```
*(Character count: 79 / 80)*

---

### 📄 1.3 Full Description (Descrição Completa)
*(Maximum 4,000 characters)*

```text
Master Morse Code (CW) telegraphy with MorseGO — the comprehensive trainer combining visual tree pedagogy, an advanced iambic keyer engine, and plug-and-play support for physical dual-paddle USB keyers!

Whether you are an aspiring amateur radio operator (Ham Radio), preparing for your CW license, or a seasoned telegrapher looking to hone your sending speed and rhythm, MorseGO offers an engaging, intuitive, and scientifically proven way to practice.

---

🌳 MASTER MORSE WITH THE BINARY TREE
Forget boring alphabetical memorization! MorseGO guides you through the Morse Binary Tree:
• Moving Left corresponds to a DIT (dot: •)
• Moving Right corresponds to a DAH (dash: —)

Progress through 21 structured training levels:
• Levels 1 to 7: Build fundamental letters depth-by-depth, starting with E and T, then I, A, N, M, progressing through the entire core tree.
• Levels 8 to 13: Complete all 26 alphabet characters with precision.
• Levels 14 to 18: Master numbers (0 to 9) in structured pairs.
• Levels 19 to 21: Learn punctuation and international prosigns (., ?, /, SOS, AR).

---

🔌 NATIVE PHYSICAL USB PADDLE SUPPORT
Experience real CW telegraphy directly on your Android phone or tablet:
• Plug & Play: Connect physical USB Type-C and 3.5mm dual-paddle keyers (compatible with "CW Keyer Automatic Trainer PCB 4 ND Magnet Bases" and standard VBand keyers).
• Hardware Modes: Native support for USB HID keyboard mode (Left/Right Ctrl) and mouse emulation mode.
• Custom Calibration: One-touch calibration lets you map any external paddle, keyer, or foot switch.
• Ergonomic Paddle Reversal: Instantly swap DIT and DAH paddle positions for left-handed or right-handed operation.

---

⚡ ADVANCED IAMBIC KEYER ENGINE
• True Iambic Mode B (Curtis with squeeze memory).
• Classic Iambic Mode A.
• Straight Key / Manual mode for classic single-lever or hand-key operation.
• Visual touch paddles with instant visual feedback and tactile haptics.

---

🎛️ PRECISION TIMING & CUSTOMIZATION
• Adjustable Speed: 5 WPM to 45 WPM (Default: 10 WPM).
• Flexible Spacing: Configurable letter spacing (2 to 12 dits, default 720ms) and word spacing (5 to 20 dits, default 1560ms).
• Pure Audio Synthesis: Clean, anti-click PCM sine-wave generator with customizable tone pitch (400 Hz to 1000 Hz, default 700 Hz).
• Silent Mode with Haptics: Practice anywhere discreetly with optimized vibration feedback adhering to PARIS timing standards.
• Real-time Decoder: Translates your paddle cadence into clean text with instant timing diagnostics.

---

🔒 100% FREE, OPEN-SOURCE & PRIVACY-FOCUSED
• No ads.
• No trackers or analytics.
• No internet permission required.
• Complete source code available on GitHub: https://github.com/dirtybug/morsego

Start your journey into the world of CW telegraphy today with MorseGO!
```

---

## 2. Graphic Assets (Recursos Gráficos)

All graphic assets are pre-rendered at the exact specifications required by Google Play and are ready in your project directory:

| Asset Type | Dimension / Format | Local File Path |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px (PNG) | [`docs/playstore/playstore_app_icon_512x512.png`](docs/playstore/playstore_app_icon_512x512.png) |
| **Feature Graphic** | 1024 x 500 px (PNG) | [`docs/playstore/playstore_feature_graphic_1024x500.png`](docs/playstore/playstore_feature_graphic_1024x500.png) |
| **Screenshot 1** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_1_binary_tree.png`](docs/playstore/screenshots/playstore_screen_1_binary_tree.png) |
| **Screenshot 2** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_2_level_study.png`](docs/playstore/screenshots/playstore_screen_2_level_study.png) |
| **Screenshot 3** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_3_listening_exam.png`](docs/playstore/screenshots/playstore_screen_3_listening_exam.png) |
| **Screenshot 4** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_4_transmission_exam.png`](docs/playstore/screenshots/playstore_screen_4_transmission_exam.png) |
| **Screenshot 5** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_5_word_transmission.png`](docs/playstore/screenshots/playstore_screen_5_word_transmission.png) |
| **Screenshot 6** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_6_ham_radio_cw.png`](docs/playstore/screenshots/playstore_screen_6_ham_radio_cw.png) |
| **Screenshot 7** | Phone / Tablet (PNG) | [`docs/playstore/screenshots/playstore_screen_7_hardware_calibration.png`](docs/playstore/screenshots/playstore_screen_7_hardware_calibration.png) |

> 💡 **Tip:** You can upload all 7 screenshots to both the **Phone** and **Tablet (7-inch and 10-inch)** tabs.

---

## 3. Store Settings, Categorization & Tags (Definições, Categorias e Etiquetas)

Vá a **Crescimento > Presença na loja > Definições da loja** (*Growth > Store presence > Store settings*):

### 🏷️ 3.1 Application Type (Tipo de Aplicação)
- **Tipo:** **App** (*Aplicação*)

---

### 📂 3.2 Category (Categoria da Aplicação)
- **Categoria Principal (Recomendada):** **Education** (*Educação*)
  > *Motivo:* O MorseGO é estruturado pedagogicamente como um curso progressivo de 21 níveis baseado na Árvore Binária para aprender e memorizar código Morse.
- **Categoria Alternativa:** **Communication** (*Comunicação*) ou **Tools** (*Ferramentas*).

---

### 🔖 3.3 Official Google Play Console Tags (Etiquetas Oficiais da Play Store)
Na Google Play Console, clique em **Gerir etiquetas** (*Manage tags*) e selecione **até 5 etiquetas** da lista oficial:

| # | Etiqueta na Consola (PT) | Tag in Console (EN) | Relevância para o MorseGO |
| :-: | :--- | :--- | :--- |
| **1** | **Educação** | **Education** | Aprendizagem guiada da Árvore Binária e descodificação |
| **2** | **Comunicação** | **Communication** | Telegrafia CW e telecomunicações de rádio amador |
| **3** | **Ferramentas** | **Tools** | Manipulador eletrónico iâmbico e sintetizador de sinais |
| **4** | **Treino cerebral** | **Brain training** | Treino de agilidade auditiva e reflexos de transmissão |
| **5** | **Música e áudio** | **Music & audio** | Treino de ouvido acústico em tons sinusoidais de 400–1000 Hz |

---

### 🔍 3.4 Search Engine Optimization & Keywords (Palavras-Chave ASO para Pesquisa)
Termos estratégicos para garantir que os utilizadores encontram a aplicação ao pesquisar na Play Store:

#### 🇬🇧 English Keywords:
```text
Morse Code, CW Keyer, CW Trainer, Amateur Radio, Ham Radio, Telegraph, Iambic Keyer, VBand, Morse Binary Tree, Morse Code Learner, CW Telegraphy, Paddle Keyer, Straight Key, PARIS Timing, Morse Audio Tutor
```

#### 🇵🇹 Palavras-Chave em Português:
```text
Código Morse, Radioamadorismo, Manipulador Morse, Telegrafia CW, Treino Morse, Radioamador, Árvore Binária Morse, Aprender Código Morse, Pás Morse, Transmissor CW, Exame de Radioamador
```

---

### 📬 3.5 Contact Details (Detalhes de Contacto do Programador)
- **Email:** *(O seu email de contacto para suporte na Play Store)*
- **Website:** `https://github.com/dirtybug/morsego`
- **Telefone:** *(Opcional)*

---


## 4. App Content & Declarations Checklist (Conteúdo da Aplicação)

| Section | Required Value / Answer | Notes |
| :--- | :--- | :--- |
| **Privacy Policy** | `https://github.com/dirtybug/morsego/blob/main/PRIVACY_POLICY.md` | Live on GitHub |
| **App Access** | *All functionality is available without special access* | No logins or paywalls |
| **Ads** | *No, my app does not contain ads* | 100% ad-free |
| **Content Ratings** | Answer **No** to all questions (violence, drugs, location, etc.) | Rating: **Everyone / PEGI 3** |
| **Target Audience** | Select **13-15**, **16-17**, and **18 and over** | Appeal to children: **No** |
| **News Apps** | *No* | Not a news aggregator |
| **COVID-19 Tracing** | *No* | Not a health tracing app |
| **Data Safety** | *Does your app collect or share user data?* ➔ **No** | Offline only, zero tracking |
| **Government Apps** | *No* | Not a government agency |
| **Financial Features** | *My app doesn't provide financial features* | No payments or crypto |

---

## 5. Production Release (Produção)

### 📦 Deliverable AAB
- **File:** `morseGO-v1.0.1.aab` (Signed production Android App Bundle, 2.47 MB)
- **Download Link:** [GitHub Releases v1.0.1](https://github.com/dirtybug/morsego/releases/tag/v1.0.1)
- **Version Name:** `1.0.1`
- **Version Code:** `10001`
- **Target API:** Android 16 (API 36) with R8 optimization

### 📢 Release Notes (O que há de novo nesta versão)
*(Copy and paste into the release notes box)*

```text
Initial release of MorseGO v1.0.1:
• Interactive Morse Binary Tree training with 21 progressive levels.
• Native USB Type-C & 3.5mm dual-paddle physical keyer support.
• Iambic Keyer Engine (Mode B, Mode A, Straight Key).
• Built for Android 16 (API 36) with R8 performance optimizations.
• Pure PCM sine-wave audio engine with custom pitch and timing controls.
• 100% free and open-source with no ads or trackers.
```

---

## 🇵🇹 Versão Portuguesa (Para Ficha de Loja em Português)

Se adicionar a localização em Português na Play Console:

### Nome da aplicação:
```text
MorseGO: Manipulador & Treino
```

### Breve descrição:
```text
Aprenda código Morse com árvore binária interativa e manipuladores físicos USB!
```

### Descrição completa:
```text
Aprenda e domine o Código Morse (CW) com o MorseGO — a aplicação definitiva que combina a pedagogia visual da Árvore Binária, um motor de manipulador iâmbico avançado e suporte plug-and-play para manipuladores físicos USB de duplo paddle!

Quer seja radioamador, esteja a preparar-se para o exame de telegrafia ou pretenda simplesmente praticar a receção e envio com ritmo perfeito, o MorseGO oferece um método intuitivo, rigoroso e agradável.

---

🌳 APRENDIZAGEM PELA ÁRVORE BINÁRIA
Esqueça a memorização alfabética monótona! O MorseGO guia-o através da estrutura lógica da Árvore Binária Morse:
• Movimento para a Esquerda corresponde a um DIT (ponto: •)
• Movimento para a Direita corresponde a um DAH (traço: —)

Evolua ao longo de 21 níveis pedagógicos progressivos:
• Níveis 1 a 7: Letras fundamentais introduzidas por profundidade na árvore (começando por E e T, I, A, N, M).
• Níveis 8 a 13: Domínio de todo o alfabeto de 26 letras.
• Níveis 14 a 18: Números (0 a 9) organizados em pares.
• Níveis 19 a 21: Pontuação e pro-sinais internacionais (., ?, /, SOS, AR).

---

🔌 SUPORTE NATIVO PARA MANIPULADORES FÍSICOS USB
Sinta a sensação real de telegrafia no seu smartphone ou tablet:
• Plug & Play: Conecte manipuladores físicos USB Type-C ou 3.5mm (compatível com a PCB "CW Keyer Automatic Trainer 4 ND Magnet Bases" e keyers VBand).
• Modos de Hardware: Suporte nativo para modo teclado USB HID (Left/Right Ctrl) e modo de emulação de rato.
• Calibração em 1 Toque: Mapeie facilmente qualquer paddle ou pedal externo.
• Inversão de Pás: Alterne instantaneamente entre operação dextra e esquerdina.

---

⚡ MOTOR ELETRÓNICO IAMBIC DE ALTA PRECISÃO
• Modo Iambic B (Curtis com memória).
• Modo Iambic A.
• Modo Straight Key (Manual) para manipuladores clássicos de batente ou tecla simples.
• Pás táteis virtuais com resposta háptica vibratória e animação visual.

---

🎛️ TEMPORIZAÇÃO E PERSONALIZAÇÃO TOTAL
• Velocidade Ajustável: 5 WPM a 45 WPM (Padrão: 10 WPM).
• Espaçamento Flexível: Espaçamento entre letras regulável (2 a 12 dits, padrão 720ms) e entre palavras (5 a 20 dits, padrão 1560ms).
• Síntese de Áudio Pura: Tom sinusoidal PCM suave e sem ruído de clique, com frequência ajustável (400 Hz a 1000 Hz, padrão 700 Hz).
• Modo Silencioso: Pratique discretamente com vibração tátil sincronizada com o padrão internacional PARIS.
• Descodificador em Tempo Real: Transcreve o ritmo das suas pás para texto com diagnóstico imediato de ritmo.

---

🔒 100% GRATUITO, OPEN-SOURCE E PRIVACIDADE TOTAL
• Sem publicidade.
• Sem rastreadores ou recolha de dados pessoais.
• Não requer permissão de acesso à Internet.
• Código aberto e auditável no GitHub: https://github.com/dirtybug/morsego

Comece hoje mesmo a sua jornada no mundo da telegrafia CW com o MorseGO!
```

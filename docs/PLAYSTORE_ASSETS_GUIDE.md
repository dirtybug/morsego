# Guia Completo de Publicação na Google Play Store - MorseGO

Este documento contém todas as especificações técnicas, textos de marketing e ficheiros de imagem gerados para a publicação da aplicação **MorseGO** na Google Play Console.

---

## 1. Inventário de Imagens Geradas para a Play Store

Todos os ficheiros abaixo foram gerados com as dimensões, proporções e resoluções exatas exigidas pela Google Play Console:

| Tipo de Imagem | Resolução Exigida | Ficheiro Gerado | Localização no Repositório |
|---|---|---|---|
| **Ícone da App (Hi-Res)** | 512 × 512 px (PNG 32-bit com alfa) | `playstore_app_icon_512x512.png` | [`docs/playstore/playstore_app_icon_512x512.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/playstore_app_icon_512x512.png) |
| **Gráfico de Destaque (Feature Graphic)** | 1024 × 500 px (PNG/JPEG sem alfa) | `playstore_feature_graphic_1024x500.png` | [`docs/playstore/playstore_feature_graphic_1024x500.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/playstore_feature_graphic_1024x500.png) |
| **Screenshot 1: Árvore Binária** | 1080 × 1920 px (9:16) | `playstore_screen_1_binary_tree.png` | [`docs/playstore/screenshots/playstore_screen_1_binary_tree.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_1_binary_tree.png) |
| **Screenshot 2: Estudo de Nível** | 1080 × 1920 px (9:16) | `playstore_screen_2_level_study.png` | [`docs/playstore/screenshots/playstore_screen_2_level_study.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_2_level_study.png) |
| **Screenshot 3: Teste de Escuta** | 1080 × 1920 px (9:16) | `playstore_screen_3_listening_exam.png` | [`docs/playstore/screenshots/playstore_screen_3_listening_exam.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_3_listening_exam.png) |
| **Screenshot 4: Pás de Transmissão** | 1080 × 1920 px (9:16) | `playstore_screen_4_transmission_exam.png` | [`docs/playstore/screenshots/playstore_screen_4_transmission_exam.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_4_transmission_exam.png) |
| **Screenshot 5: Transmissão de Palavras** | 1080 × 1920 px (9:16) | `playstore_screen_5_word_transmission.png` | [`docs/playstore/screenshots/playstore_screen_5_word_transmission.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_5_word_transmission.png) |
| **Screenshot 6: Ham Radio CW & QSO** | 1080 × 1920 px (9:16) | `playstore_screen_6_ham_radio_cw.png` | [`docs/playstore/screenshots/playstore_screen_6_ham_radio_cw.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_6_ham_radio_cw.png) |
| **Screenshot 7: Calibração de Hardware** | 1080 × 1920 px (9:16) | `playstore_screen_7_hardware_calibration.png` | [`docs/playstore/screenshots/playstore_screen_7_hardware_calibration.png`](file:///C:/Users/JúlioAndrade/morseGo/docs/playstore/screenshots/playstore_screen_7_hardware_calibration.png) |

---

## 2. Metadados Oficiais para a Ficha da Play Store

### 2.1 Nome da Aplicação (Title)
- **Limite:** Máximo 30 caracteres.
- **Português:** `MorseGO - Código Morse CW` (25 caracteres)
- **Inglês:** `MorseGO - CW Morse Code Tutor` (29 caracteres)

### 2.2 Descrição Curta (Short Description)
- **Limite:** Máximo 80 caracteres.
- **Português:** `Aprenda telegrafia Morse com Árvore Binária visual, áudio e cadência PARIS.` (75 caracteres)
- **Inglês:** `Learn Morse code CW with visual Binary Tree, dynamic audio & PARIS timing.` (74 caracteres)

### 2.3 Descrição Completa (Full Description)
- **Limite:** Máximo 4000 caracteres.

```markdown
Aprenda código Morse (CW) de forma intuitiva, visual e rigorosa com o MorseGO!

O MorseGO combina a pedagogia da Árvore Binária com o treino auditivo de alta fidelidade e a manipulação tátil com verificação de cadência profissional (Padrão Internacional PARIS ITU).

🌟 PRINCIPAIS FUNCIONALIDADES:

🌳 ÁRVORE BINÁRIA INTERATIVA
• Visualize a estrutura lógica do código Morse sem tabelas enfadonhas.
• Ramo esquerdo = TRAÇO (Dah / —), Ramo direito = PONTO (Dit / •).
• Navegação com linhas diagonais diretas, nós coloridos e bloqueio gradual por níveis.
• Suporte completo a orientação Vertical (Portrait) e Horizontal (Landscape).

📚 PROGRESSÃO EM 13 NÍVEIS
• Nível 1 ao 13: desbloqueie 2 novos caracteres por nível (E & T, I & A, N & M, até números e sinais de pontuação).
• Cada nível apresenta vocabulário progressivo gerado dinamicamente.

🎧 EXAMES DE DUAS ETAPAS (OUVIR E TRANSMITIR)
• Etapa 1 (Escuta): Treine o ouvido musical identificando caracteres e palavras em tempo real.
• Etapa 2 (Transmissão): Use as pás táteis integradas (Iambic Paddles) para transmitir.
• Código Morse de palavras oculto que se revela progressivamente letra a letra.
• Sistema de 3 vidas (❤️❤️❤️) com penalidades pedagógicas imediatas.

⏱️ CALIBRAÇÃO DE CADÊNCIA PARIS (20 WPM)
• Avaliação precisa da duração do Dit (60ms a 20 WPM) e Dah (180ms - relação exata 3:1).
• Deteção instantânea de erros de cadência com avisos explícitos.
• Ajuste contínuo de velocidade de 5 a 50 WPM e frequência de pitch de 400 a 1000 Hz.

📻 VOCABULÁRIO DE RADIOAMADOR & QSO
• Pratique chamadas gerais (CQ), códigos Q (QSL, QTH, QSO, QRZ) e saudações (73, 88, SOS).
• Simulação realista de diálogos de telegrafia entre operadores.

📱 COMPATIBILIDADE DE HARDWARE
• Suporte a manipuladores de telegrafia USB externos (USB OTG / Teclado / Chave reta / Chave iâmbica).
• Retenção de estado completa ao rodar o telemóvel entre vertical e horizontal.
• Modo silencioso com vibração tátil sincronizada para treino discreto.

Comece hoje a sua jornada como telegrafista amador ou profissional com o MorseGO!
```

---

## 3. Como Correr os Testes e o Release no Docker

Todo o ciclo de vida da aplicação está encapsulado no Docker (garantindo o mesmo resultado em Windows, Linux, macOS e CI/CD):

### 3.1 Executar a Suite Completa de Testes no Docker:
```cmd
run-docker-tests.bat all
```
*Executa testes unitários, validação de regras de sintaxe, lint e testes de instrumentação/comportamento.*

### 3.2 Compilar o Release APK no Docker:
```cmd
run-docker-release.bat
```
*Gera o ficheiro `build-apks/morseGO-release.apk` pronto para assinatura e publicação.*

---

## 4. Checklist para Envio na Google Play Console

1. **Criar Nova App:** Selecionar Idioma predefinido (Português de Portugal ou Inglês) e tipo "Aplicação Gratuita".
2. **Ficha da Loja (Store Listing):**
   - Inserir Título, Descrição Curta e Descrição Completa (Secção 2).
   - Fazer upload do ícone `playstore_app_icon_512x512.png`.
   - Fazer upload do gráfico `playstore_feature_graphic_1024x500.png`.
   - Fazer upload das 7 capturas de ecrã em `docs/playstore/screenshots/`.
3. **Classificação de Conteúdo:** Preencher o questionário IARC (Sem violência, Livre / PEGI 3).
4. **Público-Alvo:** 13+ anos ou Todas as Idades.
5. **Faixa de Lançamento (Release Track):**
   - Produção ou Teste Fechado (Closed Testing).
   - Fazer upload do ficheiro `morseGO-release.apk` gerado pelo Docker.

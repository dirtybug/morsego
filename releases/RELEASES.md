# Catálogo Oficial de Versões & Releases • MorseGO

Este documento mantém o histórico estruturado de todas as versões lançadas do **MorseGO**, com separação estrita entre binários de **Releases** e relatórios de **Testes**, cada um organizado na sua respetiva pasta versionada.

---

## 1. Índice Global de Versões

| Versão (Tag) | Data de Lançamento | Pasta de Release | Pasta de Testes | Cobertura / Testes | Estado |
|---|---|---|---|---|---|
| [`v1.0.0`](#v100---lançamento-inicial-oficial) | 2026-09-11 | [`releases/v1.0.0/`](file:///C:/Users/JúlioAndrade/morseGo/releases/v1.0.0/) | [`tests/v1.0.0/`](file:///C:/Users/JúlioAndrade/morseGo/tests/v1.0.0/) | 79 Unitários + 49 Comportamento (100%) | ✅ Aprovado |

---

## 2. Estrutura de Diretórios por Versão

Cada versão publicada gera automaticamente pastas isoladas para binários e testes:

```
morseGO/
├── releases/
│   ├── RELEASES.md                         <-- Este catálogo cumulativo
│   └── v1.0.0/
│       ├── morseGO-v1.0.0-release.apk      <-- APK de produção (Google Play)
│       ├── morseGO-v1.0.0-debug.apk        <-- APK de depuração e testes
│       ├── SHA256SUMS.txt                  <-- Hashes criptográficos de integridade
│       └── release-manifest.json           <-- Metadados em formato JSON para automação
└── tests/
    └── v1.0.0/
        ├── index.html                      <-- Relatório interativo visual
        ├── morseGO-test-suite-v1.0.0.zip   <-- Pacote zip completo de relatórios e telas
        ├── unit-tests/                     <-- Relatórios HTML dos 79 testes unitários JVM
        ├── behavior-tests/                 <-- Relatórios dos 49 testes instrumentados Android
        └── screenshots/                    <-- As 49 capturas de ecrã verificadas
```

---

## 3. Detalhes das Versões

### v1.0.0 - Lançamento Inicial Oficial
- **Data:** 11 de Setembro de 2026
- **Tag Git:** `v1.0.0`
- **Compilação:** Docker container `eclipse-temurin:17-jdk-jammy` / Android SDK 34 / Gradle 8.2

#### 📦 Ficheiros da Release (`releases/v1.0.0/`):
- `morseGO-v1.0.0-release.apk` (APK assinado para distribuição)
- `morseGO-v1.0.0-debug.apk` (APK com símbolos de debug)
- `SHA256SUMS.txt` (Assinatura digital dos binários)
- `release-manifest.json`

#### 🧪 Ficheiros de Testes (`tests/v1.0.0/`):
- **Testes Unitários:** 79 testes executados e aprovados (100% de métodos em 15 ficheiros).
- **Testes de Comportamento:** 49 testes instrumentados e screenshots verificados.
- **Validações Críticas:**
  - Árvore Binária Morse sem ângulos a 90° (linhas diagonais diretas).
  - Cadência padrão PARIS ITU (50 dits, intra-elemento 1 Dit, entre letras 3 Dits).
  - Transmissão progressiva de palavras com letras ocultas `[ ? ]`.
  - Separação bilíngue estrita (100% Inglês ou 100% Português sem misturas).
  - Rotação suave entre Vertical (Portrait) e Horizontal (Landscape).
  - Modo silencioso com vibração tátil sincronizada.
  - Vocabulário operacional Ham Radio QSO (CQ, 73, DX, QSL, SOS).

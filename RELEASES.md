# 📦 MorseGO - Lista de Releases Oficiais

Este documento mantém o registo histórico oficial de todas as versões lançadas do **MorseGO**, incluindo metadados da Google Play Store, pacotes APK, relatórios de testes e integridade SHA256.

---

<!-- RELEASES_LIST_START -->
## [v1.0.0] - 2026-09-11

### 📱 Metadados Google Play Store
- **Nome da Versão (`versionName`):** `1.0.0`
- **Código da Versão (`versionCode`):** `10000`
- **Ambiente de Build:** Docker Hermético (OpenJDK 17 + Android SDK 34)
- **Target SDK:** Android 34 (Android 14) / Min SDK: Android 24 (Android 7.0)

### 🧪 Testes e Qualidade
- **Suíte de Testes:** 100% de Aprovação (86 Testes Unitários JVM + 49 Testes de Comportamento Android)
- **Relatório de Testes:** [Dashboard Central de Testes](release/v1.0.0/reports/index.html)
- **Galeria Visual:** [Screenshots de Comportamento](release/v1.0.0/reports/behavior-tests/index.html) (95 capturas em modo retrato, paisagem e rotação a 90°)

### 📥 Artefatos de Instalação
| Artefato | Ficheiro | Descrição |
| :--- | :--- | :--- |
| **APK Release** | [`morseGO-v1.0.0-release.apk`](release/v1.0.0/morseGO-v1.0.0-release.apk) | Binário de produção otimizado para a Google Play Store |
| **APK Debug** | [`morseGO-v1.0.0-debug.apk`](release/v1.0.0/morseGO-v1.0.0-debug.apk) | Binário com logging ativado para depuração |
| **Manifesto JSON** | [`release-manifest.json`](release/v1.0.0/release-manifest.json) | Metadados da release para consumo automatizado |
| **Sumário de Testes** | [`test-summary.json`](release/v1.0.0/test-summary.json) | Registo de cobertura e validações dos testes |
| **Checksums** | [`SHA256SUMS.txt`](release/v1.0.0/SHA256SUMS.txt) | Assinaturas de verificação de integridade dos ficheiros |

### 🚀 Funcionalidades da Versão
- **Árvore Binária de Morse Completa:** 21 níveis de aprendizagem progressiva (letras, números e sinais prosigns).
- **Temporização PARIS de Alta Precisão:** Ritmo a 20 WPM com tolerância dinâmica adaptativa.
- **Manipulador Físico USB Type-C:** Suporte nativo ao manipulador de pás duplas da Amazon (*CW Keyer Automatic Trainer PCB*).
- **Modos de Operação:** Transmissão (Keyer), Recepção (Árvore interativa), Exames de Escuta e Envio.
- **Layouts Adaptativos:** Orientação vertical (*portrait*) e horizontal (*landscape*) em ecrãs de telemóveis e tablets.
<!-- RELEASES_LIST_END -->

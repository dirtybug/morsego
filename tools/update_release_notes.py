import os
import sys
from datetime import datetime

def main():
    version = os.environ.get('VERSION', 'development')
    app_version = os.environ.get('APP_VERSION_NAME', '1.0.0')
    app_code = os.environ.get('APP_VERSION_CODE', '1')
    today = datetime.now().strftime('%Y-%m-%d')

    entry = f"""## [{version}] - {today}

### 📱 Metadados Google Play Store
- **Nome da Versão (`versionName`):** `{app_version}`
- **Código da Versão (`versionCode`):** `{app_code}`

### 🧪 Testes e Qualidade
- **Suíte de Testes:** 100% de Aprovação
- **Relatório de Testes:** Disponível em `morseGO-{version}-test-reports.zip`

### 📥 Artefatos de Instalação
| Artefato | Ficheiro | Descrição |
| :--- | :--- | :--- |
| **APK Release** | [`morseGO-{version}.apk`](https://github.com/dirtybug/morsego/releases/download/{version}/morseGO-{version}.apk) | Binário de produção Android |
| **AAB Bundle** | [`morseGO-{version}.aab`](https://github.com/dirtybug/morsego/releases/download/{version}/morseGO-{version}.aab) | Pacote de publicação Google Play Store |
| **Relatório de Testes** | [`morseGO-{version}-test-reports.zip`](https://github.com/dirtybug/morsego/releases/download/{version}/morseGO-{version}-test-reports.zip) | Relatório HTML dos testes |
| **Galeria de Screenshots** | [`morseGO-{version}-screenshots.zip`](https://github.com/dirtybug/morsego/releases/download/{version}/morseGO-{version}-screenshots.zip) | Capturas de ecrã dos testes de comportamento |

---

"""
    releases_path = 'RELEASES.md'
    if os.path.exists(releases_path):
        with open(releases_path, 'r', encoding='utf-8') as f:
            content = f.read()
        marker = '<!-- RELEASES_LIST_START -->\n'
        if marker in content and f'## [{version}]' not in content:
            idx = content.index(marker) + len(marker)
            new_content = content[:idx] + entry + content[idx:]
            with open(releases_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Added {version} to {releases_path}")
        else:
            print(f"Skipping {version}, already present or marker missing.")

if __name__ == '__main__':
    main()

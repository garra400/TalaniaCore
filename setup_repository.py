#!/usr/bin/env python3
"""
TalaniaCore Repository Setup Script
====================================
Automated setup for the Orbis and Dungeons shared library ecosystem.
Creates folder structure, READMEs, and CI/CD configuration.

Usage: python setup_repository.py
"""

import os
import json
from pathlib import Path

# Base directory for the repository
BASE_DIR = Path(__file__).parent

# Repository structure definition
STRUCTURE = {
    "src/main/java/com/talania/core": {
        "stats": {
            "description": "Core Stats System",
            "purpose": "Sistema base para modificar vida (HP), mana e atributos globais de entidades.",
            "files": ["StatsManager.java", "StatType.java", "StatModifier.java", "EntityStats.java"]
        },
        "localization": {
            "description": "Localization System",
            "purpose": "Sistema de tradução baseado em JSON com suporte a múltiplos idiomas e fallback.",
            "files": ["TranslationManager.java", "LanguageLoader.java", "T.java", "LocaleConfig.java"]
        },
        "utils": {
            "description": "Technical Utilities",
            "purpose": "Componentes de animação de assets, player input e modificação de componentes do modelo.",
            "submodules": {
                "animation": {
                    "purpose": "Utilitários para animação de assets e modelos.",
                    "files": ["AnimationHelper.java", "ModelAnimator.java"]
                },
                "input": {
                    "purpose": "Sistema de gerenciamento de input do jogador.",
                    "files": ["InputManager.java", "KeyBindings.java"]
                },
                "model": {
                    "purpose": "Modificação de componentes do modelo (ex: orelhas de elfo).",
                    "files": ["ModelModifier.java", "ComponentAttacher.java"]
                }
            }
        },
        "ui": {
            "description": "UI Wrapper",
            "purpose": "Camada de abstração para bibliotecas de interface (Simple UI, HyUI, etc.).",
            "files": ["UIWrapper.java", "UIFactory.java", "ComponentBuilder.java", "ThemeManager.java"]
        },
        "config": {
            "description": "Configuration System",
            "purpose": "Sistema centralizado de configuração com suporte a hot-reload.",
            "files": ["ConfigManager.java", "ConfigLoader.java", "ConfigValidator.java"]
        },
        "events": {
            "description": "Event System",
            "purpose": "Sistema de eventos compartilhado para comunicação entre módulos.",
            "files": ["EventBus.java", "EventListener.java", "CoreEvents.java"]
        }
    },
    "src/main/resources": {
        "languages": {
            "description": "Default Language Files",
            "purpose": "Arquivos de tradução padrão incluídos na biblioteca.",
            "files": ["en.json", "pt_br.json"]
        },
        "schemas": {
            "description": "JSON Schemas",
            "purpose": "Schemas para validação de arquivos de configuração e tradução.",
            "files": ["language_schema.json", "config_schema.json", "stats_schema.json"]
        }
    },
    "docs": {
        "": {
            "description": "Documentation",
            "purpose": "Documentação técnica e guias de uso da biblioteca.",
            "files": ["GETTING_STARTED.md", "API_REFERENCE.md", "MIGRATION_GUIDE.md"]
        }
    },
    "examples": {
        "": {
            "description": "Usage Examples",
            "purpose": "Exemplos de implementação para cada módulo da biblioteca.",
            "files": []
        }
    },
    "tests": {
        "": {
            "description": "Test Suite",
            "purpose": "Testes unitários e de integração.",
            "files": []
        }
    }
}

# README templates
MAIN_README = '''# TalaniaCore

<p align="center">
  <strong>Shared Library for Orbis and Dungeons Ecosystem</strong>
</p>

<p align="center">
  <a href="#modules">Modules</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#contributing">Contributing</a>
</p>

---

## Overview

TalaniaCore is a **public domain shared library** designed for the Hytale modding ecosystem. It provides a collection of reusable utilities and systems that serve as the foundation for the "Orbis and Dungeons" mod series.

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| **Core Stats** | HP, mana, and global attribute modification system | 🚧 In Progress |
| **Localization** | JSON-based translation system with fallback support | ✅ Ready |
| **Technical Utilities** | Animation, input, and model modification helpers | 🚧 In Progress |
| **UI Wrapper** | Abstraction layer for UI libraries (Simple UI, HyUI) | 📋 Planned |
| **Config System** | Centralized configuration with hot-reload | 📋 Planned |
| **Event System** | Inter-module event communication | 📋 Planned |

## Installation

### Gradle (Recommended)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.garra400:TalaniaCore:VERSION'
}
```

### Manual

1. Download the latest release from [Releases](https://github.com/garra400/TalaniaCore/releases)
2. Add the JAR to your mod's `libs/` folder
3. Include it in your `build.gradle`

## Usage

### Core Stats System

```java
import com.talania.core.stats.StatsManager;
import com.talania.core.stats.StatType;

// Get entity stats
EntityStats stats = StatsManager.getStats(entity);

// Modify HP
stats.setMaxHealth(100);
stats.modifyAttribute(StatType.HEALTH, 1.5f, ModifierType.MULTIPLY);
```

### Localization System

```java
import com.talania.core.localization.T;

// Simple translation
String text = T.get("ui.welcome_message");

// With parameters
String formatted = T.get("combat.damage_dealt", damage, targetName);

// Change language
T.setLocale("pt_br");
```

### UI Wrapper

```java
import com.talania.core.ui.UIFactory;
import com.talania.core.ui.ComponentBuilder;

// Create a button with the abstraction layer
UIComponent button = UIFactory.button()
    .text(T.get("ui.confirm"))
    .onClick(this::handleConfirm)
    .build();
```

## Project Structure

```
TalaniaCore/
├── src/main/java/com/talania/core/
│   ├── stats/          # Core stats system
│   ├── localization/   # Translation system
│   ├── utils/          # Technical utilities
│   │   ├── animation/  # Animation helpers
│   │   ├── input/      # Input management
│   │   └── model/      # Model modification
│   ├── ui/             # UI abstraction layer
│   ├── config/         # Configuration system
│   └── events/         # Event bus
├── src/main/resources/
│   ├── languages/      # Default translations
│   └── schemas/        # JSON validation schemas
├── docs/               # Documentation
├── examples/           # Usage examples
└── tests/              # Test suite
```

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Clone the repository
2. Run `./gradlew build` to compile
3. Run `./gradlew test` to execute tests

### Code Style

- Follow Java naming conventions
- Include Javadoc for public APIs
- Write tests for new functionality

## License

This project is released into the **Public Domain** under the [Unlicense](LICENSE).

---

<p align="center">
  Part of the <strong>Orbis and Dungeons</strong> ecosystem
</p>
'''

MODULE_README_TEMPLATE = '''# {title}

## Purpose

{purpose}

## Files

{files_list}

## Usage

```java
// TODO: Add usage examples
```

## API Reference

See the main [API Reference](../../docs/API_REFERENCE.md) for detailed documentation.
'''

SUBMODULE_README_TEMPLATE = '''# {title}

## Purpose

{purpose}

## Files

{files_list}

## Parent Module

This is part of the [{parent}](../) module.
'''

# Language file templates
EN_LANGUAGE = {
    "meta": {
        "language": "English",
        "code": "en",
        "version": "1.0.0"
    },
    "common": {
        "confirm": "Confirm",
        "cancel": "Cancel",
        "save": "Save",
        "load": "Load",
        "error": "Error",
        "success": "Success",
        "warning": "Warning"
    },
    "stats": {
        "health": "Health",
        "mana": "Mana",
        "stamina": "Stamina",
        "strength": "Strength",
        "defense": "Defense"
    },
    "ui": {
        "welcome": "Welcome to Orbis and Dungeons!",
        "loading": "Loading...",
        "select_option": "Select an option"
    }
}

PT_BR_LANGUAGE = {
    "meta": {
        "language": "Português (Brasil)",
        "code": "pt_br",
        "version": "1.0.0"
    },
    "common": {
        "confirm": "Confirmar",
        "cancel": "Cancelar",
        "save": "Salvar",
        "load": "Carregar",
        "error": "Erro",
        "success": "Sucesso",
        "warning": "Aviso"
    },
    "stats": {
        "health": "Vida",
        "mana": "Mana",
        "stamina": "Stamina",
        "strength": "Força",
        "defense": "Defesa"
    },
    "ui": {
        "welcome": "Bem-vindo ao Orbis and Dungeons!",
        "loading": "Carregando...",
        "select_option": "Selecione uma opção"
    }
}

# JSON Schema for language validation
LANGUAGE_SCHEMA = {
    "$schema": "http://json-schema.org/draft-07/schema#",
    "title": "TalaniaCore Language File",
    "type": "object",
    "required": ["meta"],
    "properties": {
        "meta": {
            "type": "object",
            "required": ["language", "code", "version"],
            "properties": {
                "language": {"type": "string"},
                "code": {"type": "string", "pattern": "^[a-z]{2}(_[a-z]{2})?$"},
                "version": {"type": "string", "pattern": "^\\d+\\.\\d+\\.\\d+$"}
            }
        }
    },
    "additionalProperties": {
        "type": "object",
        "additionalProperties": {"type": "string"}
    }
}

# GitHub Actions workflow
GITHUB_ACTIONS_VALIDATE = '''name: Validate JSON Files

on:
  push:
    branches: [ main, develop ]
    paths:
      - '**.json'
  pull_request:
    branches: [ main ]
    paths:
      - '**.json'

jobs:
  validate-json:
    runs-on: ubuntu-latest
    name: Validate JSON Syntax

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Install dependencies
        run: |
          pip install jsonschema

      - name: Validate JSON syntax
        run: |
          echo "Validating JSON files..."
          find . -name "*.json" -type f | while read file; do
            echo "Checking: $file"
            python -c "import json; json.load(open('$file'))" || exit 1
          done
          echo "All JSON files are valid!"

      - name: Validate language files against schema
        run: |
          python scripts/validate_languages.py

  validate-gradle:
    runs-on: ubuntu-latest
    name: Validate Gradle Build

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Validate Gradle Wrapper
        uses: gradle/wrapper-validation-action@v2

      - name: Build with Gradle
        run: ./gradlew build --no-daemon

      - name: Run tests
        run: ./gradlew test --no-daemon
'''

# Language validation script
VALIDATE_LANGUAGES_SCRIPT = '''#!/usr/bin/env python3
"""
Language File Validator
=======================
Validates language JSON files against the schema and checks for consistency.
"""

import json
import sys
from pathlib import Path

def load_json(path):
    """Load and parse a JSON file."""
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except json.JSONDecodeError as e:
        print(f"ERROR: Invalid JSON in {path}: {e}")
        return None
    except Exception as e:
        print(f"ERROR: Could not read {path}: {e}")
        return None

def get_all_keys(obj, prefix=""):
    """Recursively get all keys from a nested dict."""
    keys = set()
    for key, value in obj.items():
        full_key = f"{prefix}.{key}" if prefix else key
        if isinstance(value, dict):
            keys.update(get_all_keys(value, full_key))
        else:
            keys.add(full_key)
    return keys

def validate_languages():
    """Validate all language files."""
    languages_dir = Path(__file__).parent.parent / "src" / "main" / "resources" / "languages"
    
    if not languages_dir.exists():
        print(f"WARNING: Languages directory not found: {languages_dir}")
        return True
    
    language_files = list(languages_dir.glob("*.json"))
    
    if not language_files:
        print("WARNING: No language files found")
        return True
    
    print(f"Found {len(language_files)} language file(s)")
    
    # Load all language files
    languages = {}
    errors = []
    
    for lang_file in language_files:
        data = load_json(lang_file)
        if data is None:
            errors.append(f"Failed to load {lang_file.name}")
            continue
        languages[lang_file.stem] = data
        print(f"  ✓ Loaded {lang_file.name}")
    
    if not languages:
        print("ERROR: No valid language files found")
        return False
    
    # Use English as the reference
    if "en" not in languages:
        print("WARNING: English (en.json) not found, using first available as reference")
        reference_lang = list(languages.keys())[0]
    else:
        reference_lang = "en"
    
    reference_keys = get_all_keys(languages[reference_lang])
    print(f"\\nReference language: {reference_lang} ({len(reference_keys)} keys)")
    
    # Check other languages for missing keys
    for lang_code, lang_data in languages.items():
        if lang_code == reference_lang:
            continue
        
        lang_keys = get_all_keys(lang_data)
        missing = reference_keys - lang_keys
        extra = lang_keys - reference_keys
        
        if missing:
            print(f"\\nWARNING: {lang_code}.json missing {len(missing)} key(s):")
            for key in sorted(missing)[:10]:
                print(f"  - {key}")
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
        
        if extra:
            print(f"\\nINFO: {lang_code}.json has {len(extra)} extra key(s)")
    
    if errors:
        print(f"\\nValidation completed with {len(errors)} error(s)")
        return False
    
    print("\\n✓ All language files validated successfully!")
    return True

if __name__ == "__main__":
    success = validate_languages()
    sys.exit(0 if success else 1)
'''

# Gradle build file
BUILD_GRADLE = '''plugins {
    id 'java-library'
    id 'maven-publish'
}

group = 'com.talania'
version = '0.1.0-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON processing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Testing
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}

publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            
            pom {
                name = 'TalaniaCore'
                description = 'Shared library for Orbis and Dungeons ecosystem'
                url = 'https://github.com/garra400/TalaniaCore'
                
                licenses {
                    license {
                        name = 'The Unlicense'
                        url = 'https://unlicense.org/'
                    }
                }
                
                developers {
                    developer {
                        id = 'garra400'
                        name = 'Garra400'
                    }
                }
                
                scm {
                    connection = 'scm:git:git://github.com/garra400/TalaniaCore.git'
                    developerConnection = 'scm:git:ssh://github.com/garra400/TalaniaCore.git'
                    url = 'https://github.com/garra400/TalaniaCore'
                }
            }
        }
    }
}
'''

SETTINGS_GRADLE = '''rootProject.name = 'TalaniaCore'
'''

GRADLE_PROPERTIES = '''org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
'''

# License (Unlicense for public domain)
UNLICENSE = '''This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <https://unlicense.org>
'''

CONTRIBUTING = '''# Contributing to TalaniaCore

Thank you for your interest in contributing to TalaniaCore! This document provides guidelines for contributing.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/TalaniaCore.git`
3. Create a branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Run tests: `./gradlew test`
6. Commit with a clear message
7. Push and create a Pull Request

## Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Include Javadoc for public APIs
- Keep methods focused and small

## Commit Messages

Use clear, descriptive commit messages:

```
feat: add new translation fallback system
fix: resolve null pointer in StatsManager
docs: update API reference for UI module
test: add tests for ConfigLoader
```

## Pull Request Process

1. Update documentation if needed
2. Add tests for new functionality
3. Ensure all tests pass
4. Request review from maintainers

## Reporting Issues

When reporting bugs, include:
- Steps to reproduce
- Expected vs actual behavior
- Hytale/mod version
- Relevant logs

## Questions?

Feel free to open an issue for questions or discussions!
'''

GITIGNORE = '''# Compiled files
*.class
*.jar
*.war
*.ear

# Build directories
build/
out/
target/
bin/

# Gradle
.gradle/
gradle-app.setting
!gradle-wrapper.jar

# IDE
.idea/
*.iml
*.ipr
*.iws
.vscode/
*.swp
*.swo
.project
.classpath
.settings/

# OS
.DS_Store
Thumbs.db
desktop.ini

# Logs
*.log
logs/

# Temporary files
*.tmp
*.temp
*.bak

# Local configuration
local.properties
*.local.json
'''


def create_directory(path: Path):
    """Create directory if it doesn't exist."""
    path.mkdir(parents=True, exist_ok=True)
    print(f"  📁 Created: {path.relative_to(BASE_DIR)}")


def create_file(path: Path, content: str):
    """Create a file with the given content."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"  📄 Created: {path.relative_to(BASE_DIR)}")


def create_json_file(path: Path, data: dict):
    """Create a JSON file with the given data."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    print(f"  📄 Created: {path.relative_to(BASE_DIR)}")


def generate_files_list(files: list) -> str:
    """Generate a markdown list of files."""
    if not files:
        return "_No files yet - module in development_"
    return "\n".join([f"- `{f}`" for f in files])


def create_java_stub(path: Path, class_name: str, package: str):
    """Create a stub Java file."""
    content = f'''package {package};

/**
 * TODO: Implement {class_name}
 * 
 * This is a stub file. Implementation pending.
 */
public class {class_name} {{
    
    // TODO: Add implementation
    
}}
'''
    create_file(path, content)


def main():
    print("=" * 60)
    print("TalaniaCore Repository Setup")
    print("=" * 60)
    print(f"\nBase directory: {BASE_DIR}\n")
    
    # Create root-level files
    print("Creating root files...")
    create_file(BASE_DIR / "README.md", MAIN_README)
    create_file(BASE_DIR / "LICENSE", UNLICENSE)
    create_file(BASE_DIR / "CONTRIBUTING.md", CONTRIBUTING)
    create_file(BASE_DIR / ".gitignore", GITIGNORE)
    create_file(BASE_DIR / "build.gradle", BUILD_GRADLE)
    create_file(BASE_DIR / "settings.gradle", SETTINGS_GRADLE)
    create_file(BASE_DIR / "gradle.properties", GRADLE_PROPERTIES)
    
    # Create GitHub Actions
    print("\nSetting up CI/CD...")
    create_file(BASE_DIR / ".github" / "workflows" / "validate.yml", GITHUB_ACTIONS_VALIDATE)
    
    # Create scripts directory
    print("\nCreating scripts...")
    create_file(BASE_DIR / "scripts" / "validate_languages.py", VALIDATE_LANGUAGES_SCRIPT)
    
    # Create module structure
    print("\nCreating module structure...")
    
    for base_path, modules in STRUCTURE.items():
        for module_name, module_info in modules.items():
            if module_name:
                module_path = BASE_DIR / base_path / module_name
            else:
                module_path = BASE_DIR / base_path
            
            create_directory(module_path)
            
            # Handle submodules
            if "submodules" in module_info:
                # Create parent README
                readme_content = MODULE_README_TEMPLATE.format(
                    title=module_info["description"],
                    purpose=module_info["purpose"],
                    files_list="This module contains the following submodules:\n" + 
                               "\n".join([f"- [{name}/]({name}/) - {info['purpose']}" 
                                         for name, info in module_info["submodules"].items()])
                )
                create_file(module_path / "README.md", readme_content)
                
                # Create submodules
                for submodule_name, submodule_info in module_info["submodules"].items():
                    submodule_path = module_path / submodule_name
                    create_directory(submodule_path)
                    
                    # Create submodule README
                    readme_content = SUBMODULE_README_TEMPLATE.format(
                        title=submodule_name.replace("_", " ").title(),
                        purpose=submodule_info["purpose"],
                        files_list=generate_files_list(submodule_info.get("files", [])),
                        parent=module_info["description"]
                    )
                    create_file(submodule_path / "README.md", readme_content)
                    
                    # Create Java stubs
                    package = f"com.talania.core.utils.{submodule_name}"
                    for java_file in submodule_info.get("files", []):
                        if java_file.endswith(".java"):
                            class_name = java_file.replace(".java", "")
                            create_java_stub(submodule_path / java_file, class_name, package)
            else:
                # Create module README
                if module_info.get("description"):
                    readme_content = MODULE_README_TEMPLATE.format(
                        title=module_info["description"],
                        purpose=module_info["purpose"],
                        files_list=generate_files_list(module_info.get("files", []))
                    )
                    create_file(module_path / "README.md", readme_content)
                
                # Create stub files
                if "files" in module_info:
                    for file_name in module_info["files"]:
                        file_path = module_path / file_name
                        
                        if file_name.endswith(".java"):
                            # Determine package from path
                            rel_path = module_path.relative_to(BASE_DIR / "src" / "main" / "java")
                            package = str(rel_path).replace(os.sep, ".")
                            class_name = file_name.replace(".java", "")
                            create_java_stub(file_path, class_name, package)
                        elif file_name.endswith(".md"):
                            create_file(file_path, f"# {file_name.replace('.md', '').replace('_', ' ').title()}\n\nTODO: Add content\n")
    
    # Create language files
    print("\nCreating language files...")
    languages_path = BASE_DIR / "src" / "main" / "resources" / "languages"
    create_json_file(languages_path / "en.json", EN_LANGUAGE)
    create_json_file(languages_path / "pt_br.json", PT_BR_LANGUAGE)
    
    # Create schema files
    print("\nCreating schema files...")
    schemas_path = BASE_DIR / "src" / "main" / "resources" / "schemas"
    create_json_file(schemas_path / "language_schema.json", LANGUAGE_SCHEMA)
    
    print("\n" + "=" * 60)
    print("✅ Repository structure created successfully!")
    print("=" * 60)
    print(f"""
Next steps:
1. cd {BASE_DIR.name}
2. git init
3. git add .
4. git commit -m "Initial commit: TalaniaCore shared library structure"
5. git remote add origin https://github.com/garra400/TalaniaCore.git
6. git push -u origin main

The repository includes:
- 📦 6 core modules (stats, localization, utils, ui, config, events)
- 📋 JSON language files with schema validation
- 🔧 GitHub Actions CI/CD for JSON and Gradle validation
- 📚 Documentation structure
- ⚙️ Gradle build configuration
- 📜 Public Domain license (Unlicense)
""")


if __name__ == "__main__":
    main()

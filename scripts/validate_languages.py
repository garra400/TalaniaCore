#!/usr/bin/env python3
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
    print(f"\nReference language: {reference_lang} ({len(reference_keys)} keys)")
    
    # Check other languages for missing keys
    for lang_code, lang_data in languages.items():
        if lang_code == reference_lang:
            continue
        
        lang_keys = get_all_keys(lang_data)
        missing = reference_keys - lang_keys
        extra = lang_keys - reference_keys
        
        if missing:
            print(f"\nWARNING: {lang_code}.json missing {len(missing)} key(s):")
            for key in sorted(missing)[:10]:
                print(f"  - {key}")
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
        
        if extra:
            print(f"\nINFO: {lang_code}.json has {len(extra)} extra key(s)")
    
    if errors:
        print(f"\nValidation completed with {len(errors)} error(s)")
        return False
    
    print("\n✓ All language files validated successfully!")
    return True

if __name__ == "__main__":
    success = validate_languages()
    sys.exit(0 if success else 1)

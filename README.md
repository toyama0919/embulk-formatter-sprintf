# Sprintf formatter plugin for Embulk

Formats Sprintf(Java String#format) files for other file output plugins.

## Overview

* **Plugin type**: formatter

## Configuration

- **format**: format (string, required)
- **column_keys**: column_keys (array, required)
- **null_string**: null_string (string, default: '')

## Example

```yaml
out:
  type: any output input plugin type
  formatter:
    type: sprintf
    null_string: null
    format: "    - { id: %s, url: '%s' }\n"
    column_keys:
      - id
      - url
```

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

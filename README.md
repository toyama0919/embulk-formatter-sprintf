# Sprintf formatter plugin for Embulk

Formats Sprintf(Java String#format) files for other file output plugins.

## Overview

* **Plugin type**: formatter

## Configuration

- **format**: format (string, required)
- **column_keys**: column_keys (array, required)
- **null_string**: null_string (string, default: '')
- **header_string**: format (string, default: '')
- **footer_string**: format (string, default: '')

## Example

json list

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

xml

```yaml
out:
  type: any output input plugin type
  formatter:
    type: sprintf
    null_string: null
    header_string: "<?xml version=\"1.0\" encodint=\"UTF-8\" ?><response><users>"
    footer_string: "</users></response>"
    format: "<user><id><![CDATA[%s]]></id><url><![CDATA[%s]]></url></user>\n"
    column_keys:
      - id
      - url
```

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

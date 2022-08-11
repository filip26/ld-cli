# Linked Data Command Line Interface

A simple command line utility allowing to process JSON-LD, RDF, and CBOR-LD, documents. The goal is to provide native executables compiled for Ubuntu, Mac and Windows.

## Features

* JSON-LD 1.1
* CBOR-LD

## Installation

[Download](https://github.com/filip26/ld-cli/releases/tag/v0.7.0)

```bash
$ unzip ld-cli-....zip
$ chmod +x jsonld
```

## Usage

```bash
$ ld-cli -h
Usage: ld-cli [-hv] [COMMAND]

Linked Data Command Line Processor

Options:
  -h, --help      display help message
  -v, --version   display a version

Commands:
  expand      Expand JSON-LD document
  compact     Compact JSON-LD document using the context
  flatten     Flatten JSON-LD document and optionally compacts it using a context
  frame       Frame JSON-LD document using the frame
  fromrdf     Transform N-Quads document into a JSON-LD document in expanded form
  tordf       Transform JSON-LD document into N-Quads document
  compress    Compress JSON-LD document into CBOR-LD  
  decompress  Decompress CBOR-LD document into JSON-LD
```

### Pipeline
```bash
cat document.json | ld-cli expand --ordered --pretty > expanded.jsonld
```

### `file:/` scheme support

```bash
> ld-cli compress -i file:/home/filip/example.jsonld output.cborld
```

## Contributing

All PR's welcome!

### Building

1. [Install GraalVM and Native Image](https://www.graalvm.org/java/quickstart/)
   - download and unpack ```graalvm-ce-java17-[platform]-[version].tar.gz```
   - set ```JAVA_HOME``` and ```PATH``` env variables
   - ```gu install native-image```
3. ```mvn clean package -P native-image```
4. ```./target/ld-cli```


## Resources

* [Titanium JSON-LD 1.1 Processor](https://github.com/filip26/titanium-json-ld)
* [Iridium CBOR-LD Processor](https://github.com/filip26/iridium-cbor-ld)

## Sponsors

<a href="https://github.com/thadguidry">
  <img src="https://avatars.githubusercontent.com/u/986438?v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com

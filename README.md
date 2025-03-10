
> [!IMPORTANT]
> Your feedback is essential to the improvement of this library. Please share any concerns, primary use cases, areas for enhancement, or challenges you have encountered. Your insights help refine and optimize the library to better meet user needs. Thank you for your time and contributions.

# Linked Data CLI

A simple command-line utility designed to process JSON-LD, RDF, and CBOR-LD documents. The goal is to provide native executables compiled for Ubuntu, macOS, and Windows.

## Features

* [JSON-LD 1.1](https://www.w3.org/TR/json-ld/) 
* [CBOR-LD 1.0](https://json-ld.github.io/cbor-ld-spec/)

## Status

[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/filip26/ld-cli?include_prereleases)](https://github.com/filip26/ld-cli/releases)
[![Downloads](https://img.shields.io/github/downloads/filip26/ld-cli/total)](https://github.com/filip26/ld-cli/releases)

## Installation

[Downloads](https://github.com/filip26/ld-cli/releases/)

```bash
> unzip ld-cli-....zip
> chmod +x ld-cli
```

## Usage

```bash
> ld-cli -h
Usage: ld-cli [-hv] [COMMAND]

Linked Data Command Line Processor

Options:
  -h, --help      display help message
  -v, --version   display a version

Commands:
  expand      Expand JSON-LD document
  compact     Compact JSON-LD document using the context
  flatten     Flatten JSON-LD document and optionally compact using a context
  frame       Frame JSON-LD document using the frame
  fromrdf     Transform N-Quads document into a JSON-LD document in an expanded form
  tordf       Transform JSON-LD document into N-Quads document
  compress    Compress JSON-LD document into CBOR-LD  
  decompress  Decompress CBOR-LD document into JSON-LD
  
> ld-cli expand -h
Usage: ld-cli expand [-op] [-b=<base>] [-c=<context>] [-i=<input>] [-m=1.0|1.1]

Expand JSON-LD document

Options:
  -b, --base=<base>         input document base IRI
  -c, --context=<context>   context IRI
  -i, --input=<input>       input document IRI
  -m, --mode=1.0|1.1        processing mode
  -o, --ordered             certain algorithm processing steps are ordered
                              lexicographically
  -p, --pretty              pretty print output JSON

```

### Pipeline
```bash
> cat document.json | ld-cli expand --ordered --pretty > expanded.jsonld
```

### `file:/` scheme support

```bash
> ld-cli compress -i file:/home/filip/example.jsonld
```

### Custom CBOR-LD dictionaries
```bash
> ld-cli decompress --pretty --dictionary=./utopia-barcodes-dictionary-example.json <<< 'd90664a60183198000198001198002189d82187618a418b8a3189c18a618ce18b218d01ae592208118baa2189c18a018a8447582002018be18aa18c0a5189c186c18d60418e018e618e258417ab7c2e56b49e2cce62184ce26818e15a8b173164401b5d3bb93ffd6d2b5eb8f6ac0971502ae3dd49d17ec66528164034c912685b8111bc04cdc9ec13dbadd91cc18e418ac'
```

## Contributing

All PR's welcome!

### Building

1. [Install GraalVM and Native Image](https://www.graalvm.org/latest/docs/)
   - download and unpack ```graalvm-jdk-....tar.gz```
   - set ```JAVA_HOME``` and ```PATH``` env variables
3. ```mvn clean package -Pnative```
4. ```./target/ld-cli```


## Resources

* [Titanium JSON-LD 1.1 Processor](https://github.com/filip26/titanium-json-ld)
* [Iridium CBOR-LD Processor](https://github.com/filip26/iridium-cbor-ld)
* [LEXREX](https://lexrex.web.app/) - Semantic vocabularies visual builder and manager

## Sponsors

<a href="https://github.com/thadguidry">
  <img src="https://avatars.githubusercontent.com/u/986438?v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com

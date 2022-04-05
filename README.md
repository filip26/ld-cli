# JSON-LD 1.1 CLI

A simple command line utility allowing to process JSON-LD 1.1 documents. The goal is to provide native executables  compiled for Ubuntu, Mac and Windows.

WORK IN PROGRESS - FEEDBACK IS WELCOME

# Installation

[Download](https://github.com/filip26/json-ld-cli/actions/runs/2097834570)

```bash
$ unzip json-ld-cli-....zip
$ chmod +x jsonld
```

# Usage

```bash
$ ./jsonld -h
Usage: jsonld [-hv] [COMMAND]

JSON-LD 1.1 Command Line Processor

Options:
  -h, --help      display help message
  -v, --version   display a version

Commands:
  expand  Expands JSON-LD document
  tordf   Transforms JSON-LD document into RDF dataset
```

## Expansion

```bash
$ ./jsonld expand -h
Usage: jsonld expand [-op] [-b=<base>] [-c=<context>] [-m=1.0|1.1] [<input>]

Expand JSON-LD 1.1 document

Parameters:
      [<input>]             input URL

Options:
  -p, --pretty              pretty print output JSON
  -c, --context=<context>   context URL
  -b, --base=<base>         base URL
  -m, --mode=1.0|1.1        processing mode
  -o, --ordered             certain algorithm processing steps are ordered
                              lexicographically
```

## To RDF

```bash
$ ./jsonld tordf -h
Usage: jsonld tordf [-go] [-b=<base>] [-c=<context>]
                    [-d=I18N_DATATYPE|COMPOUND_LITERAL] [-m=1.0|1.1] [<input>]

Transforms JSON-LD document into RDF dataset

Parameters:
      [<input>]             input URL

Options:
  -b, --base=<base>         base URL
  -c, --context=<context>   context URL
  -d, --direction=I18N_DATATYPE|COMPOUND_LITERAL
                            determines how value objects containing a base
                              direction are transformed
  -g, --generalized         emit blank nodes for triple predicates
  -m, --mode=1.0|1.1        processing mode
  -o, --ordered             certain algorithm processing steps are ordered
                              lexicographically

```

# Building

1. [Install GraalVM and Native Image](https://www.graalvm.org/java/quickstart/)
   - download and unpack ```graalvm-ce-java11-[platform]-[version].tar.gz```
   - set ```JAVA_HOME``` and ```PATH``` env variables
   - ```gu install native-image```
3. ```mvn clean package -P native-image```
4. ```./target/jsonld```

# Sponsors

<a href="https://github.com/thadguidry">
  <img src="https://avatars.githubusercontent.com/u/986438?v=4" width="25" />
</a> 

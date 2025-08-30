# Linked Data CLI

A simple command-line utility designed to process JSON-LD, RDF, CBOR-LD, and multiformats documents. 
    
Supports batch workflows, canonicalization, serialization, encoding, decoding, and format conversion for linked data resources, binary identifiers, and content addressing formats in knowledge graphs, decentralized identifiers (DIDs), and semantic web applications.

Built with GraalVM, `ld-cli` delivers native executables for Ubuntu, macOS, and Windows - eliminating JVM dependencies.

[![Snap Status](https://snapcraft.io/ld-cli/badge.svg)](https://snapcraft.io/ld-cli)
[![Downloads](https://img.shields.io/github/downloads/filip26/ld-cli/total)](https://github.com/filip26/ld-cli/releases)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/filip26/ld-cli?include_prereleases)](https://github.com/filip26/ld-cli/releases)

## ✨ Features

* [W3C JSON-LD 1.1](https://www.w3.org/TR/json-ld/) 
* [W3C CBOR-LD 1.0](https://json-ld.github.io/cbor-ld-spec/)
* [W3C Standard RDF Dataset Canonicalization Algorithm](https://www.w3.org/TR/rdf-canon/)
* [RFC 8785 JSON Canonicalization Scheme (JCS)](https://www.rfc-editor.org/rfc/rfc8785)
* [W3C CCG Multibase - Base-encoding format with self-describing prefixes](https://github.com/w3c-ccg/multibase)
* [Multicodec - Self-describing content type identifiers](https://github.com/multiformats/multicodec)
* [Multihash](https://github.com/multiformats/multihash)

## Installation

### 📦 Install via Snap

You can install this app easily on any Linux distribution that supports [Snaps](https://snapcraft.io/docs).

```bash
sudo snap install ld-cli
```

### 📁 Manual Download

Download the latest release from the [GitHub Releases page](https://github.com/filip26/ld-cli/releases/). Click Assets to see the available downloads.

After downloading, extract the archive and make the binary executable:

```bash
unzip ld-cli-....zip
chmod +x ld-cli
```

## Usage

```bash
> ld-cli -h
Usage: ld-cli [-hv] [COMMAND]

Linked Data Command Line Processor

Options:
  -h, --help      Display help message.
  -v, --version   Display version information.

Commands:
  expand      Expand a JSON-LD document.
  compact     Compact a JSON-LD document using the provided context.
  flatten     Flatten a JSON-LD document and optionally compact it using a
                context.
  frame       Frame a JSON-LD document using the provided frame.
  fromrdf     Transform an N-Quads document into a JSON-LD document in expanded
                form.
  tordf       Transform a JSON-LD document into an RDF N-Quads document.
  compress    Compress JSON-LD document into CBOR-LD.
  decompress  Decompress CBOR-LD document into JSON-LD.
  rdfc        Canonize an RDF N-Quads document using the RDFC-1.0 algorithm.
  jcs         Canonize a JSON document using the JSON Canonicalization Scheme
                (JCS).
  multibase   Encode, decode, detect, or list multibase encodings.
  multicodec  Add, remove, detect, or list multicodec headers.

> ld-cli expand -h
Usage: ld-cli expand [-op] [--debug] [-b=<uri>] [-c=<uri|file>] [-i=<uri|file>]
                     [-m=1.0|1.1]

Expand a JSON-LD document.

Options:
  -b, --base=<uri>         Base URI of the input document.
  -c, --context=<uri|file> Context URI or file path.
      --debug              Print detailed error information.
  -i, --input=<uri|file>   Input document URI or file path.
  -m, --mode=1.0|1.1       Processing mode.
  -o, --ordered            Order certain algorithm steps lexicographically.
  -p, --pretty             Pretty-print the output JSON.

```

### Pipeline
```bash
cat document.json | ld-cli expand --ordered --pretty > expanded.jsonld
```

### `https://` scheme support

```bash
ld-cli rdfc -i https://raw.githubusercontent.com/filip26/titanium-rdf-canon/refs/heads/main/src/test/resources/com/apicatalog/rdf/canon/rdfc10/test022-in.nq 
```

### `file:/` scheme support

```bash
ld-cli compress -i file:/home/filip/example.jsonld
```

### Custom CBOR-LD dictionaries
```bash
ld-cli decompress --pretty --hex --dictionary ./utopia-barcodes-dictionary-example.json <<< 'd90664a60183198000198001198002189d82187618a418b8a3189c18a618ce18b218d01ae592208118baa2189c18a018a8447582002018be18aa18c0a5189c186c18d60418e018e618e258417ab7c2e56b49e2cce62184ce26818e15a8b173164401b5d3bb93ffd6d2b5eb8f6ac0971502ae3dd49d17ec66528164034c912685b8111bc04cdc9ec13dbadd91cc18e418ac'
```

### Multicodec
```bash
ld-cli multicodec --analyze --multibase <<< 'z6MkmM42vxfqZQsv4ehtTjFFxQ4sQKS2w6WR7emozFAn5cxu'
```
```bash
Multibase:  name=base58btc, prefix=z, length=58 chars
Multicodec: name=ed25519-pub, code=237, varint=[0xED,0x01], tag=Key, status=Draft
Length:     32 bytes
```

### Multihash
```bash
ld-cli multicodec --analyze --multibase <<< 'MEiCcvAfD+ZFyWDajqipYHKICkZiqQgudmbwOEx2fPiy+Rw=='
```
```bash
Multibase:  name=base64pad, prefix=M, length=64 chars
Multihash:  name=sha2-256, code=18, varint=[0x12], tag=Multihash, status=Permanent
Length:     32 bytes
```

## Contributing

All PR's welcome!

### Building

1. [Install GraalVM and Native Image](https://www.graalvm.org/latest/docs/)
   - download and unpack ```graalvm-jdk-....tar.gz```
   - set ```GRAALVM_HOME``` env variable
3. ```mvn package -Pnative```
4. ```./target/ld-cli```


## Resources

* [Titanium JSON-LD](https://github.com/filip26/titanium-json-ld)
* [Titanium RDFC](https://github.com/filip26/titanium-rfc-canon)
* [Titanium JCS](https://github.com/filip26/titanium-jcs)
* [Iridium CBOR-LD](https://github.com/filip26/iridium-cbor-ld)
* [Copper Multibase](https://github.com/filip26/copper-multibase)
* [Copper Multicodec](https://github.com/filip26/copper-multicodec)

## Sponsors

<a href="https://github.com/thadguidry">
  <img src="https://avatars.githubusercontent.com/u/986438?v=4" width="40" />
</a> 

## Commercial Support

Commercial support and consulting are available.  
For inquiries, please contact: filip26@gmail.com

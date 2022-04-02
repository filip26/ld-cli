# JSON-LD 1.1 CLI

A simple command line utility allowing to process JSON-LD 1.1 documents. The goal is to provide native executables  compiled for Ubuntu, Mac and Windows.

WORK IN PROGRESS - FEEDBACK IS WELCOME

[Downloads](https://github.com/filip26/json-ld-cli/actions/runs/2083267593)

# Usage

```bash
$ unzip json-ld-cli-....zip
$ chmod +x jsonld
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

$ ./jsonld  expand -op https://raw.githubusercontent.com/filip26/titanium-json-ld/main/src/test/resources/com/apicatalog/jsonld/test/issue112-in.json

```

# Sponsors

<a href="https://github.com/thadguidry">
  <img src="https://avatars.githubusercontent.com/u/986438?v=4" width="25" />
</a> 

# Konaste API

---

A shim between Sound Voltex Konaste internals and the outside world,
Konaste API introduces an opportunity to access live data from the game
itself, at any time.

---

## How to use
The latest release should always be available from https://konaste.bauxe.dev/konaste-api-latest.zip.

Alternatively, you can use a 7-character short hash of a commit to download a specific release
(for example, konaste-api-728470b.zip in place of konaste-api-latest.zip).

Simply download, extract and run the executable! Note that the download is quite
large (100MB) as it comes packaged with a JVM, lightweight releases will be available
in the future if you already have Java installed on your system. Additionally, you
may need to run the program as an administrator, depending on your system's security
settings.

To verify the program is running correctly, navigate to http://localhost:4573/ and you should
be presented with a page displaying `Hello!`. Navigating to http://localhost:4573/version 
will display which version of Sound Voltex Konaste has been identified as running.

---

## Capabilities
This program is not intended to be used in isolation. It instead provides a bridge
between Sound Voltex Konaste and other programs.

If you are a **user**, here is a non-exhaustive list of programs that make use of
this API:
#### konaste-obs-sources
Hosted at https://konaste.bauxe.dev/
Repository at https://github.com/konaste-tools/konaste-obs-sources

This program provides a number of data visualisations from the game intended
to be used while streaming or recording with OBS.

_If you have created a program that depends on this API, feel free to raise a pull
request to add to this list :)_

If you are instead a **developer**, you can access the program's OpenAPI spec
at http://localhost:4573/swagger/index.html - note that WebSocket supported endpoints
are prefixed with `/ws` if consuming their WebSocket variant.

---

## Contributing

There are a number of ways to contribute, both as a developer or as a user!

Create a suggestion in the [issues](https://github.com/konaste-tools/konaste-versions/issues) tab.

Add offsets for new game versions or new functionality in the [konaste-versions](https://github.com/konaste-tools/konaste-versions) repository.

Write some new functionality, or tests for existing functionality!

Or just use the program :)
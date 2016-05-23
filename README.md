IMCKTG is an Android application for generating Morse code ringtones.


Instructions for Installation:
==============================

Pre-built Android Binaries
--------------------------
Pre-built binaries are likely available from
[F-Droid](https://f-droid.org/repository/browse/?fdid=us.achromaticmetaphor.imcktg)
and may be available from other application repositories.

Building Android Binaries
-------------------------
To build IMCKTG from source using an Android SDK, execute:

    android update project -p . -n IMCKTG
    ant debug

This will produce bin/IMCKTG-debug.apk.

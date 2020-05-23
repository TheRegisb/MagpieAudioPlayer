# Magpie Audio Player
Smart and adaptive multi-agents audio player.

## Foreword
This is a foremost a school project: a multi-agents system first and an audio player second.
I do acknowledge many of the shortcoming of the project, such as the limited tag dictionary, 
single shot configuration rules, use of string as trusted information an so on.

I do however believe this is a mildly albeit specific case study for cooperative and mediating multi-agent system.
This application will most likely remain “as-is” for the foreseeable future since it is, after all,
another school project.

One I did enjoy writing.

## Requirement
- JRE 11 or greater for running the application
- JDK 11 or greater and Maven for compiling the application

## Compilation
IntelliJ users can use the project file to restore the entire workspace.

For Maven, you can use the following command line:
```cmd
mvn clean package
```
This will generate two JAR files: `magpie-<version>` and `magie-<version>-with-dependencies`.
The later is a standalone executable while the former still require maven to start.

## Usage
```cmd
java -jar (source) (adapter) magpie.jar
```
Both the standalone and the PlayerAgent take two optional argument:
 0. `source`: The remote media source (default: `"audiosample.sqlite.db"`)
 1. `adapter`: The source adapter (default: `"local"`)
 
The arguments must be given in order. A missing argument is replaced
with its default value.

Double clicking the JAR will start the program with its default values.

In order to listen to music, and given the current constraints, music files and a database must be provided in the same 
folder as the application.

A complete working example including a binary release, audio files and their dedicated database is available [here](
https://mega.nz/file/AA8BSCJC#0Xddb0gbVK5sS616muRhiNyDAejI0XfXloJQW2EYQOQ).
Decompress all files into the same folder and execute the JAR.

## License
Every files in this repository is licensed under the Apache License 2.0.

## Credits
Régis BERTHELOT, 2020, as part of his Master Degree at the West University of Timisoara.

## Misc
The development environment was:
- Windows 10 x64
- Oracle JDK 14
- IntelliJ 2019.2.2.

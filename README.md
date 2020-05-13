# Description
A slightly modified version of [Dukat](https://github.com/kotlin/dukat) by JetBrains that, for the time being, works better for generating [three.js](https://github.com/mrdoob/three.js/) external declarations.

# Disclaimer
This is meant for my personal use, but sharing for reference in case you want to jump in and modify Dukat to suit your usecase like I did.

If you simply want to start building Kotlin/JS with three.js, you can check my [kotlin-three-js-starter](https://github.com/liorgonnen/kotlin-three-js-starter) repo

# How to install
If you want to run this locally, and also easily test your changes:
```shell script
./gradlew build

cd node-package/build/distrib

# Instead of re-installing every time we build, we simply create a symlink to our local npm package
npm link node-package/build/distrib

# Create a symbolic link between the executed .jar in the local npm package, and the .jar
# created when running gradle's command-line/build task
ln -s command-line/build/libs/dukat-cli.jar /node-package/build/distrib/build/runtime/dukat-cli.jar
```

# Usage
```shell
dukat [<options>] <d.ts files>
```

where possible options include:
```shell
    -p  <qualifiedPackageName>      package name for the generated file (by default filename.d.ts renamed to filename.d.kt)
    -q  String                      Add a JS qualifier at the top of the generated file                       
    -m  String                      use this value as @file:JsModule annotation value whenever such annotation occurs
    -d  <path>                      destination directory for files with converted declarations (by default declarations are generated in current directory)
    -v, -version                    print version
```
(Note that the `-q` option doesn't exist in the original Dukat)

You'll need my [three.js.kt](https://github.com/liorgonnen/three.js.kt) repo. It's a fork of three.js with slight modifications to the `d.ts` files.
Ideally all the fixes would have been done here. But that's the current state of things.


```shell script
# Clone the three.js.kt repo
git clone https://github.com/liorgonnen/three.js.kt

# Create a folder to put the generated files in
mkdir gen
```

Generate the external declarations:
```shell script
# In case you have previously generated files:
rm -rf gen/*

# Genetate the Kotlin externals
# -p adds a 'package three.js' declaration to each generated file
# -q adds a 'file:JsQualifier("THREE")' declaration to each generated file
# -d puts the generated files in the 'gen' folder
dukat -p three.js -q THREE -d ./gen three.js.kt/src/Three.d.ts
```

When copying the files to your target project, please only take *`.module_three.kt`* files: (We don't need the `.module_dukat.kt` files), like so:
```shell script
# Modify the target folder according to your needs:
find ./gen -iname "*.module_three.kt" -exec cp {} ~/projects/kotlin-three-js-starter/threejs_kt/src/main/kotlin/three/ \;
```

See my [kotlin-three-js-starter](https://github.com/liorgonnen/kotlin-three-js-starter) repo for a fully working example.

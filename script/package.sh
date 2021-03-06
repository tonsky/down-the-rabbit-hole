#! /bin/bash
set -o errexit -o nounset -o pipefail
cd "`dirname $0`/.."

rm -rf target
mkdir target

cp /Users/prokopov/.m2/repository/datascript/datascript/1.1.0/datascript-1.1.0.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/clojure/1.10.3/clojure-1.10.3.jar target/
cp /Users/prokopov/.m2/repository/org/jetbrains/skija/skija-macos-x64/0.90.16/skija-macos-x64-0.90.16.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl/3.2.3/lwjgl-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl/3.2.3/lwjgl-3.2.3-natives-macos.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-glfw/3.2.3/lwjgl-glfw-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-glfw/3.2.3/lwjgl-glfw-3.2.3-natives-macos.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-opengl/3.2.3/lwjgl-opengl-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-opengl/3.2.3/lwjgl-opengl-3.2.3-natives-macos.jar target/
cp /Users/prokopov/.m2/repository/persistent-sorted-set/persistent-sorted-set/0.1.2/persistent-sorted-set-0.1.2.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/core.specs.alpha/0.2.56/core.specs.alpha-0.2.56.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/spec.alpha/0.2.194/spec.alpha-0.2.194.jar target/
cp /Users/prokopov/.m2/repository/org/jetbrains/skija/skija-shared/0.90.16/skija-shared-0.90.16.jar target/

pushd src
zip -q -r ../target/down_the_rabbit_hole.jar *
popd

rm -rf "Down the Rabbit Hole.app"
jpackage --input target/ --name "Down the Rabbit Hole" --main-jar clojure-1.10.3.jar --main-class clojure.main --type dmg --java-options '-XstartOnFirstThread' --arguments '-m down-the-rabbit-hole.main' --icon resources/icon.icns
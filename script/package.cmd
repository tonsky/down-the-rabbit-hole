rm -rf target
mkdir target

cp /Users/prokopov/.m2/repository/datascript/datascript/1.1.0/datascript-1.1.0.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/clojure/1.10.3/clojure-1.10.3.jar target/
cp /Users/prokopov/.m2/repository/org/jetbrains/skija/skija-windows/0.90.16/skija-windows-0.90.16.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl/3.2.3/lwjgl-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl/3.2.3/lwjgl-3.2.3-natives-windows.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-glfw/3.2.3/lwjgl-glfw-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-glfw/3.2.3/lwjgl-glfw-3.2.3-natives-windows.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-opengl/3.2.3/lwjgl-opengl-3.2.3.jar target/
cp /Users/prokopov/.m2/repository/org/lwjgl/lwjgl-opengl/3.2.3/lwjgl-opengl-3.2.3-natives-windows.jar target/
cp /Users/prokopov/.m2/repository/persistent-sorted-set/persistent-sorted-set/0.1.2/persistent-sorted-set-0.1.2.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/core.specs.alpha/0.2.56/core.specs.alpha-0.2.56.jar target/
cp /Users/prokopov/.m2/repository/org/clojure/spec.alpha/0.2.194/spec.alpha-0.2.194.jar target/
cp /Users/prokopov/.m2/repository/org/jetbrains/skija/skija-shared/0.90.16/skija-shared-0.90.16.jar target/

pushd src
zip -q -r ../target/down_the_rabbit_hole.jar *
popd

jpackage --input target/ --name "Down the Rabbit Hole" --main-jar clojure-1.10.3.jar --main-class clojure.main --type msi --arguments "-m down-the-rabbit-hole.main" --icon resources/icon.ico --win-menu --win-shortcut --win-dir-chooser
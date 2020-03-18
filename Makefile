server-dev:
	vertx run src/main/java/veebrate/App.java --redeploy="**/*.java" --launcher-class=io.vertx.core.Launcher

server-build:
	npm --prefix src/main/js/veebrate-spa run build
	sassc --style compressed src/main/scss/main.scss > src/main/resources/webroot/css/main.css
	./gradlew build

sass-dev:
	sassc src/main/scss/main.scss > src/main/resources/webroot/css/main.css

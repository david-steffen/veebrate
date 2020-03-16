server-dev:
	./gradlew runShadow -t

server-build:
	npm --prefix src/main/js/veebrate-spa run build
	sassc --style compressed src/main/scss/main.scss > src/main/resources/webroot/css/main.css
	./gradlew build

sass-dev:
	sassc src/main/scss/main.scss > src/main/resources/webroot/css/main.css

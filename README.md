# Veebrate

A little fun project to play around with websockets and the html5 vibrate api.
Written with Vert.x 3 on the server using SockJS and ractive.js on the frontend

## Requirements

* Java JDK 8
* Maven

## Installation

```
git clone https://github.com/david-steffen/veebrate.git
cd veebrate
mvn clean compile package
```
Vert.x will be compiled and packaged as a *fat jar* file so all you need to run it is
```
java -jar target/veebrate-fat.jar -conf src/conf/conf.json
```

Then, open a browser to http://localhost:8080.

### Frontend

Webpack is required if you want to play around with the frontend. Just do
```
cd src/main/resources
npm install
```
then run webpack
```
node_modules/webpack/bin/webpack.js  --progess --color --watch
```
or
```
webpack  --progess --color --watch
```
if you installed webpack globally
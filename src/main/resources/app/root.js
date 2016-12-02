module.exports = (function() {
    'use strict';
    var Ractive = require('ractive');
    var template = require("ractive!./views/root.html");
    var headerComponent = require("./components/header");
    var EventBus = require('vertx3-eventbus-client');
    require('ractive-touch');
    require('../scss/main.scss');

    function Root(root) {
        let eb;
        navigator.vibrate = navigator.vibrate || navigator.webkitVibrate || navigator.mozVibrate || navigator.msVibrate;
        Ractive.DEBUG = false;
        const view = new Ractive({
            el: root,
            template: template,
            magic: true,
            data: {
                vibrate: {
                    vibrate: false,
                    speed: 500,
                    recipient: "",
                    sender: "",
                },
                signupOpen: false,
                menuOpen: false,
                user: "",
                conn: false,
                sending: false,
                selectUser: false,
            },
            events: {
                tap: require( 'ractive-events-tap' )
            },
            components: {
                Header: headerComponent(),
            },
            oninit: function () {
                this.on({
                    handleInput: function(event) {
                        this.set(event.original.target.name, event.original.target.value);
                    },
                    hideUserForm: function(event) {
                        if(this.get("user")) {
                            this.set("sender",this.get("user"));
                            this.toggle("selectUser");
                            eb = this.connect();
                        }
                    },
                    startVibrate: function(event) {
                        var  model = this.get("vibrate");
                        model.vibrate = true;
                        model.sender = this.get("user");
                        this.set("sending",true);
                        this.sendMessage(model);
                    },
                    endVibrate: function(event) {
                        var model = this.get("vibrate");
                        model.vibrate = false;
                        model.sender = "";
                        this.set("sending",false);
                        this.sendMessage(model);
                    },
                    selectSpeed: function(event) {
                        var  model = this.get("vibrate");
                        model.speed = event.node.value;
                        this.sendMessage(model);
                    },
                    changeUser: function(event) {
                        this.set("vibrate.recipient",event.node.value);
                    },
                    connect: function(event) {
                        eb = this.connect();
                    },
                });
            },
            connect: function() {
                const eventbusUrl = "http://"+window.location.hostname+':8080/eventbus';
                let eb = new EventBus(eventbusUrl);

                eb.onopen = function() {
                    this.set("conn", true);
                    eb.registerHandler('chat.messageOut', function(error, msg) {
                        this.set({"vibrate.vibrate": msg.body.vibrate,
                            "vibrate.speed": msg.body.speed,
                            "vibrate.sender": msg.body.sender});
                        this.msgHandler();
                    }.bind(this));
                    eb.send("chat.user",{ user: this.get("user") });
                }.bind(this);
                eb.onclose = function() {
                    this.set("conn", false);
                    eb.unregisterHandler('chat.messageOut');
                    eb.unregisterHandler('chat.user');
                }.bind(this);
                return eb;
            },
            vibrate: function(speed) {
                if (navigator.vibrate) {
                    navigator.vibrate(speed);
                }
            },
            sendMessage: function(msg) {
                eb.send("chat.messageIn",msg);
            },
            msgHandler: function() {
                var model = this.get("vibrate");
                if(!model.vibrate) return;
                this.vibrate(model.speed);
                var start = null;
                var step = function(timestamp) {
                    if(!model.vibrate) {
                        this.vibrate(0);
                        return;
                    }
                    if (!start) start = timestamp;
                    var progress = timestamp - start;
                    if (progress <= model.speed) {
                        window.requestAnimationFrame(step);
                    } else {
                        start = null;
                        this.vibrate(model.speed);
                        window.requestAnimationFrame(step);
                    }
                }.bind(this);
                window.requestAnimationFrame(step);
            }
        });

        return view;
    }
    return Root;

})();
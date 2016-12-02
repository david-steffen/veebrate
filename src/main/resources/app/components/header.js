module.exports = (function() {
    'use strict';

    var Ractive = require('ractive');
    var template = require("html!./../views/components/header.html");

    function HeaderComponent(root) {
        var component = Ractive.extend({
            template: template,
            oninit: function() {
                this.on('toggleMenu',function(event) {
                    this.toggle("menuOpen");

                    if (this.get("signupOpen")) {
                        this.set({signupOpen:false});
                    }
                });
                this.on('toggleSignup',function(event) {
                    console.log("signup")
                    this.toggle("signupOpen");
                    this.set("menuOpen", false);
                });
            }
        });
        return component;
    }

    return HeaderComponent;
})();
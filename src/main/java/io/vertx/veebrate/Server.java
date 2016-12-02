package io.vertx.veebrate;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.*;

import java.util.HashMap;

public class Server extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {

        Router router = Router.router(vertx);
        router.route("/eventbus/*").handler(eventBusHandler());
        router.route().handler(BodyHandler.create());

        EventBus eb = vertx.eventBus();
        // Register to listen for messages coming IN to the server
        eb.consumer("chat.messageIn").handler(message -> {
            eb.publish("chat.messageOut", message.body());
        });


        String webroot ="webroot";
//        String webroot ="src/main/resources/webroot";
        StaticHandler staticHandler = StaticHandler.create(webroot);

        router.route().handler(staticHandler);

        vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("port"), config().getString("domain"), result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private SockJSHandler eventBusHandler() {
        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        HashMap<String,String> users = new HashMap<>();

        BridgeOptions bridgeOptions = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddress("chat.messageIn"))
            .addInboundPermitted(new PermittedOptions().setAddress("chat.user"))
            .addOutboundPermitted(new PermittedOptions().setAddress("chat.messageOut"));

        return SockJSHandler.create(vertx,options).bridge(bridgeOptions,be -> {
            if (be.type() == BridgeEventType.SEND && be.getRawMessage().getString("address").equals("chat.user")) {
                String sender = be.getRawMessage().getJsonObject("body").getString("user");
                users.put(sender, be.socket().writeHandlerID());
                be.complete(true);
            } else if (be.type() == BridgeEventType.RECEIVE && be.getRawMessage().getString("address").equals("chat.messageOut")) {
                String recipient = be.getRawMessage().getJsonObject("body").getString("recipient");
                if (be.socket().writeHandlerID().equals(users.get(recipient))) {
                    be.complete(true);
                } else {
                    be.complete(false);
                }
            } else {
                be.complete(true);
            }
        });
    }
}

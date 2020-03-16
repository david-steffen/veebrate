package veebrate;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.bridge.PermittedOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class App extends AbstractVerticle {

    private Map<String,User> connections;

    public static void main(String[] args) {
        Launcher.executeCommand("run", App.class.getName());
    }

    @Override
    public void start() {

        Router router = Router.router(vertx);
        SockJSHandlerOptions options = new SockJSHandlerOptions();
        options.setHeartbeatInterval(20000);
        options.setSessionTimeout(60000);

        connections = new HashMap<>();
        
        EventBus eb = vertx.eventBus();

        eb.consumer("user.connect").handler(message -> {
            eb.publish("user.connected", message.body());
        })
        .exceptionHandler(error -> {
            System.out.println(error.getMessage());
        });
        // Register to listen for messages coming IN to the server
        eb.consumer("user.messageIn").handler(message -> {
            eb.publish("user.messageOut", message.body());
        })
        .exceptionHandler(error -> {
            System.out.println(error.getMessage());
        });

        BridgeOptions bridgeOptions = new BridgeOptions()
        .addInboundPermitted(new PermittedOptions().setAddress("user.messageIn"))
        .addInboundPermitted(new PermittedOptions().setAddress("user.connect"))
        .addOutboundPermitted(new PermittedOptions().setAddress("user.messageOut"))
        .addOutboundPermitted(new PermittedOptions().setAddress("user.connected"));

        Handler<BridgeEvent> bridgeEventHandler = be -> {
            if (be.type() == BridgeEventType.SEND && be.getRawMessage().getString("address").equals("user.connect")) {
                JsonObject body = be.getRawMessage().getJsonObject("body");
                String connectionID = be.socket().writeHandlerID();
                User user = new User();
                user.setConnectionID(connectionID);
                user.setUserName(body.getString("username"));
                user.setCanVibrate(body.getBoolean("canVibrate"));
                connections.put(connectionID, user);
            } else if (be.type() == BridgeEventType.RECEIVE && be.getRawMessage().getString("address").equals("user.connected")) {
                JsonArray users = new JsonArray();
                Collection<User> collectionConns = connections.values();
                for (User user_ : collectionConns) {
                    users.add(user_.toJson());
                }
                JsonObject payload = new JsonObject();
                payload.put("connectionID", be.socket().writeHandlerID());
                payload.put("users", users);
                JsonObject response = be.getRawMessage();
                response.put("body", payload);
                be.setRawMessage(response);
            } else if (be.type() == BridgeEventType.RECEIVE && be.getRawMessage().getString("address").equals("user.messageOut")) {
                String recipient = be.getRawMessage().getJsonObject("body").getString("recipientID");
                if (!be.socket().writeHandlerID().equals(connections.get(recipient).getConnectionID())) {
                    be.complete(false);
                    return;
                }
            } else if (be.type() == BridgeEventType.SOCKET_CLOSED) {
                String connectionID = be.socket().writeHandlerID();
                connections.remove(connectionID);
                eb.publish("user.connected", null);
            }
            be.complete(true);
        };
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        router.mountSubRouter("/eventbus", sockJSHandler.bridge(bridgeOptions, bridgeEventHandler));

        router.route().handler(BodyHandler.create());
        StaticHandler staticHandler = StaticHandler.create("webroot");

        router.route().handler(staticHandler);
        int port = Integer.getInteger("http.port", 8080);
        String httpAddress = System.getProperty("http.address", "127.0.0.1");
        vertx.createHttpServer()
        .requestHandler(router)
        .listen(port, httpAddress, handler -> {
            if (handler.succeeded()) {
                System.out.println("App now running at '" + httpAddress + ":" + port + "'");
            } else {
                System.err.println("App failed");
            }
        });
    }

    private class User {
        private String connectionID;
        private String username;
        private boolean canVibrate;

        public User(){
        }
        public String getConnectionID() {
            return connectionID;
        }
        public void setConnectionID(String id) {
            this.connectionID = id;
        }
        public String getUserName() {
            return username;
        }
        public void setUserName(String name) {
            this.username = name;
        }
        public boolean getCanVibrate() {
            return canVibrate;
        }
        public void setCanVibrate(boolean canVibrate) {
            this.canVibrate = canVibrate;
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("connectionID", connectionID);
            jsonObject.put("username", username);
            jsonObject.put("canVibrate", canVibrate);
            return jsonObject;
        }
    }
}

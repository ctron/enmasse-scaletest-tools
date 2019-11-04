package net.trystram.scaletest.http.DeviceCreater;

import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(
            Application.class);

    public static void main(String args[]) {

        if (args.length < 1){
            log.error("The path to the config file must be given as the 1st argument.");
            System.exit(1);
        }

        Creater app = new Creater(args[0]);

        Future<Void> startPromise = app.configure().compose(config -> {
                    Future<Void> runFuture = Future.future();
                    return app.run(runFuture);
                });

         startPromise.setHandler(res -> {
             if(res.succeeded()){
                     System.exit(0);
            } else {
                log.error("Failure ",res.cause());
                System.exit(1);
            }
        });
    }
}

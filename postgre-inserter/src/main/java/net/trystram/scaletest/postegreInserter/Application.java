package net.trystram.scaletest.postegreInserter;

public class Application {

    public static void main(String args[]) throws Exception {

        try (final Creater app = new Creater(Config.fromEnv())) {
            app.run();
        }

    }
}

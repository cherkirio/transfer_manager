import com.google.inject.Guice;
import com.google.inject.Injector;
import config.ConfigModule;
import server.Server;

/**
 * Created by kirio on 29.10.2018.
 */
public class Main {


    public static void main(String... args) {
        Injector injector = Guice.createInjector(new ConfigModule());
        Server server = new Server();
        injector.injectMembers(server);
        server.init();
    }
}

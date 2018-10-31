import com.google.inject.Guice;
import com.google.inject.Injector;
import endpoint.Config;
import endpoint.Server;

/**
 * Created by kirio on 29.10.2018.
 */
public class Main {


    public static void main(String... args) {
        Injector injector = Guice.createInjector(new Config());
        Server server = new Server();
        injector.injectMembers(server);
        server.init();
    }
}

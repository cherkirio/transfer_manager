package config;

import com.google.common.base.Joiner;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IAccountService;
import service.AccountService;
import java.util.Properties;

/**
 * Created by kirio on 31.10.2018.
 */
public class ConfigModule implements Module {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigModule.class);


    @Override
    public void configure(Binder binder) {
        binder.bind(IAccountService.class).to(AccountService.class);


        Names.bindProperties(binder, getProperties());


    }



    private Properties getProperties() {

        Properties props = new Properties();
        props.setProperty("server.port", "8080");

        LOG.info("Used properties:\n{}", Joiner.on("\n").join(props.entrySet()));
        return props;
    }


}

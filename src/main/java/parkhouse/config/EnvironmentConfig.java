package parkhouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

@Configuration
public class EnvironmentConfig {

    @Bean
    @Profile("local")
    public static PropertySourcesPlaceholderConfigurer localPropertiesConfigurer() throws IOException {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        Properties properties = new Properties();
        FileSystemResource resource = new FileSystemResource(".env");

        if (resource.exists()) {
            for (String line : Files.readAllLines(resource.getFile().toPath())) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    properties.put(parts[0].trim(), parts[1].trim());
                }
            }
        }

        configurer.setProperties(properties);
        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(false);

        return configurer;
    }

    @Bean
    @Profile({"test", "prod"})
    public static PropertySourcesPlaceholderConfigurer defaultPropertiesConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}

package parkhouse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ParkHouseApplicationTests {

    @Test
    @Disabled("Disabled -> the application context requires external secrets (ISSUER, keystore) that are not available in CI.")
    void contextLoads() {
        // This test normally loads the full Spring application context,
        // but it is disabled for CI because it depends on external secrets.
    }
}


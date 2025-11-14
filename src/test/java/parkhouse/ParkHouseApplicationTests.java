package parkhouse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ParkHouseApplicationTests {

    @Test
    @Disabled("Context requires external secrets (ISSUER, keystore); skipped in CI")
    void contextLoads() {
    }

}

package parkhouse;

import org.springframework.boot.SpringApplication;

public class TestParkHouseApplication {

    public static void main(String[] args) {
        SpringApplication.from(ParkHouseApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

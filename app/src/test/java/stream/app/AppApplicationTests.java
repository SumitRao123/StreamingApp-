package stream.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import stream.app.Service.VideoService;

@SpringBootTest
class AppApplicationTests {

	@Autowired
	VideoService videoService;

	@Test
	void contextLoads() {

	}

}

package com.back;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") // application-test.yaml(메모리 DB) 사용
@SpringBootTest
class BackApplicationTests {

	@Test
	void contextLoads() {
	}

}

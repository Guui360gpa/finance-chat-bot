package br.com.guilherme.financebot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.telegram.polling-enabled=false")
class FinanceChatBotApplicationTests {

	@Test
	void contextLoads() {
	}

}

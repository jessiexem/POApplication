package com.example.demo;

import com.example.demo.model.Quotation;
import com.example.demo.service.QuotationService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class DemoApplicationTests {

	@Autowired
	QuotationService quotationService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(quotationService);
	}

	@Test
	void invalidListShouldThrowException() {
		List<String> invalidItems = new ArrayList<String>(Arrays.asList("durian","plum","pear"));
		Optional<Quotation> opt = quotationService.getQuotations(invalidItems);
		Assertions.assertTrue(opt.isEmpty());
	}

	@Test
	void testControllerShouldThrowException() throws Exception {

		JsonObject durianItem = Json.createObjectBuilder()
				.add("item","durian")
				.add("quantity",1)
				.build();

		JsonObject plumItem = Json.createObjectBuilder()
				.add("item","plum")
				.add("quantity",1)
				.build();

		JsonObject pearItem = Json.createObjectBuilder()
				.add("item","pear")
				.add("quantity",1)
				.build();

		JsonObject payload = Json.createObjectBuilder()
				.add("name","Jessie")
				.add("address","Jurong")
				.add("email","j@hotmail.com")
				.add("lineItems",Json.createArrayBuilder()
						.add(durianItem)
						.add(plumItem)
						.add(pearItem)
						.build())
				.build();

		this.mockMvc.perform(MockMvcRequestBuilders.post("/api/po")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.valueOf(payload))
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest());

	}
}

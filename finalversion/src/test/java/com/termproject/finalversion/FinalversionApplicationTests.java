package com.termproject.finalversion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import static org.junit.Assert.*;

@SpringBootTest
class FinalversionApplicationTests {

	MainController mainContObj = new MainController();

	@Test
	public void testApiResponse() throws IOException, ParseException, IllegalArgumentException {
		URL url = new URL("https://api.jokes.one/joke/random");
		String result = mainContObj.getJoke(url);
		assertNotNull(result);
	}

	@Test
	public void testParserResponse() throws IOException, ParseException, IllegalArgumentException {
		URL url = new URL("https://api.jokes.one/joke/random");
		String result = "";
		String newString = mainContObj.getJoke(url);
		result = mainContObj.parseJokeString(newString);
		assertNotNull(result);
	}

}

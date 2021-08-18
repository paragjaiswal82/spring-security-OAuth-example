package com.example.demo.controller;

import java.security.Principal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class WebController {
	@Value("${spring.security.oauth2.client.registration.custom-client.client-secret}")
	private String clientSecret;
	
	@Value("${spring.security.oauth2.client.registration.custom-client.client-id}")
	private String clientId;
	
	@Value("${spring.security.oauth2.client.registration.custom-client.redirect-uri}")
	private String redirectUri;
	
	@Autowired
	private RestTemplate restTemplate;

    @GetMapping("/securedPage")
    public String securedPage() {
        return "securedPage";
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        return "index";
    }
    
    @GetMapping("/login")
    public void getAccessToken(@RequestParam String code) throws JsonMappingException, JsonProcessingException {
    	System.out.println("Authorization code - "+code);
    	ResponseEntity<String> response = null;
    	
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		HttpEntity<String> request = new HttpEntity<String>(headers);

		String access_token_url = "https://github.com/login/oauth/access_token";
		access_token_url += "?code="+code;
		access_token_url += "&client_id="+clientId;
		access_token_url += "&client_secret="+clientSecret;
		access_token_url += "&redirect_uri="+redirectUri;

		response = restTemplate.exchange(access_token_url, HttpMethod.POST, request, String.class);

		System.out.println("Access Token Response ---------" + response.getBody());

		// Get the Access Token From the recieved JSON response
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree(response.getBody());
				String token = node.path("access_token").asText();

				String url = "https://api.github.com/user";

				// Use the access token for authentication
				HttpHeaders headers1 = new HttpHeaders();
				headers1.add("Authorization", "Bearer " + token);
				HttpEntity<String> entity = new HttpEntity<>(headers1);

				ResponseEntity<String> str = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
				System.out.println(str);
    }
}

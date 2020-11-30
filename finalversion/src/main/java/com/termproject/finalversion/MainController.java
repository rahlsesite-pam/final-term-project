package com.termproject.finalversion;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.text.ParseException;
import java.io.File;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

@Controller
public class MainController {
	@Autowired
	private UserRepository userRepository;

	@Value("${accesskey}")
	String accesskey;
	@Value("${secretkey}")
	String secretkey;
	@Value("${bucketName}")
	String bucketName;

	@GetMapping(path = "/login")
	public ModelAndView showLogin() {
		return new ModelAndView("login");
	}

	public String parseJokeString(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			String content = json.getString("contents");
			JSONObject json3 = new JSONObject(content);
			String things2 = json3.getString("jokes");
			String b1 = things2.replace("[", "");
			String b2 = b1.replace("]", "");
			JSONObject json4 = new JSONObject(b2);
			String things3 = json4.getString("text");
			return things3;
		}

		catch (Exception e) {
			return "";
		}
	}

	public String getJoke(URL url) {
		try {
			// make connection
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod("GET");
			// set the content type
			urlc.setRequestProperty("Content-Type", "application/json");
			urlc.setRequestProperty("X-JokesOne-Api-Secret", "2p2g4kSJ8RbPCExQzg2SRweF");
			System.out.println("Connect to: " + url.toString());
			urlc.setAllowUserInteraction(false);
			urlc.connect();

			// get result
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String l = null;
			String newString = "";
			while ((l = br.readLine()) != null) {
				newString = l;
			}

			String joke = parseJokeString(newString);
			br.close();
			return joke;
		}

		catch (Exception e) {
			System.out.println("Error occured");
			System.out.println(e.toString());
			return "";
		}
	}

	@GetMapping(path = "/")
	public ModelAndView home(Model model) throws IOException, ParseException, IllegalArgumentException {
		URL url = new URL("https://api.jokes.one/joke/random");
		String joke = getJoke(url);
		model.addAttribute("joke", joke);
		return new ModelAndView("home");
	}

	@GetMapping(path = "/home")
	public ModelAndView home2(Model model) throws IOException, ParseException, IllegalArgumentException {
		URL url = new URL("https://api.jokes.one/joke/random");
		String joke = getJoke(url);
		model.addAttribute("joke", joke);
		return new ModelAndView("home");
	}

	@GetMapping(path = "/editprofile")
	public ModelAndView editprofile() {
		return new ModelAndView("editprofile");
	}

	@PostMapping(path = "/add2")
	public @ResponseBody ModelAndView loginanalyzer(@RequestParam String name, @RequestParam String password,
			HttpSession session, HttpServletResponse httpResponse, Model model) throws Exception {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it i a parameter from the GET or POST request
		User theUser = userRepository.findByName(name);
		String given_name = theUser.getName();
		String given_password = theUser.getPassword();
		String given_email = theUser.getEmail();

		if ((given_name.equals(name)) && (given_password.equals(password))) {

			String given_bio = theUser.getBio();
			model.addAttribute("name", given_name);
			model.addAttribute("bio", given_bio);
			model.addAttribute("email", given_email);
			String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + name + ".jpg";
			model.addAttribute("imgSrc", imgSrc);
			return new ModelAndView("profile");

		}

		else {
			return new ModelAndView("loginfailed");
		}

	}

	@PostMapping(path = "/add3")
	public @ResponseBody ModelAndView editprofile(@RequestParam String name, @RequestParam String newbio,
			HttpSession session, HttpServletResponse httpResponse, Model model) throws Exception {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it i a parameter from the GET or POST request

		try {
			User theUser = userRepository.findByName(name);
			String given_name = theUser.getName();
			String given_email = theUser.getEmail();

			if (given_name.equals(name)) {
				theUser.setBio(newbio);
				model.addAttribute("name", given_name);
				model.addAttribute("bio", newbio);
				model.addAttribute("email", given_email);
				String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + name + ".JPG";
				model.addAttribute("imgSrc", imgSrc);
				userRepository.save(theUser);
				return new ModelAndView("profile");
			}

			else {
				return new ModelAndView("bioupdatefailed");
			}
		}

		catch (Exception e) {
			return new ModelAndView("bioupdatefailed");
		}

	}

	@PostMapping(path = "/add") // Map ONLY POST Requests
	public @ResponseBody ModelAndView addNewUser(@RequestParam String name, @RequestParam String email,
			@RequestParam String bio, @RequestParam String password, @RequestParam("photo") MultipartFile image,
			@RequestParam(name = "desc") String desc) {

		User newUser = new User();
		newUser.setName(name);
		newUser.setEmail(email);
		newUser.setPassword(password);
		newUser.setBio(bio);
		System.out.println("description      " + desc);
		System.out.println(image.getOriginalFilename());

		BasicAWSCredentials cred = new BasicAWSCredentials(accesskey, secretkey);
		// AmazonS3Client client=AmazonS3ClientBuilder.standard().withCredentials(new
		// AWSCredentialsProvider(cred)).with
		AmazonS3 client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred))
				.withRegion(Regions.US_EAST_2).build();
		try {
			PutObjectRequest put = new PutObjectRequest(bucketName, name + ".jpg", image.getInputStream(),
					new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
			client.putObject(put);

			String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + name + ".jpg";

			// returnPage.setViewName("showImage");
			// returnPage.addObject("name", desc);
			// returnPage.addObject("imgSrc", imgSrc);

			// Save this in the DB.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// returnPage.setViewName("error");
		}
		userRepository.save(newUser);
		return new ModelAndView("login");
	}

	@GetMapping(path = "/all")
	public @ResponseBody Iterable<User> getAllUsers() {
		// This returns a JSON or XML with the users
		return userRepository.findAll();
	}

	@GetMapping(path = "/user")
	public @ResponseBody Optional<User> getOneUser(@RequestParam Integer id) {
		// This returns a JSON or XML with the users
		return userRepository.findById(id);

	}

	@GetMapping(path = "/userByName")
	public @ResponseBody User getOneUserByName(@RequestParam String name) {
		return userRepository.findByName(name);
	}

	@GetMapping(path = "/addUser")
	public ModelAndView showPage() {
		return new ModelAndView("registration");
	}

	@GetMapping(path = "/uploadImage")
	public ModelAndView editprofile2() {

		return new ModelAndView("uploadImage");
	}

	@PostMapping(value = "/add4")
	public ModelAndView uploads3(@RequestParam String name, Model model, @RequestParam("photo") MultipartFile image,
			@RequestParam(name = "desc") String desc) {
		ModelAndView returnPage = new ModelAndView();
		System.out.println("description " + desc);
		System.out.println(image.getOriginalFilename());

		BasicAWSCredentials cred = new BasicAWSCredentials(accesskey, secretkey);
		AmazonS3 client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred))
				.withRegion(Regions.US_EAST_2).build();
		try {

			PutObjectRequest put = new PutObjectRequest(bucketName, name + ".JPG", image.getInputStream(),
					new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
			client.putObject(put);

			String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + name + ".JPG";
			User theUser = userRepository.findByName(name);
			String given_name = theUser.getName();
			String given_email = theUser.getEmail();
			String given_bio = theUser.getEmail();
			model.addAttribute("name", given_name);
			model.addAttribute("bio", given_bio);
			model.addAttribute("email", given_email);
			model.addAttribute("imgSrc", imgSrc);
			return new ModelAndView("profile");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ModelAndView("login");

	}

}

package com.smart.controller;


import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import net.bytebuddy.utility.RandomString;


@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title" , "Home-smart Contact Manager");
		return "home";
	}
	
	
	
	

	@RequestMapping("/about")
	public String about(Model model)
	
	{
		model.addAttribute("title" , "About-smart Contact Manager");
		return "about";
	}
	

	@RequestMapping("/signup")
	public String signup(Model model)
	
	{
		model.addAttribute("title" , "Signup-smart Contact Manager");
		model.addAttribute("user" ,new User());
		return "signup";
	}
	//handler for registering user
	
     @RequestMapping(value = "/do_register", method = RequestMethod.POST)

	public String registerUser(@Valid @ModelAttribute("user") User user, 
			
			@RequestParam("profileImage") MultipartFile file 
			,BindingResult result1, @RequestParam(value = "agreement", defaultValue = "false")boolean agreement, Model model,
			
			HttpSession session) {
    	 model.addAttribute("title" , "Register Here");
    	try {
    		 if(!agreement) {
        		 System.out.println("You have not ageed the terms and conditions");
        		 throw new Exception("You have not ageed the terms and conditions");
        	 }
        	 
    		 if(result1.hasErrors()) {
    			 System.out.println("ERROR" + result1.toString());
    			 model.addAttribute("user",user);
    			 return "signup";
    		 }
    			//processing and uploading file
    			if (file.isEmpty()) {
    				System.out.println("file is empty");
    				user.setImageUrl("user.png");
    				//file is empty
    				
    			}else {
    				// upload to folder
    				user.setImageUrl(file.getOriginalFilename());
    				File saveFile =new ClassPathResource("static/img").getFile();
    				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
    				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    			System.out.println("image is uploaded");
    			}
        	 user.setRole("ROLE_USER");
        	 user.setEnabled(true);
        	 
        	 
        	 
        	 user.setPassword(passwordEncoder.encode(user.getPassword()));
        	 
        	 System.out.println("Agreement "+agreement);
    	System.out.println("USER "+user);
    	
    	
    	//verification-code
    	String randomCode=RandomString.make(64);
    	user.setVerificationCode(randomCode);
    	
    	User result = this.userRepository.save(user);
    	
    	sendVerificationEmail(user);
    	
    	model.addAttribute("user", new User());
    	
    	session.setAttribute("message", new Message("Please check your email and verify !!", "alert-success"));
		
    	return "signup";
    		
		} catch (Exception e) {
			
			
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Sonthing went wrong !!"+e.getMessage(), "alert-danger"));
			return "signup";
				
		}
	}
     
     public void sendVerificationEmail(@Valid User user) {
		String subject = "Please verify your registration";
		String senderName = "Smart Contact Manager";
		String mailContent="<p>Dear " + user.getName() + ",</p>";
		mailContent += "<p> Please click the link below to verify to your registration:</p>";
		
		
		mailContent += "<p>Thank you <br>Smart Contact Manager</p>";
		
		
	}





	//handler for custom login
     
     @GetMapping("/signin")
     public String customLogin(Model model) {
    	 model.addAttribute("title" , "About-smart Contact Manager");
    	 return "login";
     }
     
     
     
}

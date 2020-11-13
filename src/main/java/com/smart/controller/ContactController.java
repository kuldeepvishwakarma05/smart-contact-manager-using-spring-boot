package com.smart.controller;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;





@Controller
public class ContactController {

	
	@Autowired
	private JavaMailSender mailSender;
	
	@GetMapping("/contact")
	public String showContactForm() {
		
		return "contact_form";
	}
	
	@PostMapping("/contact")
	public String submitContact(HttpServletRequest request,
			@RequestParam("attachment") MultipartFile multipartFile
			) throws MessagingException, UnsupportedEncodingException{
		
		String fullname = request.getParameter("fullname");
		String email = request.getParameter("email");
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");
        
		MimeMessage simplemassage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(simplemassage , true);
		
		String mailSubject = fullname + " has sent a message";
		String mailContent = "<h3><b>Sender Name:</b> " + fullname + "</h3>";
		mailContent +="<h3><b>Sender E-mail:</b> " + email + "</h3>";
		mailContent +="<h3><b>Subject:</b> " + subject + "</h3>";
		mailContent +="<h3><b>Content:</b> " + content + "</h3>";
		
         
		helper.setFrom("kv752547@gmail.com", "SmartContactManager");
		helper.setTo("tabcamp.official@gmail.com");
		
		
		helper.setSubject(mailSubject);
		helper.setText(mailContent, true);
		
		
		
		if(!multipartFile.isEmpty()){
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			InputStreamSource source = new InputStreamSource() {
				
				@Override
				public InputStream getInputStream() throws IOException {
					
					return multipartFile.getInputStream();
				}
			};
			helper.addAttachment(fileName, source);
		}
		
		mailSender.send(simplemassage);
		
		return "message";
	}
}

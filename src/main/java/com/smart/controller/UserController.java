package com.smart.controller;



import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;





@Controller
@RequestMapping("/user")

public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal){
		String userName = principal.getName();
		System.out.println("USERNAME " +userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		
		model.addAttribute("user" ,user);
		
	}
	
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
              model.addAttribute("title", "Dashboard");
		
		
		return "normal/user_dashboard";
	}
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file , 
			Principal principal, HttpSession session) {
		
		try {
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		if (file.isEmpty()) {
			System.out.println("file is empty");
			contact.setImage("contactii.png");
			//file is empty
			
		}else {
			// upload to folder
			contact.setImage(file.getOriginalFilename());
			File saveFile =new ClassPathResource("static/img").getFile();
			Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("image is uploaded");
		}
		
		
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		System.out.println("Added to database");
		
		System.out.println("DATA "+contact);
		
		//message success
		
		session.setAttribute("message", new Message("Your contact in addes !! add more","success"));
		return "normal/user_dashboard";
		
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			
			//message error
			session.setAttribute("message", new Message("Somthing went wrong !! try again","danger"));
			return "normal/add_contact_form";
			
		}

		
		}

	//show contacts handler 
	// per page = 5[n]
	//current page = 0[page]
	
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m, Principal principal) {
	m.addAttribute("title", "Show user details");
	
	//sending contacts list from database
	String userName=principal.getName();
	User user=this.userRepository.getUserByUserName(userName);
	
	
	//currentPage-page
	//contact page page-5
	
	
	 Pageable pageable=PageRequest.of(page, 3);
	Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
	m.addAttribute("contacts", contacts);
	m.addAttribute("currentPage", page);
	m.addAttribute("totalpages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particular contact details.
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
		    model.addAttribute("title", contact.getName());	
		}
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId , Model model, HttpSession session,
			Principal principal) {
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		
		Contact contact=contactOptional.get();
		
		//deleting 
		//contact.setUser(null);
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		
		
		//this.contactRepository.delete(contact);
		
		
		
		session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	//open update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,Model m) {
		
	m.addAttribute("title","update contact");
	
	Contact contact=this.contactRepository.findById(cid).get();
	
	m.addAttribute("contact", contact);
	
		return "normal/update_form";
	}
	//update contact handler
	
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam ("profileImage") MultipartFile file , Model m, HttpSession session, Principal principal) {
		try {
			//old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
				//file work
				//rewrite
			
				//delete old photo
				File deleteFile =new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				//update new photo
				File saveFile =new ClassPathResource("static/img").getFile();
				
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact updated", "success") );
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT" +contact.getName());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	@GetMapping("/user-profile")
	public String userProfile(Model m) {
		
	m.addAttribute("title", "Your Profile");
	
		return "normal/user_profile";
	}
	
	}
	


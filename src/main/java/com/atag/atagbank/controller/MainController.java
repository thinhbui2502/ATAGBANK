package com.atag.atagbank.controller;

import com.atag.atagbank.model.ConfirmationToken;
import com.atag.atagbank.model.MyUser;
import com.atag.atagbank.service.EmailSenderService;
import com.atag.atagbank.service.confirmationToken.IConfirmationTokenService;
import com.atag.atagbank.model.Role;
import com.atag.atagbank.service.user.MyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Controller
@SessionAttributes("currentUser")
public class MainController {

    @ModelAttribute("currentUser")
    public MyUser getCurrentUser() {
        return new MyUser();
    }

    @Autowired
    MyUserService myUserService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private IConfirmationTokenService confirmationTokenService;

    @GetMapping("/")
    public ModelAndView getHomePage() {
        return new ModelAndView("index");
    }

    @GetMapping("/login-form")
    public ModelAndView getLoginForm(@ModelAttribute MyUser currentUser) {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("currentUser", currentUser);
        return modelAndView;
    }

   @PostMapping("/login")
    public ModelAndView login(@ModelAttribute MyUser currentUser, HttpSession session) {
        MyUser loginUser = myUserService.findByUserName(currentUser.getUsername());
        if (loginUser != null && currentUser.getPassword().equals(loginUser.getPassword())) {
            if (loginUser.isEnabled()) {
                session.setAttribute("currentUser", loginUser);
                session.setAttribute("currentUserName", loginUser.getName());
                ModelAndView modelAndView = new ModelAndView("index");
                modelAndView.addObject("currentUser", loginUser);
                return modelAndView;
            } else {
                return new ModelAndView("login", "deactivated", "Account is deactivated. Please contact Admin to active!");
            }
        }
        return new ModelAndView("login", "notFound", "Wrong username or password!");
    }

    @GetMapping("/personal-profile")
    public ModelAndView editProfile() {
        return new ModelAndView("personal/profile");
    }

    @PostMapping("/personal-profile")
    public ModelAndView updateProfile(@ModelAttribute MyUser customer) {
        myUserService.save(customer);
        MyUser updatedCustomer = myUserService.findById(customer.getId());
        updatedCustomer.setEnabled(true);

        ModelAndView modelAndView = new ModelAndView("personal/profile");
        modelAndView.addObject("currentUser", updatedCustomer);
        modelAndView.addObject("message", "The information has been updated!");
        return modelAndView;
    }

    @RequestMapping(value="/registration", method = RequestMethod.GET)
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        MyUser user = new MyUser();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid @ModelAttribute("user") MyUser user, BindingResult bindingResult) {
        Long id = Long.valueOf(myUserService.findAllList().size() + 1);
        user.setId(id);
        if (myUserService.isRegister(user)) {
            ModelAndView modelAndView = new ModelAndView("registration");
            modelAndView.addObject("message", "Username or email is already registered");
            return modelAndView;
        }
        if (!myUserService.isCorrectConfirmPassword(user)) {
            ModelAndView modelAndView = new ModelAndView("registration");
            modelAndView.addObject("message", "Confirm Password is incorrect");
            return modelAndView;
        }
        if (bindingResult.hasFieldErrors()) {
            ModelAndView modelAndView1 = new ModelAndView();
            modelAndView1.setViewName("registration");
            modelAndView1.addObject("user", user);
            return modelAndView1;
        } else {
            ModelAndView modelAndView = new ModelAndView("successfulRegisteration");
            myUserService.saveUser(user);
            ConfirmationToken confirmationToken = new ConfirmationToken(user);

            confirmationTokenService.save(confirmationToken);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Complete Registration!");
            mailMessage.setFrom("huynhxuanbui@gmail.com");
            mailMessage.setText("To confirm your account, please click here : "
                    + "10.30.0.78:8080/confirm-account?token=" + confirmationToken.getConfirmationToken());

            emailSenderService.sendEmail(mailMessage);

            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("successMessage", "User has been registered successfully");
            return modelAndView;
        }
    }

    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET})
    public ModelAndView confirmUserAccount(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
    {
        ConfirmationToken token = confirmationTokenService.findByConfirmationToken(confirmationToken);
        if(token != null)
        {
            MyUser user = token.getUser();
            user.setEnabled(true);
            myUserService.save(user);
            modelAndView.setViewName("accountVerified");
        }
        else
        {
            modelAndView.addObject("message","The link is invalid or broken!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

}
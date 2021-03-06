package com.atag.atagbank.service.user;

import com.atag.atagbank.model.MyUser;
import com.atag.atagbank.model.Role;
import com.atag.atagbank.repository.MyUserRepository;
import com.atag.atagbank.repository.RoleRepository;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.transaction.Transactional;
import java.util.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@SessionAttributes("username")
@Component
public class MyUserServiceImpl implements MyUserService {

    @ModelAttribute("username")
    String getUsername() {
        return "";
    }

    MyUserRepository myUserRepository;
    RoleRepository roleRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public MyUserServiceImpl(MyUserRepository myUserRepository,
                             RoleRepository roleRepository,
                             BCryptPasswordEncoder bCryptPasswordEncoder, ObjectFactory<HttpSession> httpSessionFactory) {
        this.myUserRepository = myUserRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Page<MyUser> findAll(Pageable pageable) {
        return myUserRepository.findAll(pageable);
    }

    @Override
    public void save(MyUser user) {
        myUserRepository.save(user);
    }

    @Override
    public MyUser findById(Long id) {
        return myUserRepository.findById(id).get();
    }

    @Override
    public MyUser findByUserName(String username) {
        return myUserRepository.findByUsername(username);
    }

    @Override
    public MyUser findByName(String name) {
        return myUserRepository.findByName(name);
    }

    @Override
    public MyUser findByEmail(String email) {
        return myUserRepository.findByEmail(email);
    }

    @Override
    public List<MyUser> findByNameOrUsernameOrAddressOrRole_RoleLike(String keyword) {
        return myUserRepository.findByNameOrUsernameOrAddressOrRole_RoleLike("%" + keyword + "%");
    }

    @Override
    public boolean isRegister(MyUser user) {
        boolean isRegister = false;
        Iterable<MyUser> users = myUserRepository.findAll();
        for (MyUser currentUser : users) {
            if (user.getUsername().equals(currentUser.getUsername()) ||
                    user.getEmail().equals(currentUser.getEmail())) {
                isRegister = true;
                break;
            }
        }
        return isRegister;
    }

    @Override
    public boolean isCorrectConfirmPassword(MyUser user) {
        boolean isCorrentConfirmPassword = false;
        if (user.getPassword().equals(user.getConfirmPassword())) {
            isCorrentConfirmPassword = true;
        }
        return isCorrentConfirmPassword;
    }

    @Override
    public List<MyUser> findAllList() {

        return (List<MyUser>) myUserRepository.findAll();
    }


    @Autowired
    HttpSession session;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser myUser = myUserRepository.findByUsername(username);
        if (myUser == null) {
            myUser = new MyUser();
            myUser.setUsername(username);
            myUser.setPassword("");
            myUser.setRole(new Role(2L, "ROLE_USER"));
        }

        if (!myUser.isEnabled()) {
            return null;
        }

        List<GrantedAuthority> authors = new ArrayList<>();
        authors.add(new SimpleGrantedAuthority(myUser.getRole().getRole()));

        return new User(myUser.getUsername(), myUser.getPassword(), authors);
    }
}


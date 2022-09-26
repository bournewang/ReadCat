package org.readcat.security.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.readcat.security.repository.AuthorityRepository;
import org.readcat.security.repository.UserRepository;
import org.readcat.security.rest.dto.LoginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.readcat.security.jwt.JWTFilter;
import org.readcat.security.jwt.TokenProvider;

import javax.validation.Valid;
import java.util.Collections;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class AuthenticationRestController {
   @Autowired
   UserRepository userRepository;

   @Autowired
   AuthorityRepository authorityRepository;

   private final TokenProvider tokenProvider;

   private final AuthenticationManagerBuilder authenticationManagerBuilder;

   public AuthenticationRestController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
      this.tokenProvider = tokenProvider;
      this.authenticationManagerBuilder = authenticationManagerBuilder;
   }

   @PostMapping("/login")
   public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginDto loginDto) {

      try {
         UsernamePasswordAuthenticationToken authenticationToken =
                 new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

         Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
         SecurityContextHolder.getContext().setAuthentication(authentication);

         boolean rememberMe = (loginDto.isRememberMe() == null) ? false : loginDto.isRememberMe();
         String jwt = tokenProvider.createToken(authentication, rememberMe);

         HttpHeaders httpHeaders = new HttpHeaders();
         httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

         return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
      } catch(Exception e) {
         System.out.println(e.getMessage());
      }
      return null;
   }

//   @PostMapping("/register")
//   public ResponseEntity<?> register(@Valid @RequestBody LoginDto loginDto) {
//      if (userRepository.existsByUsername(loginDto.getUsername())) {
//         return new ResponseEntity<>("User already exists!", HttpStatus.BAD_REQUEST);
//      }
//      System.out.println("=============");
//      System.out.println(loginDto);
//      var password = loginDto.getPassword();
//      loginDto.setPassword(passwordEncoder.encode(password));
//      Role roles = authorityRepository.findByName("User").get();
//      loginDto.setRoles(Collections.singleton(roles));
//      userRepository.save(user);
//
//      Authentication authentication = authenticationManagerBuilder.getObject().authenticate(
//              new UsernamePasswordAuthenticationToken(
//                      user.getUsername(), password
//              )
//      );
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//      String jwt = tokenProvider.createToken(authentication, true);
//      HttpHeaders httpHeaders = new HttpHeaders();
//      httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
//
//      return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
//   }

   /**
    * Object to return as body in JWT Authentication.
    */
   static class JWTToken {

      private String idToken;

      JWTToken(String idToken) {
         this.idToken = idToken;
      }

      @JsonProperty("id_token")
      String getIdToken() {
         return idToken;
      }

      void setIdToken(String idToken) {
         this.idToken = idToken;
      }
   }
}

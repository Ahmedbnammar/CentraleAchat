package com.CentraleAchat.userservice.controllers;

import com.CentraleAchat.userservice.services.utilsService.EmailSenderService;
import com.CentraleAchat.userservice.dto.UserDto;
import com.CentraleAchat.userservice.services.entitiesService.UserService;
import com.CentraleAchat.userservice.services.utilsService.KeycloakService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@RestController
@RequestMapping("user")
@Slf4j
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private UserService userService;
    private EmailSenderService emailSenderService;
    private KeycloakService keycloakService;
    private Keycloak keycloak;

    ///test
    @GetMapping("/test")
    public ResponseEntity test() {

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/sendMail")
    public ResponseEntity sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        emailSenderService.sendSimpleEmail(to, subject, body);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestParam String username, @RequestParam String password) {
        return ResponseEntity.status(HttpStatus.OK).body(KeycloakService.authenticate(username, password));
    }

    //    @PostMapping("/logout")
//    public ResponseEntity logout() {
//        return ResponseEntity.status(HttpStatus.OK).body(KeycloakService.logout(keycloakService.whoAmI()););
//    }
    @CrossOrigin(origins = "*")
    @GetMapping("/profile")
    public ResponseEntity profile() {
        return ResponseEntity.status(HttpStatus.OK).body(keycloakService.whoAmI());
    }

    ///

    @PostMapping("/registerSupplierClient")
    public ResponseEntity registerSupplierClient(@Valid @RequestBody UserDto userDto) {
        String result = userService.registerSupplierClient(userDto);
        if (result == "User Already Exist")
            return ResponseEntity.unprocessableEntity().body(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/addOperatorCourier")
    @RolesAllowed({"SYSTEMADMIN", "SUPPLIER", "ADMIN"})
    public ResponseEntity registerOperatorCourier(@Valid @RequestBody UserDto userDto) {
        String result = userService.registerOperatorCourier(userDto);
        if (result == "User Already Exist")
            return ResponseEntity.unprocessableEntity().body(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity updateProfile(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateProfile(userDto));
    }

    @PutMapping("/updateEmployee")
    public ResponseEntity updateEmployee(@Valid @RequestBody UserDto userDto, @RequestParam String idEmployee) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateEmployee(userDto, idEmployee));
    }

    @PutMapping("/updatePassword")
    public ResponseEntity updatePassword(@RequestParam String newPassword) {
        userService.updatePassword(newPassword);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/deactivateActivateAccount")
    public String deactivateActivateAccount(@RequestParam String idUser) {
        return "Acount activated: " + userService.deactivateActivateAccount(idUser);
    }

    ///Nadhir start
    @GetMapping("/getNumeroClient/{idClient}")
    public String getNumeroClient(@PathVariable String idClient) {
        return userService.getNumeroClient(idClient);
    }

    ///Nadhir end

}

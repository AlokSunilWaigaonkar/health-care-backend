package com.example.UserManagement.controller;

import com.example.UserManagement.event.RegistrationCompletionEvent;
import com.example.UserManagement.event.listener.RegistrationCompletionListener;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.model.VerificationToken;
import com.example.UserManagement.repo.VerificationTokenRepo;
import com.example.UserManagement.request.RegisterRequest;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.service.PatientService;
import com.example.UserManagement.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;
    private final VerificationTokenRepo verificationTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final RegistrationCompletionListener eventListener;
    private final PatientService patientService;

    @PostMapping("/registerUser")
    public ResponseEntity<ApiResponseDTO<?>> registerUser(@RequestBody RegisterRequest registerRequest, final HttpServletRequest request) {
        try {
            User user = userService.registerUser(registerRequest);
            eventPublisher.publishEvent(new RegistrationCompletionEvent(user, applicationUrl(request)));
            return ResponseEntity.ok(new ApiResponseDTO<>("Success! Please check your email for verification.", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Error during registration: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/registerPatient")
    public ResponseEntity<ApiResponseDTO<?>> registerPatient(@RequestBody RegisterRequest registerRequest, final HttpServletRequest request) {
        try {
            User user = patientService.registerPatient(registerRequest);
            eventPublisher.publishEvent(new RegistrationCompletionEvent(user, applicationUrl(request)));
            return ResponseEntity.ok(new ApiResponseDTO<>("Success! Please check your email for verification.", true, null));
        }
        catch (DataIntegrityViolationException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDTO<>("Email is already registered. Please login or use another email.", false, null));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Error during registration: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/verifyEmail")
    public ResponseEntity<ApiResponseDTO<?>> verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token);

        if (verificationToken == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>("Token not found.", false, null));
        }

        if (verificationToken.getUser().isEnabled()) {
            return ResponseEntity.ok(new ApiResponseDTO<>("This account is already verified, please login.", true, null));
        }

        String verificationResult = userService.validateToken(token);

        if (verificationResult.equalsIgnoreCase("valid")) {
            return ResponseEntity.ok(new ApiResponseDTO<>("Email verified successfully. Now you can login to your account.", true, null));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDTO<>("Invalid or expired verification link.", false, null));
    }

    @GetMapping("/resend-verification-token")
    public ResponseEntity<ApiResponseDTO<?>> resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {
        try {
            VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);

            if (verificationToken == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDTO<>("Token not found or user already verified.", false, null));
            }

            User user = verificationToken.getUser();
            resendVerificationTokenEmail(user, applicationUrl(request), verificationToken);

            return ResponseEntity.ok(new ApiResponseDTO<>("A new verification link has been sent to your email.", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Error resending token: " + e.getMessage(), false, null));
        }
    }

    public void resendVerificationTokenEmail(User user, String applicationUrl, VerificationToken verificationToken)
            throws MessagingException, UnsupportedEncodingException {

        String url = applicationUrl + "/register/verifyEmail?token=" + verificationToken.getToken();
        eventListener.sendVerificationEmail(user, url);
        log.info("Resend verification link: {}", url);
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
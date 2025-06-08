package com.example.UserManagement.model;

import com.example.UserManagement.model.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Date expirationTime;
    public static final int EXPIRATION_TIME = 15;

    public VerificationToken(User user ,String token){
        this.user= user;
        this.token = token;
        this.expirationTime = getTokenExpirationTime();
    }

    public VerificationToken(String token){
        this.token = token;
        this.expirationTime = getTokenExpirationTime();
    }

    public static  Date getTokenExpirationTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE,EXPIRATION_TIME);
        return new Date(calendar.getTime().getTime());
    }
}

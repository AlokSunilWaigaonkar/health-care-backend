package com.example.UserManagement.event;

import com.example.UserManagement.model.Users.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompletionEvent extends ApplicationEvent {
    public User user;
    public String applicationUrl;
    public RegistrationCompletionEvent(User user,String applicationUrl){
        super(user);
        this.user=user;
        this.applicationUrl=applicationUrl;

    }
}

package com.example.UserManagement.model.Users;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
public class Admin  extends User{

    private String FirstName;
    private String LastName;
    private String ContactNo;
    private String contactNumber;
    private String Address;
    private String accessLevel;
}

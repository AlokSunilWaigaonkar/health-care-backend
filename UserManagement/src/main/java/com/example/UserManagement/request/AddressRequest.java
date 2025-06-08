package com.example.UserManagement.request;

import lombok.Data;

@Data
public class AddressRequest {
    private String street;
    private String city;
    private String state;
    private String zipcode;
    private String country;
}

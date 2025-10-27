package com.finance.tracker.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String name;

    public UserDto(String number, String mail, String name) {
        this.id=number;
        this.email=mail;
        this.name=name;
    }
}

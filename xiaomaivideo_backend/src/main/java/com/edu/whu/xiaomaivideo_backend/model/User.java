package com.edu.whu.xiaomaivideo_backend.model;


import javax.persistence.*;

@Entity
@Table(name="userTable")
public class User {
    @Id
    @GeneratedValue
    private Long userId;
    private String username;
    private String password;
    private String gender;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

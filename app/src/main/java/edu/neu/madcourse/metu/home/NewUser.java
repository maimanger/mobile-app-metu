package edu.neu.madcourse.metu.home;

import java.io.Serializable;

public class NewUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String email;

    public NewUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NewUser(String id, String firstname, String lastname, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
package it.unipi.lsmsdb.bookadvisor.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String nickname;
    private String password;
    private LocalDateTime birthdate;
    private String gender;

    public User(String name, String nickname, String password, LocalDateTime birthdate, String gender) {
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
    
    
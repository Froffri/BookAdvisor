package it.unipi.lsmsdb.bookadvisor.model;

import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {
    private String id;
    private String name;
    private String nickname;
    private String password;
    private LocalDate birthdate;
    private String gender;

    public User(String name, String nickname, String password, LocalDate birthdate, String gender) {
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
    }

    public User(Document doc) {
        this.id = doc.getString("id");
        this.name = doc.getString("name");
        this.nickname = doc.getString("nickname");
        this.password = doc.getString("password");
        this.gender = doc.getString("gender");

        // Conversione della stringa di data in LocalDate
        String birthdateStr = doc.getString("birthdate");
        if (birthdateStr != null && !birthdateStr.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.birthdate = LocalDate.parse(birthdateStr, formatter);
        }
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

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
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

    // Convert to MongoDB Document
    public Document toDocument() {
        return new Document("id", id)
                .append("name", name)
                .append("nickname", nickname)
                .append("password", password)
                .append("birthdate", birthdate)
                .append("gender", gender);
    }
}
    
    
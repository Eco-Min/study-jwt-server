package study.jwtServer.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role roles;

    /*public List<String> getRoleList(){
        if (this.roles.toString().length() > 0) {
            return List.of(this.roles.toString().split(","));
        }
        return new ArrayList<>();
    }*/
}

package com.example.mummoomserver.domain.User;

import com.example.mummoomserver.domain.Post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class User { //extends BseTimeEntity
    @Id // pk필드 의미
    @GeneratedValue(strategy = GenerationType.IDENTITY) //autoincrement
    private Long userIdx;

    @Column
    private String imgUrl;

    @Column(nullable = false)
    private String oauthId;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String pwd;

    @Column(nullable = false)
    private String status;

//    @OneToMany(mappedBy = "user")
//    private List<Post> posts = new ArrayList<>();

    @Builder
    public User(String imgUrl,String oauthId, String nickName, String email,String pwd, String status) {
        this.imgUrl = imgUrl;
        this.oauthId = oauthId;
        this.nickName = nickName;
        this.email = email;
        this.pwd = pwd;
        this.status = status;
    }

    public User update(String nickName, String status) {
        this.nickName = nickName;
        this.status = status;

        return this;
    }

}

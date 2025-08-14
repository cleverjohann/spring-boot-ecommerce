package com.example.springbootecommerce.user.specification;

import com.example.springbootecommerce.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> isActive(){
        return (root, query, cb) ->
                cb.equal(root.get("isActive"), true);
    }

    public static Specification<User> nameContains(String name){
        return (root, query, cb) ->cb.or(
                cb.like(cb.lower(root.get("firstName")),"%" +name.toLowerCase() + "%" ),
                cb.like(cb.lower(root.get("lastName")),"%" +name.toLowerCase() + "%" )
        );
    }

    public static Specification<User> hasRole (String roleName){
        return (root, query, cb) ->
                cb.equal(root.join("roles").get("name"), roleName);
    }

    public static Specification<User> registeredBetween(LocalDateTime start, LocalDateTime end){
        return (root, query, cb) ->
                cb.between(root.get("createdAt"), start, end);
    }
}

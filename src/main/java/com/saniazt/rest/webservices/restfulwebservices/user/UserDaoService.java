package com.saniazt.rest.webservices.restfulwebservices.user;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDaoService {

    private static List<User> users = new ArrayList<>();
    private static int usersCount = 1;

    static {
        users.add(new User(usersCount++,"Adam", LocalDate.now().minusYears(30)));
        users.add(new User(usersCount++,"Eve", LocalDate.now().minusYears(25)));
        users.add(new User(usersCount++,"Jim", LocalDate.now().minusYears(20)));
    }

    public List<User> findAll(){
        return users;
    }

    public User findOne(int id){
        return users.stream().filter(user->user.getId()==id).findFirst().orElse(null);
    }

    public User save(User user){
        user.setId(usersCount++);
        users.add(user);
        return user;
    }

}

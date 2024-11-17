package com.example.server_management.service;

import com.example.server_management.models.MyShop;
import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public int registerServiceMethod(String user_name,
                                     String name,
                                     String last_name,
                                     String email,
                                     String password,
                                     String address,
                                     String tel) {
        return userRepository.register(user_name, name, last_name, email, password, address, tel);
    }
    public int countByUserName(String userName) {
        return userRepository.countByUserName(userName);
    }

    public User loginUser(String user_name) {
        return userRepository.findByUserName(user_name);
    }

    public void createShopForUser(String userName, String title, String detail) { // ลบ static ออก
        User user = userRepository.findByUserName(userName);

        if (user != null) {
            MyShop myShop = new MyShop();
            myShop.setTitle(title);
            myShop.setDetail(detail);
            myShop.setUser(user);

            user.setMyShop(myShop);
            userRepository.save(user);
        }
    }
}

package com.example.server_management.rest_controllers;

import com.example.server_management.dto.MyAuctionResponse;
import com.example.server_management.models.MyAuction;
import com.example.server_management.models.User;
import com.example.server_management.repository.MyAuctionRepository;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auctions")
public class MyAuctionController {

    @Autowired
    private MyAuctionRepository myAuctionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-auction")
    public ResponseEntity<?> getMyAuctions(HttpSession session) {
        try {
            String userName = (String) session.getAttribute("user_name");
            if (userName == null) {
                return new ResponseEntity<>(Map.of("message", "User not logged in"), HttpStatus.FORBIDDEN);
            }

            Optional<User> optionalUser = userRepository.findUserByUserName(userName);
            if (!optionalUser.isPresent()) {
                return new ResponseEntity<>(Map.of("message", "User not found"), HttpStatus.NOT_FOUND);
            }

            User user = optionalUser.get();
            List<MyAuction> myAuctions = myAuctionRepository.findByWinner(user);

            if (myAuctions.isEmpty()) {
                return new ResponseEntity<>(Map.of("message", "No winning auctions found"), HttpStatus.OK);
            }

            List<MyAuctionResponse> responses = myAuctions.stream()
                    .map(MyAuctionResponse::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(Map.of("auctions", responses), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Internal Server Error", "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

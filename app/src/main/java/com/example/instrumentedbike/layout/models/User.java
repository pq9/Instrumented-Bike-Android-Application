package com.example.instrumentedbike.layout.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by 邱培杰 on 2018/3/13.
 */
// [START blog_user_class]
@IgnoreExtraProperties

public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
}

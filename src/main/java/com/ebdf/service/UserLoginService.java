
package com.ebdf.service;

import com.ebdf.dao.UserDAO;
import com.ebdf.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserLoginService {

    private UserDAO userDAO = new UserDAO();

    public User loginUser(String email, String password) {

        // 1. Validate email
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        // 2. Validate password
        if (password == null || password.isEmpty()) {
            return null;
        }

        // 3. Find user by email
        User user = userDAO.findByEmail(email.trim());

        if (user == null) {
            return null;
        }

        // 4. Check account status
        if (!"ACTIVE".equals(user.getAccountStatus())) {
            return null;
        }

        // 5. Verify password using BCrypt
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return null;
        }

        // 6. Login successful
        return user;
    }
}

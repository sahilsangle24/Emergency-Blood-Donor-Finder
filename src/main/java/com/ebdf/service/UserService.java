
package com.ebdf.service;

import com.ebdf.dao.UserDAO;
import com.ebdf.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private UserDAO userDAO = new UserDAO();

    public String registerUser(User user, String rawPassword) {

        // 1. Check user details
        if (user == null) {
            return "Invalid user details";
        }

        // 2. Validate name
        if (user.getFullName() == null ||
            user.getFullName().trim().length() < 2 ||
            user.getFullName().trim().length() > 120) {
            return "Name must contain 2-120 characters";
        }

        // 3. Validate email
        if (user.getEmail() == null || user.getEmail().trim().length() > 254 ||
            !user.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "Enter a valid email address (maximum 254 characters)";
        }

        // 4. Validate phone
        if (user.getPhone() == null ||
            !user.getPhone().trim().matches("[0-9]{10,15}")) {
            return "Enter a valid 10-15 digit phone number";
        }

        // 5. Validate password
        if (rawPassword == null || rawPassword.length() < 8) {
            return "Password must contain at least 8 characters";
        }

        // 6. Validate role
        if (user.getRole() == null ||
            !(user.getRole().equals("DONOR") ||
              user.getRole().equals("REQUESTER"))) {
            return "Only donor and requester accounts can be registered publicly";
        }

        // 7. Check whether email already exists
        if (userDAO.emailExists(user.getEmail().trim())) {
            return "Email already registered";
        }

        // 8. Encrypt password using BCrypt
        String hashedPassword =
                BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

        user.setFullName(user.getFullName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        user.setPhone(user.getPhone().trim());
        user.setPasswordHash(hashedPassword);

        // 9. Save user in database
        boolean result = userDAO.registerUser(user);

        if (result) {
            return "Registration successful";
        } else {
            return "Registration failed";
        }
    }
}

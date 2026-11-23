
package com.ebdf.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.model.User;
import com.ebdf.service.UserLoginService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/login")
public class UserLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserLoginService loginService = new UserLoginService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            JsonObject json = gson.fromJson(
                    request.getReader(), JsonObject.class);

            if (json == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                        "{\"message\":\"Invalid request body\"}");
                return;
            }

            String email = json.has("email")
                    ? json.get("email").getAsString() : null;

            String password = json.has("password")
                    ? json.get("password").getAsString() : null;

            // Authenticate user
            User user = loginService.loginUser(email, password);

            JsonObject output = new JsonObject();

            if (user != null) {

                // Rotate the session identifier after successful authentication.
                HttpSession previousSession = request.getSession(false);
                if (previousSession != null) {
                    previousSession.invalidate();
                }
                HttpSession session = request.getSession(true);
                session.setMaxInactiveInterval(30 * 60);

                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("fullName", user.getFullName());
                session.setAttribute("email", user.getEmail());

                output.addProperty("message", "Login successful");
                output.addProperty("userId", user.getUserId());
                output.addProperty("fullName", user.getFullName());
                output.addProperty("email", user.getEmail());
                output.addProperty("role", user.getRole());

                response.setStatus(HttpServletResponse.SC_OK);

            } else {

                output.addProperty("message",
                        "Invalid email or password, or account inactive");

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);
            }

            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            JsonObject output = new JsonObject();
            output.addProperty("message", "Login failed");

            response.getWriter().write(gson.toJson(output));
        }
    }
}

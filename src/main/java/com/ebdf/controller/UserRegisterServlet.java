
package com.ebdf.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ebdf.model.User;
import com.ebdf.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/register")
public class UserRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserService userService = new UserService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (!new com.ebdf.dao.AppSettingsDAO().enabled("allowRegistration", true)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"New registrations are temporarily disabled\"}");
                return;
            }
            JsonObject json = gson.fromJson(
                    request.getReader(), JsonObject.class);

            if (json == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                        "{\"message\":\"Invalid request body\"}");
                return;
            }

            User user = new User();

            user.setFullName(json.has("fullName")
                    ? json.get("fullName").getAsString() : null);

            user.setEmail(json.has("email")
                    ? json.get("email").getAsString() : null);

            user.setPhone(json.has("phone")
                    ? json.get("phone").getAsString() : null);

            user.setRole(json.has("role")
                    ? json.get("role").getAsString() : null);

            String password = json.has("password")
                    ? json.get("password").getAsString() : null;

            String result = userService.registerUser(user, password);

            JsonObject output = new JsonObject();
            output.addProperty("message", result);

            if ("Registration successful".equals(result)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            JsonObject output = new JsonObject();
            output.addProperty("message", "Registration failed");

            response.getWriter().write(gson.toJson(output));
            e.printStackTrace();
        }
    }
}

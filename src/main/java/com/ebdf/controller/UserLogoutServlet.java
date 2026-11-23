
package com.ebdf.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

@WebServlet("/api/logout")

public class UserLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        JsonObject output = new JsonObject();

        if (session != null) {
            session.invalidate();
            output.addProperty("message", "Logout successful");
        } else {
            output.addProperty("message", "No active session");
        }

        response.getWriter().write(gson.toJson(output));
    }
}

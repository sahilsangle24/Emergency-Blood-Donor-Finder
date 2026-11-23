
package com.ebdf.controller;

import java.io.IOException;
import java.util.List;

import com.ebdf.dao.DonorSearchDAO;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/donors/search")
public class DonorSearchServlet extends HttpServlet {

    private DonorSearchDAO donorSearchDAO;
    private Gson gson;

    @Override
    public void init() {
        donorSearchDAO = new DonorSearchDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"Please login first\"}");
                return;
            }
            String role = String.valueOf(session.getAttribute("userRole"));
            if (!"REQUESTER".equals(role) && !"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Only requesters can search donors\"}");
                return;
            }

            String bloodGroupIdParam =
                    request.getParameter("bloodGroupId");

            if (bloodGroupIdParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"message\":\"bloodGroupId is required\"}"
                );
                return;
            }

            int bloodGroupId = Integer.parseInt(bloodGroupIdParam);

            String city = request.getParameter("city");

            List<java.util.Map<String,Object>> donors =
                donorSearchDAO.searchAvailableDonors(bloodGroupId, city);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(donors));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"message\":\"Invalid bloodGroupId\"}"
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"message\":\"Unable to search donors\"}"
            );
        }
    }
}

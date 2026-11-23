package com.ebdf.controller;
import com.ebdf.dao.PortalDAO;import com.google.gson.Gson;import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;import java.io.*;
@WebServlet("/api/requester/responses") public class RequesterResponsesServlet extends HttpServlet {
 private final PortalDAO dao=new PortalDAO();private final Gson gson=new Gson();
 protected void doGet(HttpServletRequest q,HttpServletResponse p)throws IOException{p.setContentType("application/json");p.setCharacterEncoding("UTF-8");HttpSession s=q.getSession(false);if(s==null||s.getAttribute("userId")==null){p.setStatus(401);p.getWriter().write("{\"message\":\"Please login first\"}");return;}if(!"REQUESTER".equals(s.getAttribute("userRole"))){p.setStatus(403);return;}try{p.getWriter().write(gson.toJson(dao.requesterResponses(((Number)s.getAttribute("userId")).intValue())));}catch(Exception e){p.setStatus(500);p.getWriter().write("{\"message\":\"Unable to load requester responses\"}");}}
}

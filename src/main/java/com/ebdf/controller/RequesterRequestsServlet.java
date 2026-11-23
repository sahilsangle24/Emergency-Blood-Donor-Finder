package com.ebdf.controller;
import com.ebdf.dao.PortalDAO; import com.google.gson.Gson; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*; import java.io.*;
@WebServlet("/api/my-blood-requests") public class RequesterRequestsServlet extends HttpServlet {
 private final PortalDAO dao=new PortalDAO(); private final Gson gson=new Gson();
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{res.setContentType("application/json");res.setCharacterEncoding("UTF-8");HttpSession s=req.getSession(false);if(s==null||s.getAttribute("userId")==null){res.setStatus(401);res.getWriter().write("{\"message\":\"Please login first\"}");return;}if(!"REQUESTER".equals(s.getAttribute("userRole"))&& !"ADMIN".equals(s.getAttribute("userRole"))){res.setStatus(403);return;}try{res.getWriter().write(gson.toJson(dao.myRequests(((Number)s.getAttribute("userId")).intValue())));}catch(Exception e){getServletContext().log("request history failed",e);res.setStatus(500);res.getWriter().write("{\"message\":\"Unable to load request history\"}");}}
}

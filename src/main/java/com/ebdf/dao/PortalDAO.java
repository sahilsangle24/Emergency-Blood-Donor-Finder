package com.ebdf.dao;

import com.ebdf.util.DBConnection;
import java.sql.*;
import java.util.*;

/** Oracle-backed read/write operations used by requester, donor and admin screens. */
public class PortalDAO {
    private List<Map<String,Object>> rows(String sql, Binder binder) throws SQLException {
        List<Map<String,Object>> data = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            if(binder!=null) binder.bind(p);
            try(ResultSet r=p.executeQuery()) {
                ResultSetMetaData m=r.getMetaData(); int n=m.getColumnCount();
                while(r.next()) { Map<String,Object> row=new LinkedHashMap<>(); for(int i=1;i<=n;i++){Object v=r.getObject(i); if(v instanceof Timestamp) v=((Timestamp)v).toLocalDateTime().toString(); else if(v instanceof java.sql.Date) v=((java.sql.Date)v).toString(); row.put(m.getColumnLabel(i).toLowerCase(),v);} data.add(row); }
            }
        } return data;
    }
    private int update(String sql, Binder binder) throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(binder!=null)binder.bind(p);return p.executeUpdate();} }
    @FunctionalInterface private interface Binder { void bind(PreparedStatement p)throws SQLException; }

    public List<Map<String,Object>> myRequests(int userId)throws SQLException{return rows("""
      SELECT br.request_id, bg.blood_group_code, br.hospital_name, br.hospital_address,
      br.units_required, br.urgency, br.request_status, br.required_by, br.created_at,
      (SELECT COUNT(*) FROM DONOR_RESPONSES dr WHERE dr.request_id=br.request_id) response_count
      FROM BLOOD_REQUESTS br JOIN BLOOD_GROUPS bg ON bg.blood_group_id=br.blood_group_id
      WHERE br.requester_user_id=? ORDER BY br.created_at DESC
      """,p->p.setInt(1,userId));}
    public List<Map<String,Object>> myResponses(int userId)throws SQLException{return rows("""
      SELECT di.invitation_id response_id,di.request_id,d.donor_id,
      CASE di.invitation_status WHEN 'SENT' THEN 'PENDING' WHEN 'ACCEPTED' THEN 'ACCEPTED' ELSE 'REJECTED' END response_status,
      di.created_at responded_at,CAST(NULL AS VARCHAR2(1000)) response_notes,bg.blood_group_code,
      br.hospital_name,br.hospital_address,br.units_required,br.urgency,br.request_status,l.city
      FROM DONOR_INVITATIONS di JOIN DONORS d ON d.donor_id=di.donor_id
      JOIN BLOOD_REQUESTS br ON br.request_id=di.request_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=br.blood_group_id
      LEFT JOIN LOCATIONS l ON l.location_id=br.location_id WHERE d.user_id=?
      UNION ALL
      SELECT dr.response_id,dr.request_id,d.donor_id,dr.response_status,dr.responded_at,dr.response_notes,
      bg.blood_group_code,br.hospital_name,br.hospital_address,br.units_required,br.urgency,br.request_status,l.city
      FROM DONOR_RESPONSES dr JOIN DONORS d ON d.donor_id=dr.donor_id
      JOIN BLOOD_REQUESTS br ON br.request_id=dr.request_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=br.blood_group_id
      LEFT JOIN LOCATIONS l ON l.location_id=br.location_id WHERE d.user_id=?
      AND NOT EXISTS (SELECT 1 FROM DONOR_INVITATIONS di WHERE di.request_id=dr.request_id AND di.donor_id=dr.donor_id)
      ORDER BY responded_at DESC
      """,p->{p.setInt(1,userId);p.setInt(2,userId);});}
    public List<Map<String,Object>> requestResponses(int requestId)throws SQLException{return rows("""
      SELECT dr.response_id,dr.response_status,dr.responded_at,dr.response_notes,
      u.full_name donor_name,bg.blood_group_code,l.city
      FROM DONOR_RESPONSES dr JOIN DONORS d ON d.donor_id=dr.donor_id
      JOIN USERS u ON u.user_id=d.user_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
      LEFT JOIN LOCATIONS l ON l.location_id=d.location_id WHERE dr.request_id=? ORDER BY dr.responded_at DESC
      """,p->p.setInt(1,requestId));}
    public List<Map<String,Object>> donationHistory(int userId)throws SQLException{return rows("""
      SELECT dh.donation_id,dh.request_id,dh.donation_date,dh.units_donated,dh.verification_status,
      dh.hospital_name,bg.blood_group_code,l.city FROM DONATION_HISTORY dh JOIN DONORS d ON d.donor_id=dh.donor_id
      JOIN USERS u ON u.user_id=d.user_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
      LEFT JOIN LOCATIONS l ON l.location_id=d.location_id WHERE u.user_id=? ORDER BY dh.donation_date DESC
      """,p->p.setInt(1,userId));}
    public List<Map<String,Object>> notifications(int userId)throws SQLException{return rows("""
      SELECT notification_id,title,message,notification_type,is_read,related_request_id,created_at
      FROM NOTIFICATIONS WHERE user_id=? ORDER BY created_at DESC
      """,p->p.setInt(1,userId));}
    public boolean markNotificationRead(int id,int userId)throws SQLException{return update("UPDATE NOTIFICATIONS SET is_read='Y' WHERE notification_id=? AND user_id=?",p->{p.setInt(1,id);p.setInt(2,userId);})==1;}
    public int markAllNotificationsRead(int userId)throws SQLException{return update("UPDATE NOTIFICATIONS SET is_read='Y' WHERE user_id=? AND is_read='N'",p->p.setInt(1,userId));}
    public boolean deleteNotification(int id,int userId)throws SQLException{return update("DELETE FROM NOTIFICATIONS WHERE notification_id=? AND user_id=?",p->{p.setInt(1,id);p.setInt(2,userId);})==1;}

    public Map<String,Object> adminStats()throws SQLException{
      Map<String,Object> s=new LinkedHashMap<>();
      String q="SELECT (SELECT COUNT(*) FROM USERS) total_users,(SELECT COUNT(*) FROM DONORS) total_donors,(SELECT COUNT(*) FROM BLOOD_REQUESTS WHERE request_status IN ('OPEN','MATCHED')) active_requests,(SELECT COUNT(*) FROM DONATION_HISTORY WHERE verification_status='VERIFIED') verified_donations,(SELECT COUNT(*) FROM REQUESTERS) total_requesters FROM DUAL";
      List<Map<String,Object>> r=rows(q,null); if(!r.isEmpty())s.putAll(r.get(0)); return s;
    }
    public List<Map<String,Object>> adminUsers()throws SQLException{return rows("SELECT user_id,full_name,email,phone,role,account_status,created_at FROM USERS ORDER BY created_at DESC",null);}
    public List<Map<String,Object>> adminDonors()throws SQLException{return rows("""
      SELECT d.donor_id,u.user_id,u.full_name,u.email,u.phone,bg.blood_group_code,l.city,d.availability_status,d.contact_consent
      FROM DONORS d JOIN USERS u ON u.user_id=d.user_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
      LEFT JOIN LOCATIONS l ON l.location_id=d.location_id ORDER BY d.created_at DESC
      """,null);}
    public List<Map<String,Object>> adminRequests()throws SQLException{return rows("""
      SELECT br.request_id,br.requester_user_id,u.full_name requester_name,bg.blood_group_code,br.hospital_name,
      br.hospital_address,br.units_required,br.urgency,br.request_status,br.required_by,br.created_at
      FROM BLOOD_REQUESTS br JOIN USERS u ON u.user_id=br.requester_user_id
      JOIN BLOOD_GROUPS bg ON bg.blood_group_id=br.blood_group_id ORDER BY br.created_at DESC
      """,null);}
    public List<Map<String,Object>> adminDonations()throws SQLException{return rows("""
      SELECT dh.donation_id,dh.request_id,dh.donation_date,dh.units_donated,dh.verification_status,
      dh.hospital_name,u.full_name donor_name,u.phone,bg.blood_group_code,l.city
      FROM DONATION_HISTORY dh JOIN DONORS d ON d.donor_id=dh.donor_id JOIN USERS u ON u.user_id=d.user_id
      JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id LEFT JOIN LOCATIONS l ON l.location_id=d.location_id
      ORDER BY dh.created_at DESC
      """,null);}
    public boolean setUserStatus(int id,String status)throws SQLException{return update("UPDATE USERS SET account_status=? WHERE user_id=? AND role<>'ADMIN'",p->{p.setString(1,status);p.setInt(2,id);})==1;}
    public boolean setDonorAvailability(int id,String status)throws SQLException{return update("UPDATE DONORS SET availability_status=? WHERE donor_id=?",p->{p.setString(1,status);p.setInt(2,id);})==1;}
    public boolean setRequestStatus(int id,String status)throws SQLException{return update("UPDATE BLOOD_REQUESTS SET request_status=?,updated_at=SYSTIMESTAMP WHERE request_id=? AND request_status NOT IN ('CANCELLED','CLOSED')",p->{p.setString(1,status);p.setInt(2,id);})==1;}
    public boolean verifyDonation(int id,String status)throws SQLException{
      List<Map<String,Object>> owner=rows("SELECT u.user_id,dh.request_id FROM DONATION_HISTORY dh JOIN DONORS d ON d.donor_id=dh.donor_id JOIN USERS u ON u.user_id=d.user_id WHERE dh.donation_id=?",p->p.setInt(1,id));
      boolean changed=update("UPDATE DONATION_HISTORY SET verification_status=? WHERE donation_id=? AND verification_status='PENDING'",p->{p.setString(1,status);p.setInt(2,id);})==1;
      if(changed&&!owner.isEmpty()){Object req=owner.get(0).get("request_id");createNotification(((Number)owner.get(0).get("user_id")).intValue(),"Donation record reviewed","Your donation record #"+id+" was "+status.toLowerCase()+".","DONATION_REVIEW",req==null?null:((Number)req).intValue());}
      return changed;
    }

    public List<Map<String,Object>> openRequestsForDonor(int userId)throws SQLException{return rows("""
      SELECT br.request_id,br.requester_user_id,u.full_name requester_name,bg.blood_group_code,
      br.hospital_name,br.hospital_address,br.units_required,br.urgency,br.required_by,br.request_status,
      l.city,l.state,dr.response_status FROM BLOOD_REQUESTS br JOIN USERS u ON u.user_id=br.requester_user_id
      JOIN BLOOD_GROUPS bg ON bg.blood_group_id=br.blood_group_id LEFT JOIN LOCATIONS l ON l.location_id=br.location_id
      JOIN DONORS d ON d.user_id=? JOIN USERS donor_user ON donor_user.user_id=d.user_id
      JOIN BLOOD_COMPATIBILITY bc ON bc.donor_group_id=d.blood_group_id AND bc.recipient_group_id=br.blood_group_id
      LEFT JOIN DONOR_RESPONSES dr ON dr.request_id=br.request_id AND dr.donor_id=d.donor_id
      WHERE br.request_status='OPEN' AND d.availability_status='AVAILABLE' AND d.contact_consent='Y'
      AND donor_user.account_status='ACTIVE' AND u.account_status='ACTIVE'
      AND (dr.response_status IS NULL OR dr.response_status='PENDING')
      ORDER BY CASE br.urgency WHEN 'CRITICAL' THEN 1 WHEN 'URGENT' THEN 2 ELSE 3 END,br.created_at DESC
      """,p->p.setInt(1,userId));}

    public List<Map<String,Object>> requesterResponses(int userId)throws SQLException{return rows("""
      SELECT dr.response_id,dr.request_id,d.donor_id,dr.response_status,dr.responded_at,u.full_name donor_name,
      bg.blood_group_code,br.hospital_name,
      CASE WHEN dr.response_status='ACCEPTED' AND d.contact_consent='Y' THEN u.phone ELSE NULL END donor_phone
      FROM DONOR_RESPONSES dr JOIN BLOOD_REQUESTS br ON br.request_id=dr.request_id
      JOIN DONORS d ON d.donor_id=dr.donor_id JOIN USERS u ON u.user_id=d.user_id
      JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
      WHERE br.requester_user_id=? ORDER BY dr.responded_at DESC
      """,p->p.setInt(1,userId));}

    public Map<String,Object> donorDashboard(int userId)throws SQLException{
      List<Map<String,Object>> r=rows("""
       SELECT d.donor_id,d.availability_status,bg.blood_group_code,l.city,l.state,
       (SELECT COUNT(*) FROM DONATION_HISTORY dh WHERE dh.donor_id=d.donor_id) donation_count,
       (SELECT COUNT(*) FROM DONOR_RESPONSES dr WHERE dr.donor_id=d.donor_id) response_count,
       (SELECT COUNT(*) FROM DONOR_RESPONSES dr WHERE dr.donor_id=d.donor_id AND dr.response_status='ACCEPTED') accepted_count
       FROM DONORS d JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
       LEFT JOIN LOCATIONS l ON l.location_id=d.location_id WHERE d.user_id=?
       """,p->p.setInt(1,userId));
      return r.isEmpty()?Collections.emptyMap():r.get(0);
    }
    public Map<String,Object> requesterDashboard(int userId)throws SQLException{
      List<Map<String,Object>> r=rows("""
       SELECT COUNT(*) total_requests,
       SUM(CASE WHEN request_status='OPEN' THEN 1 ELSE 0 END) open_requests,
       SUM(CASE WHEN request_status='MATCHED' THEN 1 ELSE 0 END) matched_requests,
       SUM(CASE WHEN request_status='FULFILLED' THEN 1 ELSE 0 END) fulfilled_requests
       FROM BLOOD_REQUESTS WHERE requester_user_id=?
       """,p->p.setInt(1,userId));
      return r.isEmpty()?Collections.emptyMap():r.get(0);
    }
    public boolean setDonorAvailabilityByUser(int userId,String status)throws SQLException{
      return update("UPDATE DONORS SET availability_status=? WHERE user_id=?",p->{p.setString(1,status);p.setInt(2,userId);})==1;
    }
    public boolean inviteDonor(int requesterUserId,int requestId,int donorId)throws SQLException{
      String sql="""
       INSERT INTO DONOR_INVITATIONS(request_id,donor_id,invited_by_user_id)
       SELECT br.request_id,d.donor_id,br.requester_user_id
       FROM BLOOD_REQUESTS br JOIN DONORS d ON d.donor_id=? JOIN USERS du ON du.user_id=d.user_id
       JOIN BLOOD_COMPATIBILITY bc ON bc.donor_group_id=d.blood_group_id AND bc.recipient_group_id=br.blood_group_id
       WHERE br.request_id=? AND br.requester_user_id=? AND br.request_status='OPEN'
       AND d.availability_status='AVAILABLE' AND d.contact_consent='Y' AND du.account_status='ACTIVE'
      """;
      int n=update(sql,p->{p.setInt(1,donorId);p.setInt(2,requestId);p.setInt(3,requesterUserId);});
      if(n==1){List<Map<String,Object>> u=rows("SELECT user_id FROM DONORS WHERE donor_id=?",p->p.setInt(1,donorId));if(!u.isEmpty())createNotification(((Number)u.get(0).get("user_id")).intValue(),"Blood donation request","A requester invited you to respond to blood request #"+requestId,"INVITATION",requestId);}
      return n==1;
    }
    public static void createNotification(int userId,String title,String message,String type,Integer requestId){
      String sql="INSERT INTO NOTIFICATIONS(USER_ID,TITLE,MESSAGE,NOTIFICATION_TYPE,RELATED_REQUEST_ID) VALUES(?,?,?,?,?)";
      try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,userId);p.setString(2,title);p.setString(3,message);p.setString(4,type);if(requestId==null)p.setNull(5,Types.NUMERIC);else p.setInt(5,requestId);p.executeUpdate();}catch(SQLException e){e.printStackTrace();}
    }
    public static void notifyAdmins(String title,String message,String type){
      String sql="INSERT INTO NOTIFICATIONS(USER_ID,TITLE,MESSAGE,NOTIFICATION_TYPE) SELECT USER_ID,?,?,? FROM USERS WHERE ROLE='ADMIN' AND ACCOUNT_STATUS='ACTIVE'";
      try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,title);p.setString(2,message);p.setString(3,type);p.executeUpdate();}catch(SQLException e){e.printStackTrace();}
    }
    public Map<String,Object> adminReports()throws SQLException{
      Map<String,Object> out=new LinkedHashMap<>();
      out.put("stats",adminStats());
      out.put("bloodGroups",rows("SELECT bg.blood_group_code blood_group_code,COUNT(br.request_id) request_count FROM BLOOD_GROUPS bg LEFT JOIN BLOOD_REQUESTS br ON br.blood_group_id=bg.blood_group_id GROUP BY bg.blood_group_code,bg.blood_group_id ORDER BY bg.blood_group_id",null));
      out.put("cities",rows("SELECT NVL(l.city,'Not specified') city,COUNT(*) request_count FROM BLOOD_REQUESTS br LEFT JOIN LOCATIONS l ON l.location_id=br.location_id GROUP BY NVL(l.city,'Not specified') ORDER BY request_count DESC FETCH FIRST 8 ROWS ONLY",null));
      out.put("monthly",rows("SELECT month_key month,SUM(request_count) request_count,SUM(donation_count) donation_count FROM (SELECT TO_CHAR(created_at,'YYYY-MM') month_key,COUNT(*) request_count,0 donation_count FROM BLOOD_REQUESTS WHERE created_at>=ADD_MONTHS(TRUNC(SYSDATE,'MM'),-5) GROUP BY TO_CHAR(created_at,'YYYY-MM') UNION ALL SELECT TO_CHAR(donation_date,'YYYY-MM') month_key,0 request_count,COUNT(*) donation_count FROM DONATION_HISTORY WHERE donation_date>=ADD_MONTHS(TRUNC(SYSDATE,'MM'),-5) GROUP BY TO_CHAR(donation_date,'YYYY-MM')) GROUP BY month_key ORDER BY month_key",null));
      return out;
    }
    public Map<String,String> settings()throws SQLException{
      Map<String,String> m=new LinkedHashMap<>();m.put("platformName","LifeLink");m.put("supportEmail","support@lifelink.com");m.put("allowRegistration","true");m.put("allowBloodRequests","true");m.put("emergencyAlerts","true");for(Map<String,Object> r:rows("SELECT setting_key,setting_value FROM APP_SETTINGS",null))m.put(String.valueOf(r.get("setting_key")),String.valueOf(r.get("setting_value")));return m;
    }
    public void saveSettings(Map<String,String> settings)throws SQLException{
      String sql="MERGE INTO APP_SETTINGS t USING(SELECT ? setting_key,? setting_value FROM DUAL) s ON(t.setting_key=s.setting_key) WHEN MATCHED THEN UPDATE SET t.setting_value=s.setting_value,t.updated_at=SYSTIMESTAMP WHEN NOT MATCHED THEN INSERT(setting_key,setting_value) VALUES(s.setting_key,s.setting_value)";
      try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){c.setAutoCommit(false);try{for(Map.Entry<String,String> e:settings.entrySet()){p.setString(1,e.getKey());p.setString(2,e.getValue());p.addBatch();}p.executeBatch();c.commit();}catch(SQLException e){c.rollback();throw e;}}
    }
    public Map<String,Object> accountProfile(int userId)throws SQLException{List<Map<String,Object>> r=rows("SELECT full_name,email,phone,role FROM USERS WHERE user_id=?",p->p.setInt(1,userId));return r.isEmpty()?Collections.emptyMap():r.get(0);}
    public boolean updateAccountProfile(int userId,String name,String phone)throws SQLException{return update("UPDATE USERS SET full_name=?,phone=? WHERE user_id=?",p->{p.setString(1,name);p.setString(2,phone);p.setInt(3,userId);})==1;}


}

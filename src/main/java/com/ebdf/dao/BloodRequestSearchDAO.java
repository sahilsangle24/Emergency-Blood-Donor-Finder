package com.ebdf.dao;
import com.ebdf.util.DBConnection;import java.sql.*;import java.util.*;
public class BloodRequestSearchDAO {
 public List<Map<String,Object>> getOpenBloodRequests(int donorUserId)throws Exception{
  List<Map<String,Object>> data=new ArrayList<>();String sql="""
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
   """;
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,donorUserId);try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object>m=new LinkedHashMap<>();m.put("request_id",r.getInt("request_id"));m.put("requester_name",r.getString("requester_name"));m.put("blood_group_code",r.getString("blood_group_code"));m.put("hospital_name",r.getString("hospital_name"));m.put("hospital_address",r.getString("hospital_address"));m.put("units_required",r.getInt("units_required"));m.put("urgency",r.getString("urgency"));m.put("required_by",r.getTimestamp("required_by"));m.put("request_status",r.getString("request_status"));m.put("city",r.getString("city"));m.put("state",r.getString("state"));m.put("response_status",r.getString("response_status"));data.add(m);}}}return data;
 }
}

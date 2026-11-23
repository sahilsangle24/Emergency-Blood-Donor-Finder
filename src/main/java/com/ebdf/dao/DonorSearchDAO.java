package com.ebdf.dao;
import com.ebdf.util.DBConnection; import java.sql.*; import java.util.*;
public class DonorSearchDAO {
 public List<Map<String,Object>> searchAvailableDonors(int recipientBloodGroupId,String city)throws Exception{
  List<Map<String,Object>> donors=new ArrayList<>(); String sql="""
   SELECT d.donor_id,u.full_name,bg.blood_group_code, l.city,l.district,l.state,
   d.availability_status
   FROM DONORS d JOIN USERS u ON u.user_id=d.user_id JOIN BLOOD_GROUPS bg ON bg.blood_group_id=d.blood_group_id
   JOIN BLOOD_COMPATIBILITY bc ON bc.donor_group_id=d.blood_group_id
   LEFT JOIN LOCATIONS l ON l.location_id=d.location_id
   WHERE bc.recipient_group_id=? AND d.availability_status='AVAILABLE' AND d.contact_consent='Y'
   AND u.account_status='ACTIVE' AND (? IS NULL OR LOWER(l.city)=LOWER(?)) ORDER BY u.full_name
   """;
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,recipientBloodGroupId);if(city==null||city.isBlank()){p.setNull(2,Types.VARCHAR);p.setNull(3,Types.VARCHAR);}else{p.setString(2,city.trim());p.setString(3,city.trim());}try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("donorId",r.getInt("donor_id"));m.put("name",r.getString("full_name"));m.put("bloodGroup",r.getString("blood_group_code"));m.put("city",r.getString("city"));m.put("district",r.getString("district"));m.put("state",r.getString("state"));m.put("availability",r.getString("availability_status"));donors.add(m);}}}return donors;
 }
}

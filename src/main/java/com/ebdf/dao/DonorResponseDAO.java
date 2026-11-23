package com.ebdf.dao;
import java.sql.*;
import com.ebdf.util.DBConnection;
public class DonorResponseDAO {
 public boolean respondToRequest(int requestId,int donorId,String responseStatus,String responseNotes){
  if(!"ACCEPTED".equals(responseStatus)&&!"REJECTED".equals(responseStatus))throw new IllegalArgumentException("Response must be ACCEPTED or REJECTED");
  String eligible="""
   SELECT br.requester_user_id FROM BLOOD_REQUESTS br JOIN DONORS d ON d.donor_id=?
   JOIN USERS du ON du.user_id=d.user_id JOIN BLOOD_COMPATIBILITY bc ON bc.donor_group_id=d.blood_group_id AND bc.recipient_group_id=br.blood_group_id
   WHERE br.request_id=? AND br.request_status='OPEN' AND d.availability_status='AVAILABLE' AND d.contact_consent='Y' AND du.account_status='ACTIVE'
   """;
  try(Connection c=DBConnection.getConnection()){
   c.setAutoCommit(false);int requesterId;
   try(PreparedStatement p=c.prepareStatement(eligible)){p.setInt(1,donorId);p.setInt(2,requestId);try(ResultSet r=p.executeQuery()){if(!r.next()){c.rollback();return false;}requesterId=r.getInt(1);}}
   int updated;
   try(PreparedStatement p=c.prepareStatement("UPDATE DONOR_RESPONSES SET response_status=?,responded_at=SYSTIMESTAMP,response_notes=? WHERE request_id=? AND donor_id=? AND response_status='PENDING'")){
    p.setString(1,responseStatus);if(responseNotes==null||responseNotes.isBlank())p.setNull(2,Types.VARCHAR);else p.setString(2,responseNotes.trim());p.setInt(3,requestId);p.setInt(4,donorId);updated=p.executeUpdate();
   }
   if(updated==0){
    String insert="INSERT INTO DONOR_RESPONSES(request_id,donor_id,response_status,response_notes) VALUES(?,?,?,?)";
    try(PreparedStatement p=c.prepareStatement(insert)){p.setInt(1,requestId);p.setInt(2,donorId);p.setString(3,responseStatus);if(responseNotes==null||responseNotes.isBlank())p.setNull(4,Types.VARCHAR);else p.setString(4,responseNotes.trim());p.executeUpdate();}
   }
   if("ACCEPTED".equals(responseStatus))try(PreparedStatement p=c.prepareStatement("UPDATE BLOOD_REQUESTS SET request_status='MATCHED',updated_at=SYSTIMESTAMP WHERE request_id=? AND request_status='OPEN'")){p.setInt(1,requestId);if(p.executeUpdate()!=1){c.rollback();return false;}}
   try(PreparedStatement p=c.prepareStatement("UPDATE DONOR_INVITATIONS SET invitation_status=? WHERE request_id=? AND donor_id=? AND invitation_status='SENT'")){p.setString(1,"ACCEPTED".equals(responseStatus)?"ACCEPTED":"DECLINED");p.setInt(2,requestId);p.setInt(3,donorId);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("INSERT INTO NOTIFICATIONS(USER_ID,TITLE,MESSAGE,NOTIFICATION_TYPE,RELATED_REQUEST_ID) VALUES(?,?,?,?,?)")){p.setInt(1,requesterId);p.setString(2,"Donor response received");p.setString(3,"A donor has "+responseStatus.toLowerCase()+" your blood request #"+requestId);p.setString(4,"DONOR_RESPONSE");p.setInt(5,requestId);p.executeUpdate();}
   c.commit();return true;
  }catch(SQLException e){e.printStackTrace();return false;}
 }
}

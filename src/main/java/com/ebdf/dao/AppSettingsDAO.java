package com.ebdf.dao;
import com.ebdf.util.DBConnection;import java.sql.*;
public class AppSettingsDAO {
 public boolean enabled(String key,boolean fallback){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT setting_value FROM APP_SETTINGS WHERE setting_key=?")){p.setString(1,key);try(ResultSet r=p.executeQuery()){return r.next()?Boolean.parseBoolean(r.getString(1)):fallback;}}catch(SQLException e){return fallback;}}
}

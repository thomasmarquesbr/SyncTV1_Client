package application;


import java.sql.Connection; // conexão SQL para Java
import java.sql.DriverManager; // driver de conexão SQL para Java
import java.sql.PreparedStatement;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;


public class ConnectionFactory{
	
	private Connection connection;
	
	public ConnectionFactory(String url, String user, String password) throws SQLException{
		connection = DriverManager.getConnection(url,user,password);
	}
	
	public boolean signUp(String id, String password,int timeZone) throws SQLException{
		//Connection connection = new ConnectionFactory().getConnection();
        String sql = "INSERT INTO `synctvsession`.`session_user` (`id`, `password`, `time_zone`,`lastAccessed`) VALUES ('"+id+"', '"+password+"', '"+timeZone+"','');";
        PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(sql);
        stmt.execute();
        stmt.close();
		
		return true;
	}
	
	public boolean userExist(String id, String password) throws SQLException{
		int n=0;
        Statement s = connection.createStatement();  
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM session_user where id='"+id+"' AND password='"+password+"' ");  
        //ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM session_user where id='samoht' AND password='123456' ");  
        rs.next();  
        n = rs.getInt(1);
        
        if (n>0){
        	System.out.println("id existe");
        	return true;
        }else{
        	System.out.println("id não existe");
        	return false;
        }
	}
	
	public void closeConnection() throws SQLException{
		this.connection.close();
	}

}

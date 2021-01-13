package downloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector {
	private Connection conn;

	/**
	 * Connect to a sample database
	 * 
	 * @throws SQLException
	 */
	public void connect(String dbFile) throws SQLException {
		conn = null;
		try {
			// db parameters
			String url = "jdbc:sqlite:" + dbFile;
			System.out.println(url);
			// create a connection to the database
			conn = DriverManager.getConnection(url);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	public void close() throws SQLException {
		conn.close();
	}

	public ResultSet executeRequest(String sql) {
		try {
			return conn.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
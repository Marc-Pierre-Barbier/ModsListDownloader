package downloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector {
	private Connection conn;

	public void connect(String dbFile) throws SQLException {
		this.conn = null;
		try {
			String url = "jdbc:sqlite:" + dbFile;
			Log.i("DB", url);
			this.conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw e;
		} 
	}

	public void close() throws SQLException {
		this.conn.close();
	}

	public ResultSet executeRequest(String sql) {
		try {
			return this.conn.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} 
	}
}

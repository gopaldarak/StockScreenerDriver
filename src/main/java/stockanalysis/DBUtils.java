package stockanalysis;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

public class DBUtils {
	static PreparedStatement pstmt = null;

	public static PreparedStatement getPstmt() {
		return pstmt;
	}

	public static Connection getConnection() throws Exception {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost:1521:XE";
		String username = "system";
		String password = "manager";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	public static void insertStocks(Connection conn, String type, String name,
			String stock, LocalDate localDate) throws Exception {

		try {
			String exchange = null, my_stock = null;
			String[] stock_arr = stock.split(":");
			if (stock_arr.length == 2) {
				exchange = stock_arr[0];
				my_stock = stock_arr[1];
			} else {
				exchange = "default";
				my_stock = "default";
			}
			// System.out.println("Type:"+type);
			// System.out.println("Name:"+name);
			// System.out.println("Exch:"+exchange);
			// System.out.println("STCK:"+my_stock);
			// System.out.println("DATE:"+localDate);
			String query = "insert into screener values(?, ?, ?, ?, ?)";
			System.out.println("Type:"+type+",name:"+name+",Exchange:"+exchange+",my_stock:"+my_stock);
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, type);
			pstmt.setString(2, name);
			pstmt.setString(3, exchange);
			pstmt.setString(4, my_stock);
			pstmt.setString(5, localDate.toString());
			pstmt.addBatch();
			pstmt.clearParameters();
		} catch (BatchUpdateException e) {
			if (e.getMessage().contains("unique constraint")) {
				System.out.println("Bypass insert for " + type + ":" + name
						+ ":" + stock);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean selectStocks(Connection conn, String type,
			String name, String stock) throws Exception {

		try {
			String exchange = null, my_stock = null;
			String[] stock_arr = stock.split(":");
			if (stock_arr.length == 2) {
				exchange = stock_arr[0];
				my_stock = stock_arr[1];
			} else {
				exchange = "default";
				my_stock = "default";
			}
// Exchange description - https://community.developers.refinitiv.com/storage/attachments/901-exchange-codes.txt			
			if (exchange.contains("OTC") || 
				exchange.contains("OBB") || 
				exchange.contains("ASQ") ||   //Consolidated Issue, listed by AMSE
				exchange.contains("NAQ") ||   //Nasdaq Stock Exchange Consolidated Capital Market
				exchange.contains("NMQ") ||   //NASDAQ Stock Market Exchange Consolidated Large Cap
				exchange.contains("NSQ") ||   //Consolidated Issue Listed on Nasdaq Global Select Market
				exchange.contains("PNK") || 
				my_stock.contains(" ")){
				return true;
			}
			String query = "select * from screener where SCREEN_TYPE like ? and SCREEN_NAME like ? AND EXCHANGE like ? AND STOCK like ?";

			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, "%" + type + "%");
			pstmt.setString(2, "%" + name + "%");
			pstmt.setString(3, "%" + exchange + "%");
			pstmt.setString(4, "%" + my_stock + "%");
			ResultSet result = pstmt.executeQuery();
			if (result.next()) {
				return true;
			} else {
				return false;
			}

		} catch (BatchUpdateException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	public static boolean refreshStocks(Connection conn) throws Exception {
		ArrayList<String> al = new ArrayList();
		try {
			String query = "delete from system.screener";
			pstmt = conn.prepareStatement(query);
			System.out.println("Total number of stocks deleted " + pstmt.executeUpdate());
			return true;
		} catch (BatchUpdateException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}

	}
}

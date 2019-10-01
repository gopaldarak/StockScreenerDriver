package stockanalysis;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StockopediaParserDirect {
	
	// public static final Logger LOGGER = Logger.getLogger("my.logger");
	public static void getStocksFromScreen(String url, String file)
			throws Exception {
		Document doc;
		Connection conn = null;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		LocalDate localDate = LocalDate.now();
		try {
			conn = DBUtils.getConnection();

			String input = GrabHTML.getURLSource(url);
			/* load it */
			doc = Jsoup.parse(input);
			String title = doc.title();
			/* Use this for loading last stock corresponding to alt id */
			ArrayList<String> stockForLastAlt = new ArrayList<String>();

			/* get table from source */
			Element table = doc.select("table").last();
			// System.out.println(table);

			/* get all rows from table */
			Elements rows = table.select("tr");

			for (Element row : rows) {
				/* get data from each row */
				Elements cols = row.select("td");

				/* 0th index will not have relevant details so get 1st index */
				// System.out.println(row);
				if (cols.size() > 0) {
					Document stockId = Jsoup.parse(cols.first().select("label")
							.attr("for"));

					// String stockId = doc2.select("label").attr("for");
					if (!stockId.body().text().isEmpty()) {
						stockForLastAlt.add(stockId.body().text());
					}

					if (cols.last().select("img").attr("alt")
							.contentEquals("United States")) {
						Document doc1 = Jsoup.parse(cols.last().text());
						String lastStock = stockForLastAlt.get(stockForLastAlt
								.size() - 1);
						if (!DBUtils
								.selectStocks(conn, file, title,
										lastStock)) {
							DBUtils.insertStocks(
									conn,
									file,
									title,
									lastStock,
									localDate);
							DBUtils.getPstmt().executeBatch();

						}

					}

				}
			}
			conn.commit();
		} catch (BatchUpdateException ex) {
			ex.printStackTrace();
		} finally {
			if (DBUtils.getPstmt() != null) {
				DBUtils.getPstmt().executeBatch();
//				DBUtils.getPstmt().close();
				conn.commit();
			}
			if (conn != null)
				conn.close();
		}

	}
	
	public static void refreshScreen()
			throws Exception {
		Connection conn = null;

		try {
			conn = DBUtils.getConnection();
            if (DBUtils.refreshStocks(conn)){
				System.out.println("Refresh screens");
				}
			conn.commit();
		} catch (BatchUpdateException ex) {
			ex.printStackTrace();
		} finally {
			if (DBUtils.getPstmt() != null) {
				conn.commit();
			}
			if (conn != null)
				conn.close();
		}

	}
}

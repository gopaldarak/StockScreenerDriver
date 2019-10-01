package stockanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

public class Driver {
	private static final String FILENAME = "src\\main\\resources\\output.txt";
	public static void main(String[] args) throws Exception {
		StockopediaParserDirect.refreshScreen();
		File file = new File("src//main//resources//");

		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".txt")) {
					return true;
				} else {
					return false;
				}
			}
		};
		File allFiles[] = file.listFiles(textFilter);

		for (File f : allFiles) {
			System.out.println("Processing File:" + f);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String st;
			while ((st = br.readLine()) != null)
				if (!st.startsWith("--https:"))
				StockopediaParserDirect.getStocksFromScreen(st,f.getName());
		}

	}
}

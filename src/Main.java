import java.util.List;

public class Main {
	public static void main(String[] args) {
		String filename = "C:\\Users\\Akash\\Documents\\GitHub\\BreakDown_Java\\asset\\demo.veg";
		StringBuffer data;
		MyFile my_file;

		try {
			my_file = new MyFile(filename);
			data = my_file.get_data();

			String eaten_string = Util.eat_spaces(data.toString());
			data = new StringBuffer(eaten_string);

			System.out.println("data: " + data);
			System.out.println("Size: " + data.length());
			System.out.println();

			// Process character by character ....
			BreakDown_1 b = new BreakDown_1(data.toString());
			List<String> bd_1_li = b.get_broken_string();

			System.out.println("-----START-----");
			for(String s: bd_1_li) {
				System.out.println(s);
			}
			System.out.println("-----END-----");
			System.out.println();

			Info info = new Info();
			for(String s: bd_1_li) {
				StatementInfo stat_info = Util.get_stat_info_from_broken_str(s);
				if(stat_info != null)
					info.add(stat_info);
			}

			info.process();

		}
		catch (Exception e) {
			System.out.println("Exception @ class Main: ");
			System.out.println("What: " + e);
		}
	}
}

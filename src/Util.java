class Util {

    // @Redundant: Can be made into a single method
    static String eat_spaces(String str) {
        String tmp = "";
        int quotes_count = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(c == '\"')
                quotes_count += 1;

            if(quotes_count % 2 != 0)
                tmp += c;
            else if(c != '\t' && c != ' ' && c != '\n')
                tmp += c;
        }

        return tmp;
    }

    // @Redundant: Can be made into a single method
    static String eat_only_spaces(String str)  {
        String tmp = "";

        int quotes_count = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(c == '\"')
                quotes_count += 1;

            if(quotes_count % 2 != 0)
                tmp += c;
            else if(c != '\t' && c != ' ')
                tmp += c;
        }

        return tmp;
    }

    // Example of pattern: "true, false, :=, ::struct"
    static String get_first_occuring_pattern(String str, int from_index, String pat) {
        String first_occurring_pattern = "";
        String new_str = str.substring(from_index);
        String[] patterns = pat.split(",");
        int first_occuring_index = 100000; // Sufficiently max.

        for(String s: patterns) {
            int index = new_str.indexOf(s);
            if(index != -1 && index < first_occuring_index) {
                first_occurring_pattern = s;
                first_occuring_index = index;
            }
        }

        return first_occurring_pattern;
    }

    // Example of pattern: "true, false, :=, ::struct"
    static int get_first_index_of(String str, int from_index, String pat) {
        String new_str = str.substring(from_index);
        String[] patterns = pat.split(",");
        int first_occurring_index = 100000; // Sufficiently max.

        for(String s: patterns) {
            int index = new_str.indexOf(s);
            if(index != -1 && index < first_occurring_index) {
                first_occurring_index = index;
            }
        }

        return first_occurring_index;
    }
}

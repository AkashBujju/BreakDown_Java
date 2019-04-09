import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InfixToPostFix {
	private static int Precedence(String str) {
		switch (str) {
			case "=":
				return 1;
			case "||":
				return  2;
			case "&&":
				return 3;
			case "|":
				return 4;
			case "^":
				return 5;
			case "&":
				return 6;
			case "==":
			case "!=":
				return 7;
			case "<":
			case "<=":
			case ">":
			case ">=":
				return 8;
			case ">>>":
			case "<<<":
				return 9;
			case "+":
			case "-":
				return 10;
			case "*":
			case "/":
			case "%":
				return 11;
			case "!":
			case ">>":
			case "<<":
				return 12;

				// @Incomplete Maybe.
		}
		return -1;
	}

	static boolean is_str_digit_or_var_or_func_call(String str) {
        if(str.length() == 0)
            return false;

        char first_char = str.charAt(0);
        if(!Util.is_char_alpha_digit_underscore(first_char))
            return false;

        return true;
    }

	public static List<String> infixToPostFix(List<String> exp) {
		List<String> result = new ArrayList<>();
		Stack<String> stack = new Stack<>();

		/*
		System.out.println("exp: " + exp);
		System.out.println("-------------------");
		System.out.println();
		*/

		for(String s: exp) {
			if(is_str_digit_or_var_or_func_call(s)) {
				result.add(s);
			}
			else if(s.equals("(")) {
				stack.push(s);
			}
			else if(s.equals(")")) {
				while (!stack.isEmpty() && !stack.peek().equals("("))
					result.add(stack.pop());

				if (!stack.isEmpty() && !stack.peek().equals("("))
					return new ArrayList<>();
				else
					stack.pop();
			}
			else {
				// @Note: Changed <= to <
				while (!stack.isEmpty() && Precedence(s) < Precedence(stack.peek())) {
					String str = stack.pop();
					result.add(str);
				}
				stack.push(s);
			}
		}

		while(!stack.empty()) {
			String str = stack.pop();
			result.add(str);
		}

		return result;
	}
}

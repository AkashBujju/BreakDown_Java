import java.util.List;
import java.util.ArrayList;

enum InfoType {
	STRUCT, ENUM, FUNCTION, VAR_DECL, VAR_ASSIGN, EXPRESSION, IF, ELSE_IF, ELSE, WHILE, NONE, OTHER, RETURN
}

public abstract class Info {
	InfoType info_type;

	abstract void display();
}

class IfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	void display() {
		System.out.println("IF");
		System.out.println(" Exp: " + exp);

		for(Info i: infos)
			i.display();

		System.out.println();
	}
}

class ElseInfo extends Info {
	List<Info> infos = new ArrayList<>();

	void display() {
		System.out.println("ELSE");
		for(Info i: infos)
			i.display();

		System.out.println();
	}
}

class ElseIfInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	void display() {
		System.out.println("ELSE");
		System.out.println(" Exp: " + exp);
		for(Info i: infos)
			i.display();

		System.out.println();
	}
}

class ReturnInfo extends Info {
	String exp;
	int line_number = -1;

	void display() {
		System.out.println("RETURN: " + exp);

		System.out.println();
	}
}

class WhileInfo extends Info {
	String exp;
	int exp_line_number = -1;
	List<Info> infos = new ArrayList<>();

	void display() {
		System.out.println("WHILE");
		System.out.println(" Exp: " + exp);
		for(Info i: infos)
			i.display();

		System.out.println();
	}
}

class OtherInfo extends Info {
	String str;
	int line_number = -1;

	void display() {
		System.out.println("OTHER: " + str);

		System.out.println();
	}
}

class VarDeclInfo extends Info {
	String name;
	String type;
	String raw_value;
	int line_number = -1;
	// @Incomplete: What about scope ????
	//
	void display() {
		System.out.println("VAR_DECLARE");
		System.out.println(" Name: " + name);
		System.out.println(" Type: " + type);
		System.out.println(" Raw_Value: " + raw_value);

		System.out.println();
	}
}

class UseInfo extends Info {
	String filename;
	int line_number = -1;

	void display() {
		System.out.println("USE");
		System.out.println(" Filename: " + filename);

		System.out.println();
	}
}

class VarAssignInfo extends Info {
	String var_name;
	String raw_value;
	int line_number = -1;

	void display() {
		System.out.println("VAR_ASSIGN");
		System.out.println(" Name: " + var_name);
		System.out.println(" Raw_Value: " + raw_value);

		System.out.println();
	}
}

class StructInfo extends Info {
	String name;
	int name_line_number = -1;
	List<VarDeclInfo> var_decl_infos = new ArrayList<>();

	void display() {
		System.out.println("STRUCT");
		System.out.println(" Name: " + name);
		for(Info i: var_decl_infos)
			i.display();	

		System.out.println();
	}
}

class EnumInfo extends Info {
	String name;
	int name_line_number = -1;
	List<String> values = new ArrayList<>();

	void display() {
		System.out.println("ENUM");
		for(String s: values) {
			System.out.println(" " + s);
		}

		System.out.println();
	}
}

class FunctionInfo extends Info {
	String name;
	String return_type;
	List<String> var_names = new ArrayList<>();
	List<String> var_types = new ArrayList<>();
	List<Info> infos = new ArrayList<>();

	int signature_line_number = -1;

	void display() {
		System.out.println("FUNCTION");
		System.out.println(" Name: " + name);
		System.out.println(" Return Type: " + return_type);

		System.out.println(" Arguments: ");
		for(int i = 0; i < var_names.size(); ++i) {
			System.out.println(" " + var_names.get(i) + ": " + var_types.get(i));
		}

		for(Info i: infos)
			i.display();

		System.out.println();
	}
}

class ExpInfo extends Info {
	String exp;
	int line_number = -1;

	void display() {
		System.out.println("EXPRESSION");
		System.out.println(" Exp: " + exp);

		System.out.println();
	}
}

package variableCounterFast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class singleFlow {
	public static VariableCounter16 c;
	public static void main(String[] args) throws FileNotFoundException {
		c = new VariableCounter16();
		PrintWriter pw = new PrintWriter(new File(GeneralUtil.path+"CM\\size\\variablecounter16"));
		for (int i=1; i<30000; i++) {
			c.increaseValue(-1, 0);
			
			pw.println(-i+"\t"+c.getValue(0));
			
		}
		pw.close();
		GeneralUtil.analyzeAccuracy(GeneralUtil.path+"CM\\size\\variablecounter16");
	}
}

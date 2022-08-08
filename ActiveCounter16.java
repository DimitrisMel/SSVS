package virtualActiveCounter;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
/** The implentation of Active counter */
public class ActiveCounter16  {
	/** parameters for counter */
	public static String name = "VariableCounter";							// counter data structure name
											// the number of counters in the counter array					
	public BitSet  C;
	public VariableCounter16() {
	
		C = new BitSet(8);
	}
	
	public  int getBitSetValue(int start, int end) {
		int res = 0;
		for (int i = start; i <= end; i++) {
			if (C.get(i)) res += Math.pow(2, i-start);
		}
		return res;
	}

	public  int getValue(int index) {
		
			int exp = getBitSetValue(4,7);
			int absV = getBitSetValue(0,3);
			 return (int)(absV * Math.pow(2, exp)+Math.pow(2, 4+exp)-16);
	}
	
	public void increaseExp(int delta) {
		int i =4;
		while (C.get(i) && i<=7) {
			C.clear(i);
			i++;
		}
		if (i<=7) {
			C.set(i);
		}
		else {
			System.out.println("ERROR11----- out of range");
		}
	}
	public void increaseValue(int delta) {
		
		if (delta==1 ) {
			int i = 0 ;
			while (C.get(i) && i<=3) {
				C.clear(i);
				i++;
			}
			if (i <=3) {
				C.set(i);
			}
			else {
				
				increaseExp(1);
			}
		}
		
	}
	public void increaseValue(int delta, int index) {
		int exp = getBitSetValue(4,7);
		int expexp = (int)Math.pow(2, exp);
		Random rand = new Random();
		if (rand.nextInt(expexp)==0)
					increaseValue(delta);
			
	}
}

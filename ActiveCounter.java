package activeCounter;
import java.util.BitSet;
import java.util.Random;
/** The implentation of active counter */
public class ActiveCounter  {
	/** parameters for counter */
	public static String name = "ActiveCounter";							// counter data structure name
											// the number of counters in the counter array
								
	public BitSet  C;
	public VariableCounter16() {
	
		C = new BitSet(16);
	}
	
	public  int getBitSetValue(int start, int end) {
		int res = 0;
		for (int i = start; i <= end; i++) {
			if (C.get(i)) res += Math.pow(2, i-start);
		}
		return res;
	}

	
	public  int getValue(int index) {
		
			int exp = getBitSetValue(11,15);
			int absV = getBitSetValue(1,10);
			if (C.get(0)) return (int)(absV * Math.pow(2, exp));
			else return -(int)(absV * Math.pow(2, exp));

	}
	
	public void increaseExp(int delta) {
		int i =11;
		while (C.get(i) && i<=15) {
			C.clear(i);
			i++;
		}
		if (i<=15) {
			C.set(i);
		}
		else {
			System.out.println("ERROR11----- out of range");
		}
	}
	public void increaseValue(int delta) {
		
		if (delta==1 && C.get(0)) {
			int i = 1 ;
			while (C.get(i) && i<=10) {
				C.clear(i);
				i++;
			}
			if (i <=10) {
				C.set(i);
			}
			else {
				C.set(10);
				increaseExp(1);
			}
		}
		else {
			if (delta==-1 && !C.get(0)) {
				int i = 1 ;
				while (C.get(i) && i<=10) {
					C.clear(i);
					i++;
				}
				if (i <=10) {
					C.set(i);
				}
				else {
					C.set(10);//System.out.println("---"+getValue(0));
					increaseExp(1);//System.out.println("after "+getValue(0));
				}
			}
			else {
				if (delta==1 && !C.get(0)) {
					int i = 1;
					while (!C.get(i) && i<=10) {
						C.set(i);
						i++;
					}
					if (i <=10) {
						C.clear(i);
					}
					else {
						C.set(0);
						C.set(1);
						for (i=2; i<=10; i++)
							C.clear(i);
					}
				}
				else {
					int i = 1;
					while (!C.get(i) && i<=10) {
						C.set(i);
						i++;
					}
					if (i <=10) {
						C.clear(i);
					}
					else {
						C.clear(0);
						C.set(1);
						for (i=2; i<=10; i++)
							C.clear(i);
					}
				}
			}
		}
	}
	public void increaseValue(int delta, int index) {
		int exp = getBitSetValue(11,15);
		int expexp = (int)Math.pow(2, exp);
		Random rand = new Random();
		if (rand.nextInt(expexp)==0)
			increaseValue(delta);
	}
}

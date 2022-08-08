import java.util.BitSet;
/** The implementation of Self Adjusting Counter */
public class SAC64  {
	/** parameters for counter */
	public static String name = "VariableCounter";							// counter data structure name
											// the number of counters in the counter array					
	public BitSet I, C;
	public SAC64() {		
		I = new BitSet(4);
		C = new BitSet(32);
	}
	public  int getBitSetValue(BitSet b) {
		int res = 0;
		for (int i = 0; i < b.length(); i++) {
			if (b.get(i)) res += Math.pow(2, i);
		}
		return res;
	}
	public  int getBitSetValue(int start, int end) {
		int res = 0;
		for (int i = start; i <= end; i++) {
			if (C.get(i)) res += Math.pow(2, i-start);
		}
		return res;
	}
	
	public  void setByteValue(int value, int index) {
		if (value<0) C.clear(index*8);
		else C.set(index*8);
		int i=1;
		int tempV = Math.abs(value);
		while  ( i<=7) {
			if ((tempV & 1) !=0)
				C.set(i+index*8);
			else 
				C.clear(i+index*8);
			tempV = tempV >>> 1;
			i++;
		}
	}
	public void setShortValue(int value, int index) {
		if (value<0) C.clear(0+index*16);
		else C.set(0+index*16);
		int i=1;
		int tempV = Math.abs(value);
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i+index*16);
			else 
				C.clear(i+index*16);
			tempV = tempV >>> 1;
			i++;
		}
	}
	public void setIntegerValue(int value) {
		if (value<0) C.clear(0);
		else C.set(06);
		int i=1;
		int tempV = Math.abs(value);
		while  (i<=31) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
	}
	
	
	public  int getValue(int index) {
		if (I.get(1)) {
			int value = getBitSetValue(1,31);
			if (!C.get(0))
				value = -value;
			
			return value;
		}
		else {
			if (I.get((index/2)*2))
			{
				int value = getBitSetValue(1+(index/2)*16, 15+(index/2)*16);
				if (!C.get((index/2)*16))
					value = -value;
				return value;
			}
			else {
				int value = getBitSetValue(1+(index)*8, 7+(index)*8);
				if (!C.get(index * 8))
					value = -value;
				return value;
			}
		}
		
	}
	public void increaseByteByDelta(int delta, int index) {
		if (delta==1 && C.get(index * 8)) {
			int i = 1 + index * 8;
			while (C.get(i) && i<=7 +index * 8) {
				C.clear(i);
				i++;
			}
			if (i <=7+index * 8) {
				C.set(i);
			}
			else {
				setShortValue(getValue(index/2*2+ 1-index%2)+128, index/2);
				I.set((index/2)*2);
			}
		}
		else {
			if (delta==-1 && !C.get(index * 8)) {
				int i = 1 + index * 8;
				while (C.get(i) && i<=7 +index * 8) {
					C.clear(i);
					i++;
				}
				if (i <=7+index * 8) {
					C.set(i);
				}
				else {
					setShortValue(getValue(index/2*2+ 1-index%2)-128, index/2);
					I.set((index/2)*2);
				}
			}
			else {
				if (delta==1 && !C.get(index * 8)) {
					int i = 1 + index * 8;
					while (!C.get(i) && i<=7 +index * 8) {
						C.set(i);
						i++;
					}
					if (i <=7+index * 8) {
						C.clear(i);
					}
					else {
						C.set(index * 8);
						C.set(1+index * 8);
						for (i=2+index*8; i<=7+index*8; i++)
							C.clear(i);
					}
				}
				else {
					int i = 1 + index * 8;
					while (!C.get(i) && i<=7 +index * 8) {
						C.set(i);
						i++;
					}
					if (i <=7+index * 8) {
						C.clear(i);
					}
					else {
						C.clear(index * 8);
						C.set(1+index * 8);
						for (i=2+index*8; i<=7+index*8; i++)
							C.clear(i);
					}
				}
			}
		}
	}
	public void increaseShortByDelta(int delta, int index) {
		if (delta==1 && C.get(0+index*16)) {
			int i = 1+index*16 ;
			while (C.get(i) && i<=15+index*16) {
				C.clear(i);
				i++;
			}
			if (i <=15+index*16) {
				C.set(i);
			}
			else {
				
				I.set(1);
				setIntegerValue(32768);
			}
		}
		else {
			if (delta==-1 && !C.get(0+index*16)) {
				int i = 1+index*16;
				while (C.get(i) && i<=15+index*16) {
					C.clear(i);
					i++;
				}
				if (i <=15+index*16) {
					C.set(i);
				}
				else {
					I.set(1);
					setIntegerValue(-32768);
				}
			}
			else {
				if (delta==1 && !C.get(0+index*16)) {
					int i = 1+index*16;
					while (!C.get(i) && i<=15+index*16) {
						C.set(i);
						i++;
					}
					if (i <=15+index*16) {
						C.clear(i);
					}
					else {
						C.set(0);
						C.set(1);
						for (i=2+index*16; i<=15+index*16; i++)
							C.clear(i);
					}
				}
				else {
					int i = 1+index*16;
					while (!C.get(i) && i<=15+index*16) {
						C.set(i);
						i++;
					}
					if (i <=15+index*16) {
						C.clear(i);
					}
					else {
						C.clear(0);
						C.set(1);
						for (i=2+index*16; i<=15+index*16; i++)
							C.clear(i);
					}
				}
			}
		}
	}
	public void increaseValue(int delta, int index) {
		if (I.get(1)) {
			int value = getBitSetValue(1,31);
			if (!C.get(0))
				value = -value;
			value += delta;
			setIntegerValue(value);
		}
		else {
			if (I.get((index/2)*2))
			{
				increaseShortByDelta(delta, index/2);
			}
			else {
				increaseByteByDelta(delta, index);

			}
		}
	}
}

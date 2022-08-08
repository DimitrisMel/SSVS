import java.util.BitSet;
import java.util.Random;
/** The implementation of Variable counter */
public class VariableCounter {
	/** parameters for counter */
	public static String name = "VariableCounter";							// counter data structure name
											// the number of counters in the counter array
	public  int MAX_ABS_VALUE;							
	public BitSet I, C;
	public VariableCounter() {
		
		MAX_ABS_VALUE =(int) Math.pow(2, 7)-1;
		
		I = new BitSet(2);
		C = new BitSet(16);
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
	public  void setIValue(int value) {
		int i = 0;
		while ( i < 2) {
			if ((value & 1) != 0) {
				I.set(i);
			} else {
				I.clear(i);
			}
			value = value >>> 1;
			i++;
		}
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
	public void setShortValue(int value) {
		if (value<0) C.clear(0);
		else C.set(0);
		int i=1;
		int tempV = Math.abs(value);
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
	}
	public  void shortToSmallAC(int currV) {
		
		if (currV<0) C.clear(3);
		else C.set(3);
		int i=4;
		int tempV = currV/8;
		tempV = Math.abs(tempV);
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
		i = 0;
		int exp = 3;
		while(i<=2) {
			if ((exp & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			exp = exp >>> 1;
			i++;
		}
	}
	public  void increaseSmallAC(int delta) {
		Random ran = new Random();
		int exp = getBitSetValue(0,2);
		int tempV = getBitSetValue(4,15);
		if (!C.get(3)) tempV = -tempV;
		int expexp = (int) Math.pow(2, exp);
		if (ran.nextInt(expexp)==0) { 
			tempV += delta;
			if (exp ==7 && (int)Math.abs(tempV)== (int)(Math.pow(2, 12))) {
				smallToLargeAC();
				setIValue(3);
				setLargeACValue( 10, C.get(5),(int)(Math.pow(2, 9) ));
			}
			else {
				if ((int)Math.abs(tempV)== (int)(Math.pow(2, 12))) {
					setSmallACValue( exp+1, C.get(3),(int)(Math.pow(2, 11) ));
				}
				else {
					setSmallACValue( exp, C.get(3),(int)Math.abs(tempV));

				}
			}
		}
		
		
	}
	public  void increaseLargeAC(int delta) {
		Random ran = new Random();
		int exp = getBitSetValue(0,4);
		int tempV = getBitSetValue(6,15);
		if (!C.get(5)) tempV = -tempV;
		int expexp = (int) Math.pow(2, exp);
		if (ran.nextInt(expexp)==0) { 
			tempV += delta;
			if ((int)Math.abs(tempV)== (int)(Math.pow(2, 10))) {
				setLargeACValue( exp+1, C.get(5),(int)(Math.pow(2, 9) ));
				
			}
			else {
				setLargeACValue( exp, C.get(5),(int)Math.abs(tempV));
			}
		}
		
		
	}
	public  void setSmallACValue(int exp, boolean sign, int tempV) {
		if (sign) C.set(3);
		int i=4;
		
		tempV = Math.abs(tempV);
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
		i = 0;
		
		while(i<=2) {
			if ((exp & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			exp = exp >>> 1;
			i++;
		}
		
	}
	public void setLargeACValue(int exp, boolean sign, int tempV) {
		if (sign) C.set(5);
		int i=6;
		
		tempV = Math.abs(tempV);
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
		i = 0;
		
		while(i<=4) {
			if ((exp & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			exp = exp >>> 1;
			i++;
		}
		
	}
	public void smallToLargeAC() {
		int exp = getBitSetValue(0,2);
		int tempV = getBitSetValue(4,15);
		boolean sign = C.get(3);
		exp += 2;
		tempV = tempV>>>2;
		if (sign) C.set(5);
		else C.clear(5);
		int i=6;
	
		while  (i<=15) {
			if ((tempV & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			tempV = tempV >>> 1;
			i++;
		}
		i = 0;
		
		while(i<=4) {
			if ((exp & 1) !=0)
				C.set(i);
			else 
				C.clear(i);
			exp = exp >>> 1;
			i++;
		}
	}
	public  int getValue(int index) {
		int IValue = getBitSetValue(I);
		if (IValue ==0) {
			int absV = getBitSetValue(1+index*8, 7 +index *8);
			if (C.get(index*8)) return absV;
			else return -absV;
		}
		if (IValue ==1) {
			int absV = getBitSetValue(1, 15);
			if (C.get(0)) return absV;
			else return -absV;
		}
		if (IValue ==2) {
			int exp = getBitSetValue(0,2);
			int absV = getBitSetValue(4,15);
			if (C.get(3)) return (int)(absV * Math.pow(2, exp));
			else return -(int)(absV * Math.pow(2, exp));
		}
		if (IValue ==3) {
			int exp = getBitSetValue(0,4);
			int absV = getBitSetValue(6,15);
			if (C.get(5)) return (int)(absV * Math.pow(2, exp));
			else return -(int)(absV * Math.pow(2, exp));
		}
		return 0;
		
	}
	public void handleOverflow(int currV,  int index) {
		if (index ==0) {
			setShortValue(getValue(0)+getValue(1));
			setIValue(1);
			MAX_ABS_VALUE = (int)(Math.pow(2, 15)-1);
		}
		else {
			if (index ==1) {
				MAX_ABS_VALUE = (int) ((Math.pow(2, 12)-1) *Math.pow(2, 7));
				setIValue(2);
				
				shortToSmallAC(currV);
			}else {
				MAX_ABS_VALUE = (int) ((Math.pow(2, 41)-1));
				setIValue(2);
				smallToLargeAC();
			}
		}
	}
	
	
	public int getI() {
		return getBitSetValue(I);
	}
	public boolean byteOverflow(int index) {
		 for (int i = 7 + index * 8; i> index *8 ; i--) {
			  if (!C.get(i)) return false;
		 }
		 return true;
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
				setShortValue(getValue(1-index)+128);
				setIValue(1);
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
					setShortValue(getValue(1-index)-128);
					setIValue(1);
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
	public void increaseShortByDelta(int delta) {
		if (delta==1 && C.get(0)) {
			int i = 1 ;
			while (C.get(i) && i<=15) {
				C.clear(i);
				i++;
			}
			if (i <=15) {
				C.set(i);
			}
			else {
				
				setIValue(2);
				setSmallACValue(4,true,2048 );
			}
		}
		else {
			if (delta==-1 && !C.get(0)) {
				int i = 1 ;
				while (C.get(i) && i<=15) {
					C.clear(i);
					i++;
				}
				if (i <=15) {
					C.set(i);
				}
				else {
					setIValue(2);
					setSmallACValue(4,false,2048 );
				}
			}
			else {
				if (delta==1 && !C.get(0)) {
					int i = 1;
					while (!C.get(i) && i<=15) {
						C.set(i);
						i++;
					}
					if (i <=15) {
						C.clear(i);
					}
					else {
						C.set(0);
						C.set(1);
						for (i=2; i<=15; i++)
							C.clear(i);
					}
				}
				else {
					int i = 1;
					while (!C.get(i) && i<=15) {
						C.set(i);
						i++;
					}
					if (i <=15) {
						C.clear(i);
					}
					else {
						C.clear(0);
						C.set(1);
						for (i=2; i<=15; i++)
							C.clear(i);
					}
				}
			}
		}
	}
	public void increaseValue(int delta, int index) {
		int IValue = getBitSetValue(I);
		
		if (IValue == 0) {
			increaseByteByDelta(delta, index);
		}
		else {
			if (IValue ==1) {
				increaseShortByDelta(delta);
			}
			else {
				if (IValue ==2) {
					increaseSmallAC(delta);
				}
				else
					increaseLargeAC(delta);
			}
		}
	}
}

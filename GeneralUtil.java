// for OVS
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Util for general framework. */
public class GeneralUtil {	
	public static String path = "\\C:\\";
	public static Boolean isDstAsID = true;
	public static HashSet<Integer> set1=new HashSet<Integer>(),set2=new HashSet<Integer>();
	public static String testresult = "\\G:\\";
	/** parameter for estimation on a single source **/
	public static String dataStreamForFlowSize = "\\C:\\";
	public static String dataSummaryForFlowSize = "\\C:\\";
	public static Double throughputSamplingRate = 1.0;
	public static int maxRange = 1000000;
	public static int totalenum=0;
	
	public static ArrayList<Integer> bin = new ArrayList<>(Arrays.asList(10, 100, 1_000, 10_000, 1_000_000));

	public static ArrayList<Integer> smallBin = new ArrayList<>(Arrays.asList(2, 4, 10, 20, 40, 60, 100, 400,500,600,700,900, 1000, 1500));
	
	/** get flow id for size measurement in each row of a file. */
	public static String getSizeFlowID(String[] strs, Boolean isEncoding) {
		if (strs.length == 0) return "";
		else if (isEncoding) return Arrays.stream(strs).collect(Collectors.joining("\t"));
		else return Arrays.stream(Arrays.copyOfRange(strs, 0, strs.length-1)).collect(Collectors.joining("\t"));
	}
	
	public static long getSize1FlowID(String[] strs, Boolean isEncoding) {
		if (strs.length == 0) return 0;
		else if (isEncoding) return ((Long.parseLong(strs[0])<<32)|Long.parseLong(strs[1]));
		else return Long.parseLong(strs[0]);
	}
	
	/** get flow id and element id for spread measurement in each row of a file. */
	public static String[] getSperadFlowIDAndElementID(String[] strs, Boolean isEncoding) {
		String[] res = new String[2];
		if (isEncoding) {
			if (isDstAsID) {res[0] = strs[1]; res[1] = strs[0];}
			else {res[0] = strs[0]; res[1] = strs[1];}
		} else {
			res[0] = strs[0];
		}
		return res;
	}
	
	
	/** get flow size sample rate to reduce the total number of measured flows */
	public static Double getSizeSampleRate(int num) {
		// for small set
		if (num <= 1) return 0.001;
		else if(num <= 100) return 0.005;
		else if (num <= 1000) return 0.1;
		else if (num <= 10000) return 0.5;
		
		return 1.0;
	}
	
	/** get flow spread sample rate to reduce the total number of measured flows */
	public static Double getSpreadSampleRate(int num) {
		return 1.0;
	}
	
	/** analyze estimation error 
	 * @throws FileNotFoundException */
	public static void analyzetopAccuracy(String filePath) throws FileNotFoundException {
		int topnumber=1000;
		
		Scanner sc = new Scanner(new File(filePath));
		int[] s=new int[topnumber];
		int[] shat=new int[topnumber];
		int pos=0;
		while (sc.hasNextLine()&&pos<topnumber) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\t");			
			s[pos] = Integer.parseInt(strs[strs.length-2]);			//true value
			shat[pos] = Integer.parseInt(strs[strs.length-1]);		//estimated value
			if(s[pos]<4096) break;
			pos++;
		}
		sc.close();
		topnumber=pos;

		int tote=0,tots=0;
		double totp=0.0;
		for(int j = 0; j < topnumber; j++) {
				int tt=Math.abs(s[j]-shat[j]);
				tots+=s[j];
				tote+=tt;
				totp+=(double)tt/(double)s[j];	
				
		}
		
		System.out.println(filePath + ":\t" +topnumber+"\t"+tots+"\t"+ (double)tote/(double)tots + "\t" + totp/(double)topnumber + "\n");
	}
	
	/** analyze estimation bias and error 
	 * @throws FileNotFoundException */
	public static void analyzeAccuracy(String filePath) throws FileNotFoundException {
		//initBin();
		analyzeSmallAccuracy(filePath);
		int binLen = bin.size();
		
		System.out.println("bin: " + binLen);
		double[] relerr = new double[binLen];				//relative bias

		double[] bias = new double[binLen];				//relative bias
		double[] stderr = new double[binLen];			//relative standard errorAbs
		double[] abserr = new double[binLen];			//absolute error
		double[] maxabserr = new double[binLen];			//max absolute error
		double[] maxrelerr = new double[binLen];			//max absolute error

		double[] avg = new double[binLen];				//average of the true sizes in each bin
		double totalBias = 0.0;
		double totalRelativeBias = 0.0;
		int[] num = new int[binLen];					//number of flows in each bin
		int totalNum = 0;
		
		PrintWriter pw1 = new PrintWriter(new File(filePath + "_Bias.txt"));
		PrintWriter pw2 = new PrintWriter(new File(filePath + "_Standard_Error.txt"));
		PrintWriter pw3 = new PrintWriter(new File(filePath + "_abs_Error.txt"));
		PrintWriter pw4 = new PrintWriter(new File(filePath + "_total_abs_Error.txt"));
		PrintWriter pw5 = new PrintWriter(new File(filePath + "_total_relative_Error.txt"));
		PrintWriter pw6 = new PrintWriter(new File(filePath + "_max_abs_Error.txt"));
		PrintWriter pw7 = new PrintWriter(new File(filePath + "_relative_Error.txt"));
		PrintWriter pw8 = new PrintWriter(new File(filePath + "_max_relative_Error.txt"));

		Scanner sc = new Scanner(new File(filePath));
					DecimalFormat df = new DecimalFormat("#0.00000");
					System.out.print("go to analyze accuracy");
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\t");
						
			int s = Integer.parseInt(strs[strs.length-2]);			//true value
			int shat = Integer.parseInt(strs[strs.length-1]);		//estimated value
			int binIndex = getBinIndex(s);
			avg[binIndex] += s;
			num[binIndex]++;
			totalNum++;
			relerr[binIndex] +=1.0*Math.abs(shat - s) / s;


			bias[binIndex] += 1.0*(shat - s) / s;
			abserr[binIndex] += Math.abs(shat-s);
			
			maxabserr[binIndex] = Math.max(maxabserr[binIndex] , Math.abs(shat-s));
			
			totalBias += Math.abs(shat-s);
			totalRelativeBias +=Math.abs(shat-s)/s;
			maxrelerr[binIndex] = Math.max(maxrelerr[binIndex],1.0*Math.abs(shat-s)/s);
			stderr[binIndex] += (1.0 * shat / s - 1) *  (1.0 * shat / s - 1);
		}
		sc.close();
		
		for(int j = 0; j < binLen; j++) {
			if(num[j] != 0) {
				relerr[j] /=num[j];

				avg[j] /= num[j];
				bias[j] /= num[j];
				abserr[j] /= num[j];
				stderr[j] /= num[j];
				stderr[j] = Math.sqrt(stderr[j]);
				
				pw1.println(bin.get(j) + "\t" + df.format(avg[j]) + "\t" + num[j]+ "\t" +df.format(bias[j]));
				pw2.println(bin.get(j) + "\t" + avg[j] + "\t" + num[j]+ "\t" + + stderr[j]);
				pw3.println(bin.get(j) + "\t" + df.format(avg[j]) + "\t" + num[j]+ "\t" + df.format(abserr[j]));
				pw6.println(bin.get(j) + "\t" + df.format(avg[j]) + "\t" + num[j]+ "\t" + df.format(maxabserr[j]));
				pw7.println(bin.get(j) + "\t" + df.format(avg[j]) + "\t" + num[j]+ "\t" + df.format(relerr[j]));
				pw8.println(bin.get(j) + "\t" + df.format(avg[j]) + "\t" + num[j]+ "\t" + df.format(maxrelerr[j]));


			}
		}
		pw4.println("total number " + totalNum + "\t total abs error" + totalBias/totalNum);
		pw5.println("total number " + totalNum + "\t total relative" + totalRelativeBias/totalNum);

		System.out.println(filePath + ":\t" + totalBias/totalNum);
		pw1.close();
		pw2.close();
		pw3.close();
		pw4.close();
		pw5.close();
		pw6.close();
		pw7.close();
		pw8.close();
	}
	public static void analyzeSmallAccuracy(String filePath) throws FileNotFoundException {
		int binLen = smallBin.size()+1;
		
		System.out.println("smallBin: " + binLen);
		
		double[] bias = new double[binLen];				//relative bias
		double[] stderr = new double[binLen];			//relative standard errorAbs
		double[] abserr = new double[binLen];			//absolute error
		double[] avg = new double[binLen];				//average of the true sizes in each bin
		int[] num = new int[binLen];					//number of flows in each bin
		
		PrintWriter pw1 = new PrintWriter(new File(filePath + "_Small_Bias"));
		PrintWriter pw2 = new PrintWriter(new File(filePath + "_Small_Standard_Error"));
		PrintWriter pw3 = new PrintWriter(new File(filePath + "_Small_abs_Error"));
		Scanner sc = new Scanner(new File(filePath));
		
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\t");
						
			int s = Integer.parseInt(strs[strs.length-2]);			//true value
			int shat = Integer.parseInt(strs[strs.length-1]);		//estimated value
			int binIndex = getSmallBinIndex(s);
							
			avg[binIndex] += s;
			num[binIndex]++;


			bias[binIndex] += 1.0*(shat - s) / s;
			abserr[binIndex] += Math.abs(shat-s);
			stderr[binIndex] += (1.0 * shat / s - 1) *  (1.0 * shat / s - 1);
		}
		sc.close();
		
		for(int j = 0; j < binLen-1; j++) {
			if(num[j] != 0) {
				avg[j] /= num[j];
				bias[j] /= num[j];
				abserr[j] /= num[j];
				stderr[j] /= num[j];
				stderr[j] = Math.sqrt(stderr[j]);
				
				pw1.println(smallBin.get(j) + "\t" + avg[j] + "\t" + num[j]+ "\t" + bias[j]);
				pw2.println(smallBin.get(j) + "\t" + avg[j] + "\t" + num[j]+ "\t" + + stderr[j]);
				pw3.println(smallBin.get(j) + "\t" + avg[j] + "\t" + num[j]+ "\t" + + abserr[j]);
			}
		}
		pw1.close();
		pw2.close();
		pw3.close();
	}
	
	/** a hash function that maps the string value to int value */
	public static int FNVHash1(String data) {
		final int p = 16777619;
		int hash = (int) 2166136261L;
		for (int i = 0; i < data.length(); i++)
			hash = (hash ^ data.charAt(i)) * p;
		hash += hash << 13;
		hash ^= hash >> 7;
		hash += hash << 3;
		hash ^= hash >> 17;
		hash += hash << 5;
		return hash;
	}
	public static int FNVHash1(long key1,long key2) {
		return FNVHash1((key1<<32)|key2);
	}
	public static int FNVHash1(long key) {
		  key = (~key) + (key << 18); // key = (key << 18) - key - 1;
		  key = key ^ (key >>> 31);
		  key = key * 21; // key = (key + (key << 2)) + (key << 4);
		  key = key ^ (key >>> 11);
		  key = key + (key << 6);
		  key = key ^ (key >>> 22);
		  return (int) key;
	}
	
	
	/** Thomas Wang hash */
	public static int intHash(int key) {
		key += ~(key << 15);
		key ^= (key >>> 10);
		key += (key << 3);
		key ^= (key >>> 6);
		key += ~(key << 11);
		key ^= (key >>> 16);
		return key;
	}
	
	private static void initBin() {
		bin.clear();
		int cur = 1;
		while (cur <= maxRange) {
			int gap = (int) Math.pow(10, Math.floor(Math.log10(cur)));
			bin.add(cur);
			cur += gap;
		}
	}
	
	private static int getBinIndex(int val) {
		int l = 0, r = bin.size()-1;
		while (l <= r) {
			int mid = l + (r - l) / 2;
			if (bin.get(mid) == val) return mid;
			if (bin.get(mid) > val) r = mid - 1;
			else l = mid + 1;
		}
		return l;
	}
	
	private static int getSmallBinIndex(int val) {
		int l = 0, r = smallBin.size()-1;
		while (l <= r) {
			int mid = l + (r - l) / 2;
			if (smallBin.get(mid) == val) return mid;
			if (smallBin.get(mid) > val) r = mid - 1;
			else l = mid + 1;
		}
		return l;
	}
}


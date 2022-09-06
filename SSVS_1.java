import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class SSVS_1 {
	public static Random rand = new Random();
	
	public static int n = 0; 						// total number of packets
	public static int flows = 0; 					// total number of flows
	public static int avgAccess = 0; 				// average memory access for each packet
	public static final int M = 1024 * 1024; 	// total memory space in bits	
	public static VariableCounter[][] C;
	public static Set<Integer> sizeMeasurementConfig = new HashSet<>(Arrays.asList(0));
	public static boolean isGetThroughput =false;
	
	/** parameters for count-min */
	public static final int d = 1; 			// the number of rows
	public static int w = 1;				// the number of columns
	public static int u = 1;				// the size of each elementary data structure
	public static int[] S = new int[d];		// random seeds
	public static int m = 1;				// number of bit/register in each unit (used for bitmap, FM sketch and HLL sketch)

	
	/** parameters for counter */
	public static int mValueCounter = 1;			// only one counter in the counter data structure
	public static int counterSize = 18;				// size of each unit

	public static int times = 0;
	
	/** number of runs for throughput measurement */
	public static int loops = 1;
	
	public static void main(String[] args) throws FileNotFoundException {
		/** measurement for flow sizes **/
		if (isGetThroughput) {
			return;
		}
		System.out.println("Start****************************");
		/** measurement for flow sizes **/
		
		for (int i : sizeMeasurementConfig) {
			times = 0;
			initCM(i);
			encodeSize(GeneralUtil.dataStreamForFlowSize);
	        	estimateSize(GeneralUtil.dataSummaryForFlowSize);
	        	times++;
		}
		System.out.println("DONE!****************************");
	}
	
	// Init the Count Min for different elementary data structures.
	public static void initCM(int index) {
		switch (index) {
	        case 0: case -1: C = generateCounter();
	                 break;
	      
	        default: break;
		}
		generateCMRamdonSeeds();
	}
	
	// Generate counter base Counter Min for flow size measurement.
	public static VariableCounter[][] generateCounter() {
		m = mValueCounter;
		u = counterSize * mValueCounter;
		w = (M / u) ;
		VariableCounter[][] B = new VariableCounter[1][w];
		for (int i = 0; i < 1; i++) {
			for (int j = 0; j < w; j++) {
				B[i][j] = new VariableCounter();
			}
		}
		return B;
	}
	
	// Generate random seeds for Counter Min.
	public static void generateCMRamdonSeeds() {
		HashSet<Integer> seeds = new HashSet<Integer>();
		int num = d;
		while (num > 0) {
			int s = rand.nextInt();
			if (!seeds.contains(s)) {
				num--;
				S[num] = s;
				seeds.add(s);
			}
		}
	}

	/** Encode elements to the Count Min for flow size measurement. */
	public static void encodeSize(String filePath) throws FileNotFoundException {
		System.out.println("Encoding elements using " +  "s..." );
		Scanner sc = new Scanner(new File(filePath));
		n = 0;
		
		String[] flowida = new String[60000000];
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			
			String flowid = GeneralUtil.getSizeFlowID(strs, true);
			
			flowida[n]  = flowid;
			n++;
		}
		int ii=0;
		long startTime = System.nanoTime();	
		while(ii<n) {
			String flowid = flowida[ii];
			ii++;
			
				
                	int i = rand.nextInt(d);
                    	int hashV = GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) ;
                    	int delta = 1;
                    	if ((hashV & 1) ==0) delta =-1;
                    	int j = hashV >>>2;
			j =(j % w + w) % w;
                    	if ((hashV&2) ==0) C[0][j].increaseValue(delta, 0);
                    	else C[0][j].increaseValue(delta, 1);
		}
		long endTime = System.nanoTime();
		double duration = 1.0 * (endTime - startTime) ;
		System.out.println("processing time ns : "+duration/n);
		System.out.println("Total number of encoded pakcets: " + n);
		sc.close();
	}

	/** Estimate flow sizes. */
	public static void estimateSize(String filePath) throws FileNotFoundException {
		System.out.println("Estimating Flow SIZEs..." ); 
		Scanner sc = new Scanner(new File(filePath));
		String resultFilePath = GeneralUtil.path + "Results\\SSVS-1_" 
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m + "_w_" + w;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		System.out.println("Result directory: " + resultFilePath);
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			String flowid = GeneralUtil.getSizeFlowID(strs, false);

			int[] value= new int[d];
			if (true) {
				int estimate = 0;
				
				for(int i = 0; i < d; i++) {
					int hashV = GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]);
					int j = hashV>>>2;
					j = (j % w + w) % w;
					int tempV;
					int delta =1;
					if ((hashV & 1) ==0) delta =-1;
					if ( (hashV &2)==0) tempV = delta * C[0][j].getValue(0);
					else tempV = delta * C[0][j].getValue(1);
					estimate += tempV;
					value[i] = tempV;
					
				}
				if (estimate<0) estimate =0;
				pw.println(entry + "\t" + estimate);
			}
		}
		sc.close();
		pw.close();
		// obtain estimation accuracy results
		GeneralUtil.analyzeAccuracy(resultFilePath);
	}
}

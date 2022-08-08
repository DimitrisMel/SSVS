package variableCounterFast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

//import minusHLLestimator.GeneralUtil;


/** A general framework for count min. The elementary data structures to be shared here can be counter, bitmap, FM sketch, HLL sketch. Specifically, we can
 * use counter to estimate flow sizes, and use bitmap, FM sketch and HLL sketch to estimate flow cardinalities
 * @author Jay, Youlin, 2018. 
 */

public class GeneralCS {
	public static Random rand = new Random();
	
	public static int n = 0; 						// total number of packets
	public static int flows = 0; 					// total number of flows
	public static int avgAccess = 0; 				// average memory access for each packet
	public static final int M = 1024 * 1024; 	// total memory space Mbits	
	public static GeneralDataStructure[][] C;
	public static Set<Integer> sizeMeasurementConfig = new HashSet<>(Arrays.asList(0)); // -1-regular CM; 0-enhanced CM; 1-Bitmap; 2-FM sketch; 3-HLL sketch
	public static Set<Integer> spreadMeasurementConfig = new HashSet<>(Arrays.asList()); // 1-Bitmap; 2-FM sketch; 3-HLL sketch
	public static Set<Integer> expConfig = new HashSet<>(Arrays.asList()); //0-ECountMin dist exp
	public static boolean isGetThroughput =false;
	
	/** parameters for count-min */
	public static final int d =4; 			// the number of rows in Count Min
	public static int w = 1;				// the number of columns in Count Min
	public static int u = 1;				// the size of each elementary data structure in Count Min.
	public static int[] S = new int[d];		// random seeds for Count Min
	public static int m = 1;				// number of bit/register in each unit (used for bitmap, FM sketch and HLL sketch)

	
	/** parameters for counter */
	public static int mValueCounter = 1;			// only one counter in the counter data structure
	public static int counterSize = 32;				// size of each unit

	/** parameters for bitmap */
	public static final int bitArrayLength = 5000;
	
	/** parameters for FM sketch **/
	public static int mValueFM = 128;
	public static final int FMsketchSize = 32;
	
	/** parameters for HLL sketch **/
	public static int mValueHLL = 128;
	public static final int HLLSize = 5;

	public static int times = 0;
	
	/** number of runs for throughput measurement */
	public static int loops = 1;
	
	public static void main(String[] args) throws FileNotFoundException {
		/** measurement for flow sizes **/
		if (isGetThroughput) {
			getThroughput();
			return;
		}
		System.out.println("Start****************************");
		/** measurement for flow sizes **/
		
		for (int i : sizeMeasurementConfig) {
			times = 0;
			
				initCM(i);
				//getThroughput();
				encodeSize(GeneralUtil.dataStreamForFlowSize);
				//long endTime = System.nanoTime();
				//double duration = 1.0 * (endTime - startTime) / 1000000000;
				//System.out.println("Average execution time: " + 1.0 * duration / loops + " seconds");
				//System.out.println("Average Throughput: " + 1.0 * n / (duration / loops) + " packets/second" );
	        	estimateSize(GeneralUtil.dataSummaryForFlowSize);
	        	times++;
			
		}
		
		/** measurement for flow spreads **/
		for (int i : spreadMeasurementConfig) {
			initCM(i);
			encodeSpread(GeneralUtil.dataStreamForFlowSpread);
    		estimateSpread(GeneralUtil.dataSummaryForFlowSpread);
		}
		
		/** experiment for specific requirement *
		for (int i : expConfig) {
			switch (i) {
	        case 0:  initCM(0);
					 encodeSize(GeneralUtil.dataStreamForFlowSize);
					 randomEstimate(10000000);
	                 break;
	        default: break;
			}
		}*/
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
		//System.out.println("\nCount Min-" + C[0][0].getDataStructureName() + " Initialized!");
	}
	
	// Generate counter base Counter Min for flow size measurement.
	public static Counter[][] generateCounter() {
		m = mValueCounter;
		u = counterSize * mValueCounter;
		w = (M / u) / d;
		Counter[][] B = new Counter[d][w];
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < w; j++) {
				B[i][j] = new Counter(1, counterSize);
			}
		}
		return B;
	}
	
	// Generate bitmap base Counter Min for flow cardinality measurement.

	
	// Generate FM sketch base Counter Min for flow cardinality measurement.
	
	
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
		System.out.println("Encoding elements using " + C[0][0].getDataStructureName().toUpperCase() + "s..." );
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
			
			
			if (C[0][0].getDataStructureName().equals("Counter")) {
                for (int i = 0; i < d; i++) {
                    int hashV = GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]);
                    int j = hashV >>>1;

                    j = (j % w + w) % w;
                    
                    if (hashV%2 ==0) C[i][j].encodeCS(-1);
                    else C[i][j].encodeCS(1);
                    
                   
                }
               
			} else {
				for (int i = 0; i < d; i++) {
					int j = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) % w + w) % w; 
					C[i][j].encode();
				}
			}
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
		String resultFilePath = GeneralUtil.path + "Results\\CS_"
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m + "_T_" + times;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		System.out.println("Result directory: " + resultFilePath); 
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			String flowid = GeneralUtil.getSizeFlowID(strs, false);
			//System.out.println("num is "+num);
			//if (rand.nextDouble() <= GeneralUtil.getSizeSampleRate(num)) {
			int[] value= new int[d];
			if (true) {
				int estimate = Integer.MAX_VALUE;
				
				for(int i = 0; i < d; i++) {
					int hashV = GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) ;
					int j = hashV >>> 1;
					j = (j % w + w) % w;
					
					value[i] = C[i][j].getValue();
					if (hashV%2 ==0) value[i]  = -value[i];
				}
				Arrays.parallelSort(value);
				estimate = (d%2)==1?(value[(d-1)/2]):(value[d/2]+value[d/2-1])/2;
				if (estimate<0) estimate =0;
				pw.println(entry + "\t" + estimate);
			}
		}
		sc.close();
		pw.close();
		// obtain estimation accuracy results
		GeneralUtil.analyzeAccuracy(resultFilePath);
	}
	public static double getnoise() {
		double res=0.0;
		Random ran=new Random();
		for(int i=0;i<100;i++) {
			int estimate = Integer.MAX_VALUE;
			//int r=ran.nextInt();
			for(int j=0;j<d;j++) {				
				int p=((ran.nextInt()^S[j])%w+w)%w;
				estimate = Math.min(estimate, C[0][p].getValue());
			}
			res+=(double)estimate;
		}
		return res/(double)100;
	}
	/** Estimate flow sizes using random flow ids. */
	public static void randomEstimate(int numOfFlows) throws FileNotFoundException {
		System.out.println("Estimating Flow SIZEs..." ); 
		String resultFilePath = GeneralUtil.path + "Random\\" + C[0][0].getDataStructureName()
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		System.out.println("Result directory: " + resultFilePath); 
		for (int times = 0; times < numOfFlows; times++) {
			String flowid = String.valueOf(rand.nextDouble());
			int estimate = Integer.MAX_VALUE;
			
			for(int i = 0; i < d; i++) {
				int j = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) % w + w) % w;
				estimate = Math.min(estimate, C[i][j].getValue());
			}
			pw.println(flowid + "\t" + estimate);
		}
		pw.close();
	}
	
	/** Encode elements to the Count Min for flow spread measurement. */
	public static void encodeSpread(String filePath) throws FileNotFoundException {
		System.out.println("Encoding elements using " + C[0][0].getDataStructureName().toUpperCase() + "s..." );
		Scanner sc = new Scanner(new File(filePath));
		n = 0;
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			String[] res = GeneralUtil.getSperadFlowIDAndElementID(strs, true);
			String flowid = res[0];
			String elementid = res[1];
			n++;
			for (int i = 0; i < d; i++) {
				int j = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) % w + w) % w;
				C[i][j].encode(elementid);
			}
		}
		System.out.println("Total number of encoded pakcets: " + n); 
		sc.close();
	}

	/** Estimate flow spreads. */
	public static void estimateSpread(String filepath) throws FileNotFoundException {
		System.out.println("Estimating Flow CARDINALITY..." ); 
		Scanner sc = new Scanner(new File(filepath));
		String resultFilePath = GeneralUtil.path + "CM\\spread\\removenoise" + C[0][0].getDataStructureName()
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		System.out.println("Result directory: " + resultFilePath);
		double noise = getnoise();
		System.out.println("the noise is "+noise);
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			String flowid = GeneralUtil.getSperadFlowIDAndElementID(strs, false)[0];
			int num = Integer.parseInt(strs[strs.length-1]);
			// TODO(youzhou): Add sampling mechanism to reduce the computation time.
			if (rand.nextDouble() <= GeneralUtil.getSpreadSampleRate(num)) {
				int estimate = Integer.MAX_VALUE;
				
				for(int i = 0; i < d; i++) {
					int j = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowid) ^ S[i]) % w + w) % w;
					//if (C[i][j].getValue()>10000) System.out.println(C[i][j].getValue());
					estimate = Math.min(estimate, C[i][j].getValue());
				}
				//if( estimate>10000) System.out.println(flowid+"\t"+estimate);
				noise = getnoise();
				estimate -= noise;
				//if( estimate>10000) System.out.println(flowid+"\t"+estimate);

				if (estimate<1) estimate =1;
				pw.println(entry + "\t" + estimate);
			}
		}
		sc.close();
		pw.close();
		// obtain estimation accuracy results
		GeneralUtil.analyzeAccuracy(resultFilePath);
	}
	
	public static void getThroughput() throws FileNotFoundException {
		Scanner sc = new Scanner(new File(GeneralUtil.dataSummaryForFlowSpread));
		ArrayList<Integer> dataFlowID = new ArrayList<Integer> ();
		ArrayList<Integer> dataElemID = new ArrayList<Integer> ();
		n = 0;
		if (sizeMeasurementConfig.size() > 0) {
			while (sc.hasNextLine()) {
				String entry = sc.nextLine();				
				String[] strs = entry.split("\\s+");
				dataFlowID.add(GeneralUtil.FNVHash1(entry));
				dataElemID.add(GeneralUtil.FNVHash1(strs[1]));
				n++;
			}
			sc.close();
		} else {
			while (sc.hasNextLine()) {
				String entry = sc.nextLine();				
				String[] strs = entry.split("\\s+");
				dataFlowID.add(GeneralUtil.FNVHash1(strs[0]));
				dataElemID.add(GeneralUtil.FNVHash1(strs[1]));
				n++;
			}
			sc.close();
		}
		System.out.println("total number of packets: " + n);
		
		/** measurment for flow sizes **/
		for (int i : sizeMeasurementConfig) {
			tpForSize(i, dataFlowID, dataElemID);
		}
		
		/** measurment for flow spreads **/
		for (int i : spreadMeasurementConfig) {
			tpForSpread(i, dataFlowID, dataElemID);
		}
	}
	
	/** Get throughput for flow size measurement. */
	public static void tpForSize(int sketchMin, ArrayList<Integer> dataFlowID, ArrayList<Integer> dataElemID) throws FileNotFoundException {
		int totalNum = dataFlowID.size();
		initCM(sketchMin);
		String resultFilePath = GeneralUtil.path + "Throughput\\CM_size_" + C[0][0].getDataStructureName()
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m + "_tp_" + GeneralUtil.throughputSamplingRate;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		Double res = 0.0;
		
		if (sketchMin == 0) { // for enhanced countmin
			double duration = 0;
			
			for (int i = 0; i < loops; i++) {
				initCM(sketchMin);
				int[] arrIndex = new int[d];
				int[] arrVal = new int[d];
				
				long startTime = System.nanoTime();
				for (int j = 0; j < totalNum; j++) {
					//if (rand.nextDouble() <= GeneralUtil.throughputSamplingRate) {
						int minVal = Integer.MAX_VALUE;
		                for (int k = 0; k < d; k++) {
		                    int jj = (GeneralUtil.intHash(dataFlowID.get(j) ^ S[k]) % w + w) % w;
		                    arrIndex[k] = jj;
		                    arrVal[k] = C[k][jj].getValue();
		                    minVal = Math.min(minVal, arrVal[k]);
		                }
		                
		                for (int k = 0; k < d; k++) {
				            if (arrVal[k] == minVal) {
				            	C[k][arrIndex[k]].encode(dataElemID.get(j));           
				            }
		                }
					//}
				}
				long endTime = System.nanoTime();
				duration += 1.0 * (endTime - startTime) / 1000000000;
			}
			res = 1.0 * totalNum / (duration / loops);
			//System.out.println("Average execution time: " + 1.0 * duration / loops + " seconds");
			System.out.println(C[0][0].getDataStructureName() + "\t Average Throughput: " + 1.0 * totalNum / (duration / loops) + " packets/second" );
		} else {
			double duration = 0;

			
			for (int i = 0; i < loops; i++) {
				initCM(sketchMin);
				long startTime = System.nanoTime();
				for (int j = 0; j < totalNum; j++) {
					//if (rand.nextDouble() <= GeneralUtil.throughputSamplingRate) {
		                for (int k = 0; k < d; k++) {
		                	C[k][(GeneralUtil.intHash(dataFlowID.get(j) ^ S[k]) % w + w) % w].encode();
		                }
					//}
				}
				long endTime = System.nanoTime();
				duration += 1.0 * (endTime - startTime) / 1000000000;
			}
			
			res = 1.0 * totalNum / (duration / loops);
			//System.out.println("Average execution time: " + 1.0 * duration / loops + " seconds");
			System.out.println(C[0][0].getDataStructureName() + "\t Average Throughput: " + 1.0 * totalNum / (duration / loops) + " packets/second" );
		}
		pw.println(res.intValue());
		pw.close();
	}
	
		/** Get throughput for flow spread measurement. */
	public static void tpForSpread(int sketchMin, ArrayList<Integer> dataFlowID, ArrayList<Integer> dataElemID) throws FileNotFoundException {
		int totalNum = dataFlowID.size();
		initCM(sketchMin);
		String resultFilePath = GeneralUtil.path + "Throughput\\CM_spread_" + C[0][0].getDataStructureName()
				+ "_M_" +  M / 1024 / 1024 + "_d_" + d + "_u_" + u + "_m_" + m + "_tp_" + GeneralUtil.throughputSamplingRate;
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		Double res = 0.0;
		totalNum = 50000;
		double duration = 0;

		for (int i = 0; i < loops; i++) {
			initCM(sketchMin);
			encodeSpread(GeneralUtil.dataStreamForFlowSpread);

			long startTime = System.nanoTime();
			for (int j = 0; j < totalNum; j++) {
				int estimate = Integer.MAX_VALUE;

				for (int k = 0; k < d; k++) {
					int jj =      (GeneralUtil.intHash(GeneralUtil.FNVHash1(dataElemID.get(j)) ^ S[k]) % w + w) % w;


                	estimate=Math.min(estimate, C[k][jj].getValue());
                }
			}	
			long endTime = System.nanoTime();
			duration += 1.0 * (endTime - startTime) / 1000000000;
		}
		res = 1.0 * totalNum / (duration / loops);
		//System.out.println("Average execution time: " + 1.0 * duration / loops + " seconds");
		System.out.println(C[0][0].getDataStructureName() + "\t Average Throughput: " + 1.0 * totalNum / (duration / loops) + " packets/second" );
		pw.println(res.intValue());
		pw.close();
	}
}

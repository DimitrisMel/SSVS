import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;

public class GeneralDataProcessing {
	public static String path = "\\D:\\GeneralFramework\\";
		// absolute path for data
	public static int time_itvl = 5;			// time interval between two consecutive raw data downloaded from UF (in minutes)
	public static int numOfFiles = 12;			// number of files to be processed
	public static Set<String> routersToBeProcessed = new HashSet<> (Arrays.asList("ctx36-nexus", "ssrb230a-nexus")); // name of UF core routers to be processed, used for file path;
	public static Set<String> flowLabels = new HashSet<> (Arrays.asList("src", "dst", "srcDst"));		// flow labels (src dst srcDst).
	public static String date = "20170712";
	
	/** parameters for spatial join	 */
	public static Set<String> routersToBeSJoint = new HashSet<> (Arrays.asList("ctx36-nexus", "ssrb230a-nexus")); // name of UF core routers to be used for joint estimation;

	/** parameters for temporal join  */
	public static int numOfJointTimes = 5;
	public static String routerToBeTJoint = "ctx36-nexus";
	public static Set<String> timeStampsToBeJoint = new HashSet<> ();
	
	public static void main(String[] args) throws FileNotFoundException {
	
		getPerSourceStatistics("","events","","dst");
		
		System.out.println("DONE!!!!!!!!!!!!!!!!!");		
		
	}
	
	// Convert an integer (0 ~ 1440) to a string of fixed length 4 (padding zeros in front if necessary).
	public static String timeStampToString(int t) {
		String result = "";
		int n = t, len = 4;
		while (len > 0) {
			result = String.valueOf(n % 10) + result;
			n = n / 10;
			len--;
		}
		return result;
	}
	
	/**
	 * Extract source IP and destination IP from raw data.
	 * File name: "output" + date + timeStamp + ".txt";
	 */
	public static void extractIPs(String date, String timeStamp, String routerName) throws FileNotFoundException {
		String fileName = "output" + date + timeStamp + ".txt";
		Scanner sc = new Scanner(new File(path + "\\raw\\" + routerName + "\\" + fileName));	// source file
		
		String resultFilePath = path + "\\processed\\" + routerName + "\\" + fileName;			// output file
		PrintWriter pw = new PrintWriter(new File(resultFilePath));
		System.out.println("Result directory: " + resultFilePath); 
		
		sc.nextLine();	// skip the first line;
		while (sc.hasNextLine()) {
			String entry = sc.nextLine();
			String[] strs = entry.split("\\s+");
			/*if (strs.length != 8) {
				System.out.println("Wrong data format!");
				return;
			}*/
			String srcIP_port = strs[4], dstIP_port = strs[5];
			String srcIP = srcIP_port.split(":")[0], dstIP = dstIP_port.split(":")[0];
			if (srcIP.split("\\.").length == 4 && dstIP.split("\\.").length == 4) 
				pw.println(srcIP + "\t" + dstIP);
		}
		sc.close();
		pw.close();
	}
	
	
	/**
	 * Count flow size and spread of each source file.
	 * Input file name: "output" + date + timeStamp + ".txt";
	 * Output file name: flowLabel + "Size"/"Spread" + date + timeStamp + ".txt";
	 */
	public static void getPerSourceStatistics(String date, String timeStamp, String routerName, String flowLabel) throws FileNotFoundException {
		String inputFileName =  date + timeStamp + ".txt";
		
		if (flowLabel.equals("src")) { 		//  per-source flow
			/** For flow size */
			Scanner sc = new Scanner(new File(path + inputFileName));
			String outputSizeFileName = "\\result\\" + flowLabel + "_size_" + inputFileName;
			PrintWriter pw = new PrintWriter(new File(path + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				mp.put(srcIP, mp.getOrDefault(srcIP, 0) + 1);
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();
			sc.close();
			
			/** For flow spread **/
			sc = new Scanner(new File(path + inputFileName));
			String outputSpreadFileName = "\\result\\" + flowLabel + "_spread_" + inputFileName;

			pw = new PrintWriter(new File(path + outputSpreadFileName));

			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				if (!mp_spread.containsKey(srcIP)) mp_spread.put(srcIP, new HashSet<String> ());
				mp_spread.get(srcIP).add(dstIP);
			}
			mp.clear();
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();		
			sc.close();
			
			
		} else if (flowLabel.equals("dst")) {	// per-destination flow
			/** For flow size **/
			Scanner sc = new Scanner(new File(path + inputFileName));
			String outputSizeFileName = "\\result\\" + flowLabel + "_size_" + inputFileName;
			PrintWriter pw = new PrintWriter(new File(path + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				mp.put(dstIP, mp.getOrDefault(dstIP, 0) + 1);
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();
			sc.close();
			
			/** For flow spread **/
			sc = new Scanner(new File(path + inputFileName));
			String outputSpreadFileName = "\\result\\" + flowLabel + "_spread_" + inputFileName;
			pw = new PrintWriter(new File(path + outputSpreadFileName));	
			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				if (!mp_spread.containsKey(dstIP)) mp_spread.put(dstIP, new HashSet<String> ());
				mp_spread.get(dstIP).add(srcIP);
			}
			mp.clear();
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();		
			sc.close();
			
			
			/* for input spread*/
			sc = new Scanner(new File(path + inputFileName));
			String SpreadFileName = "\\" + flowLabel + "_spread_" + inputFileName;

			pw = new PrintWriter(new File(path + SpreadFileName));

			List<String> sortElement = new ArrayList<>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				String flowelement = dstIP+'\t'+srcIP;
				sortElement.add(flowelement);
			}
			Collections.sort(sortElement);
			mp.clear();
			String prioStr = "";
			for (String entry: sortElement) {
				if (prioStr==entry)
					continue;
				else {
					prioStr = entry;
					pw.println(entry);
				}
				
			}
			
			//for spread input 
			sc = new Scanner(new File(path + inputFileName));
			String SpreadFileName2 = "\\" + flowLabel + "_spread_" + inputFileName;

			pw = new PrintWriter(new File(path + SpreadFileName2));

			List<String> sortElement2 = new ArrayList<>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				String flowelement2 = dstIP+'\t'+srcIP;
				sortElement2.add(flowelement2);
			}
			Collections.sort(sortElement2);
			mp.clear();
			String prioStr2 = "1\t4";
			for (String entry: sortElement2) {
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0];
				String dstIP = strs[1];
				strs = prioStr2.split("\\s+");
				String srcIP1 = strs[0];
				String dstIP1 = strs[1];
				if (Long.parseLong(srcIP1)==Long.parseLong(srcIP) &&Long.parseLong(dstIP1)==Long.parseLong(dstIP)) {
					continue;}
				else { 
					prioStr2 = entry;
					pw.println(entry);
				}
				
			}
			pw.close();		
			sc.close();
			
		} else if (flowLabel.equals("srcDst")) {	// per-src/dst flow	
			/** For flow size **/
			Scanner sc = new Scanner(new File(path + inputFileName));
			String outputSizeFileName ="\\result\\" + flowLabel + "_size_" + inputFileName;
			PrintWriter pw = new PrintWriter(new File(path +  outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] strs = entry.split("\\s+");
				String srcIP = strs[0], dstIP = strs[1];
				String label = srcIP + "\t" + dstIP;
				mp.put(label, mp.getOrDefault(label, 0) + 1);
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();
			sc.close();
			
		} else {
			System.out.println("Labels other than source/destination IP!");
		}

	}
	
	/**
	 * Sort Hashmap by values 
	 * @param hm
	 * @return
	 */
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Integer> > list = 
               new LinkedList<Map.Entry<String, Integer> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
            public int compare(Map.Entry<String, Integer> o1,  
                               Map.Entry<String, Integer> o2) 
            { 
                return (o2.getValue()).compareTo(o1.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
        for (Map.Entry<String, Integer> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    }
    
    /**
     * @throws FileNotFoundException 
     * 
     */
	public static void spatialJoinStatistics(String date, String timeStamp, Set<String> routerNames, String flowLabel) throws FileNotFoundException  {
		if (flowLabel.equals("src")) { 		//  per-source flow
			/** For flow size **/
			String outputSizeFileName = flowLabel + "JointSize" + date + timeStamp + ".txt";
			PrintWriter pw = new PrintWriter(new File(path + "\\spatial_join\\" + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			
			for (String routerName: routerNames) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					mp.put(srcIP, mp.getOrDefault(srcIP, 0) + 1);
				}
				sc.close();
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();

			String outputSpreadFileName = flowLabel + "JointSpread" + date + timeStamp + ".txt";
			pw = new PrintWriter(new File(path + "\\spatial_Join\\" + "\\" + outputSpreadFileName));
			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			/** For flow spread **/
			for (String routerName: routerNames) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));	
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					if (!mp_spread.containsKey(srcIP)) mp_spread.put(srcIP, new HashSet<String> ());
					mp_spread.get(srcIP).add(dstIP);
				}
				mp.clear();	
				sc.close();
			}
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();	
		} else if (flowLabel.equals("dst")) {	// per-destination flow
			/** For flow size **/
			String inputFileName = "output" + date + timeStamp + ".txt";
			String outputSizeFileName = flowLabel + "JointSize" + date + timeStamp + ".txt";
			PrintWriter pw = new PrintWriter(new File(path + "\\spatial_join\\" + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			
			for (String routerName: routerNames) {
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					mp.put(dstIP, mp.getOrDefault(dstIP, 0) + 1);
				}
				sc.close();
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();

			String outputSpreadFileName = flowLabel + "JointSpread" + date + timeStamp + ".txt";
			pw = new PrintWriter(new File(path + "\\spatial_Join\\" + "\\" + outputSpreadFileName));
			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			/** For flow spread **/
			for (String routerName: routerNames) {
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));	
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					if (!mp_spread.containsKey(dstIP)) mp_spread.put(dstIP, new HashSet<String> ());
					mp_spread.get(dstIP).add(srcIP);
				}
				mp.clear();	
				sc.close();
			}
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();	
		}
	}
	
	/**
     * @throws FileNotFoundException 
     * 
     */
	public static void temporalJoinStatistics(String date, Set<String> timeStamps, String routerName, String flowLabel) throws FileNotFoundException  {
		if (flowLabel.equals("src")) { 		//  per-source flow
			/** For flow size **/
			String outputSizeFileName = flowLabel + timeStamps.size() + "TsJointSize" + date + ".txt";
			PrintWriter pw = new PrintWriter(new File(path + "\\temporal_join\\" + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			
			for (String timeStamp: timeStamps) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					mp.put(srcIP, mp.getOrDefault(srcIP, 0) + 1);
				}
				sc.close();
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();

			String outputSpreadFileName = flowLabel + timeStamps.size() + "TsJointSpread" + date + ".txt";
			pw = new PrintWriter(new File(path + "\\temporal_join\\" + "\\" + outputSpreadFileName));
			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			/** For flow spread **/
			for (String timeStamp: timeStamps) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));	
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					if (!mp_spread.containsKey(srcIP)) mp_spread.put(srcIP, new HashSet<String> ());
					mp_spread.get(srcIP).add(dstIP);
				}
				mp.clear();	
				sc.close();
			}
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();	
		} else if (flowLabel.equals("dst")) {	// per-destination flow
			/** For flow size **/
			String outputSizeFileName = flowLabel + timeStamps.size() + "TsJointSpread" + date + ".txt";
			PrintWriter pw = new PrintWriter(new File(path + "\\temporal_join\\" + outputSizeFileName));	
			HashMap<String, Integer> mp = new HashMap<String, Integer>();
			
			for (String timeStamp: timeStamps) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					mp.put(dstIP, mp.getOrDefault(dstIP, 0) + 1);
				}
				sc.close();
			}
			Map<String, Integer> result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();

			String outputSpreadFileName = flowLabel + timeStamps.size() + "TsJointSpread" + date + ".txt";
			pw = new PrintWriter(new File(path + "\\temporal_join\\" + "\\" + outputSpreadFileName));
			HashMap<String, HashSet<String>> mp_spread = new HashMap<>();
			/** For flow spread **/
			for (String timeStamp: timeStamps) {
				String inputFileName = "output" + date + timeStamp + ".txt";
				Scanner sc = new Scanner(new File(path + "\\processed\\" + routerName + "\\" + inputFileName));	
				while (sc.hasNext()) {
					String entry = sc.nextLine();
					String[] strs = entry.split("\\s+");
					String srcIP = strs[0], dstIP = strs[1];
					if (!mp_spread.containsKey(dstIP)) mp_spread.put(dstIP, new HashSet<String> ());
					mp_spread.get(dstIP).add(srcIP);
				}
				mp.clear();	
				sc.close();
			}
			for (Map.Entry<String, HashSet<String>> entry: mp_spread.entrySet()) {
				mp.put(entry.getKey(), entry.getValue().size());
			}
			result = sortByValue(mp);
			for (Map.Entry<String, Integer> entry: result.entrySet()) {
				pw.println(entry.getKey() + "\t" + entry.getValue());
			}
			pw.close();
		}
	}
}

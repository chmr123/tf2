import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;


public class TF_NEW_Main {
	static Map<Integer, int[]> featureMap_train = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, int[]> featureMap_test = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, String> instanceClass_train = new LinkedHashMap<Integer, String>();
	static Map<Integer, String> instanceClass_test = new LinkedHashMap<Integer, String>();
	
	public static void main(String[] args) throws IOException {
		CSVReader reader = new CSVReader(new FileReader("filezilla.csv"));
		String[] nextLine;
		Set<String> allDependency = new LinkedHashSet<String>();	
		ArrayList<String> allkeywords = new ArrayList<String>();
		
		while ((nextLine = reader.readNext()) != null) {
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			String[] keywords = nextLine[3].split(";");
			Instance ins = new Instance(line, keywords);
			
			for(String k : keywords){
				allkeywords.add(k);
				Set<String> dependency = ins.getDependecy(k);
				for(String d : dependency){
					allDependency.add(d);
				}
			}
		}
		
		ArrayList<String> allDepedencyArray = new ArrayList<String>(allDependency);
		reader = new CSVReader(new FileReader("filezilla.csv"));
		int documentID = 0;
		while ((nextLine = reader.readNext()) != null) {
			ArrayList<String> instance_dependency = new ArrayList<String>();
			int[] feature = new int[allDependency.size()];
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			String[] keywords = nextLine[3].split(";");
			Instance ins = new Instance(line, keywords);
			instanceClass_train.put(documentID,  nextLine[2]);
			
			for(String k : keywords){
				Set<String> dependency = ins.getDependecy(k);
				for(String d : dependency){
					instance_dependency.add(d);
				}
			}
			
			for(int i = 0; i < allDepedencyArray.size(); i++){
				if(instance_dependency.contains(allDepedencyArray.get(i))){
					feature[i] = 1;
				}
			}
			
			featureMap_train.put(documentID, feature);
			documentID++;
		}
		
		
		documentID = 0;
		reader = new CSVReader(new FileReader("pristream.csv"));
		while ((nextLine = reader.readNext()) != null) {
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			ArrayList<String> keywordsInCommon = new ArrayList<String>();
			String[] sentenceSplit = line.split("\\s+");
			for(String s : sentenceSplit){
				if(allkeywords.contains(s)){
					keywordsInCommon.add(s);
				}
			}
			
			String [] keywords = keywordsInCommon.toArray(new String[keywordsInCommon.size()]); // convert arraylist to String array
			Instance ins = new Instance(line, keywords);
			instanceClass_test.put(documentID,  nextLine[2]);
			
			int[] feature = new int[allDependency.size()];
			ArrayList<String> instance_dependency = new ArrayList<String>();
			
			for(String k : keywords){
				Set<String> dependency = ins.getDependecy(k);
				for(String d : dependency){
					instance_dependency.add(d);
				}
			}
			
			for(int i = 0; i < allDepedencyArray.size(); i++){
				if(instance_dependency.contains(allDepedencyArray.get(i))){
					feature[i] = 1;
				}
			}
			featureMap_test.put(documentID, feature);
			documentID++;
		}
	}
	
	public static void writeFile(String category) throws IOException{
		FileWriter fw1 = new FileWriter(category + ".train");
		int label;
		for(int id : instanceClass_train.keySet()){
			if(instanceClass_train.get(id).equals(category))
				label = 1;
			else
				label = -1;
			fw1.write(label + " ");
			int[] feature = featureMap_train.get(id);
			for(int i = 0; i < feature.length; i++){
				if(feature[i] != 0){
					int featureIndex = i + 1;
					fw1.write(featureIndex + ":" + feature[i] + " ");
				}
			}
			fw1.write("\n");
		}
		fw1.close();
		
		FileWriter fw2 = new FileWriter(category + ".test");
		for(int id : instanceClass_test.keySet()){
			if(instanceClass_test.get(id).equals(category))
				label = 1;
			else
				label = -1;
			fw2.write(label + " ");
			int[] feature = featureMap_train.get(id);
			for(int i = 0; i < feature.length; i++){
				if(feature[i] != 0){
					int featureIndex = i + 1;
					fw2.write(featureIndex + ":" + feature[i] + " ");
				}
			}
			fw2.write("\n");
		}
		fw2.close();
		
	}

}

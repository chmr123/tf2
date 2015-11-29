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

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;


public class TF_NEW_Main {
	static Map<Integer, int[]> featureMap_train = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, int[]> featureMap_test = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, String> instanceClass_train = new LinkedHashMap<Integer, String>();
	static Map<Integer, String> instanceClass_test = new LinkedHashMap<Integer, String>();
	
	static LexicalizedParser lp = LexicalizedParser.loadModel("C:\\Users\\Mingrui\\Desktop\\NLPAPI\\englishPCFG.ser.gz", "-maxLength", "80", "-retainTmpSubcategories");
	static TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	static 	MaxentTagger tagger = new MaxentTagger("C:\\Users\\Mingrui\\Desktop\\NLPAPI\\english-left3words-distsim.tagger");
	
	static String root = "C:\\Users\\Mingrui\\Desktop\\TranferLearning\\";
	public static void main(String[] args) throws IOException {
		String category = "Capability";
		CSVReader reader = new CSVReader(new FileReader(root + "filezilla.csv"));
		String[] nextLine;
		Set<String> allDependency = new LinkedHashSet<String>();	
		ArrayList<String> allkeywords = new ArrayList<String>();
		int count = 0;
		
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in first while loop");
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			String[] keywords = nextLine[3].split(";");
			Instance ins = new Instance(line, keywords);
			ins.postag();
			for(String k : keywords){
				allkeywords.add(k.trim());
				Set<String> dependency = ins.getDependecy(k.trim());
				for(String d : dependency){
					allDependency.add(d);
				}
			}
			count++;
		}
		
		ArrayList<String> allDepedencyArray = new ArrayList<String>(allDependency);
		reader = new CSVReader(new FileReader(root + "filezilla.csv"));
		int documentID = 0;
		count=0;
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in second while loop");
			ArrayList<String> instance_dependency = new ArrayList<String>();
			int[] feature = new int[allDependency.size()];
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			String[] keywords = nextLine[3].split(";");
			Instance ins = new Instance(line, keywords);
			ins.postag();
			instanceClass_train.put(documentID,  nextLine[2]);
			
			for(String k : keywords){
				Set<String> dependency = ins.getDependecy(k.trim());
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
			count++;
		}
		
		
		documentID = 0;
		count=0;
		reader = new CSVReader(new FileReader(root + "prismstream.csv"));
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in third while loop");
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
			ins.postag();
			instanceClass_test.put(documentID,  nextLine[2]);
			
			int[] feature = new int[allDependency.size()];
			ArrayList<String> instance_dependency = new ArrayList<String>();
			
			for(String k : keywords){
				Set<String> dependency = ins.getDependecy(k.trim());
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
			count++;
		}
		
		writeFile(category);
	}
	
	public static void writeFile(String category) throws IOException{
		FileWriter fw1 = new FileWriter(root + category + ".train");
		int label;
		for(int id : instanceClass_train.keySet()){
			if(instanceClass_train.get(id).equals(category))
				label = 1;
			else
				label = -1;
			fw1.write(label + " ");
			int[] feature = featureMap_train.get(id);
			//System.out.println(feature.length);
			for(int i = 0; i < feature.length; i++){
				//if(feature[i] != 0){
					int featureIndex = i + 1;
					fw1.write(featureIndex + ":" + feature[i] + " ");
				//}
			}
			fw1.write("\n");
		}
		fw1.flush();
		fw1.close();
		
		FileWriter fw2 = new FileWriter(root + category + ".test");
		for(int id : instanceClass_test.keySet()){
			if(instanceClass_test.get(id).equals(category))
				label = 1;
			else
				label = -1;
			fw2.write(label + " ");
			int[] feature = featureMap_test.get(id);
			for(int i = 0; i < feature.length; i++){
				//if(feature[i] != 0){
					int featureIndex = i + 1;
					fw2.write(featureIndex + ":" + feature[i] + " ");
				//}
			}
			fw2.write("\n");
		}
		fw2.flush();
		fw2.close();
		
	}

}

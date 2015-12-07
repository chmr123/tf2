import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.opencsv.CSVReader;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;


public class TF_NEW_Main {
	
	static String root = "C:\\Users\\Mingrui\\Desktop\\TransferLearning\\";
	static Map<Integer, int[]> featureMap_train = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, int[]> featureMap_test = new LinkedHashMap<Integer, int[]>();
	static Map<Integer, String> instanceClass_train = new LinkedHashMap<Integer, String>();
	static Map<Integer, String> instanceClass_test = new LinkedHashMap<Integer, String>();
	static Map<Integer, ArrayList<String>> dependencyMap_train = new LinkedHashMap<Integer, ArrayList<String>>();
	
	static LexicalizedParser lp = LexicalizedParser.loadModel(root + "englishPCFG.ser.gz", "-maxLength", "80", "-retainTmpSubcategories");
	static TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	static GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	static 	MaxentTagger tagger = new MaxentTagger(root + "english-left3words-distsim.tagger");
	static Properties props = new Properties(); 
     
	//static String root = "C:\\Users\\Mingrui\\Desktop\\TranferLearning\\";
	
	public static void main(String[] args) throws IOException {
		
		props.put("annotators", "tokenize, ssplit, pos, lemma"); 
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
		String category = "Usability";
		
		//First while loop for training and testing files
		CSVReader reader = new CSVReader(new FileReader(root + "filezilla.csv"));
		String[] nextLine;
		Set<String> allDependency = new LinkedHashSet<String>();	
		ArrayList<String> allkeywords = new ArrayList<String>();
		
		int count = 0;
		int documentID = 0;
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in first while loop");
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			//System.out.println(line);
			//line = lemmatize(line, pipeline);
			//System.out.println(line);
			//nextLine[3] = lemmatize( nextLine[3], pipeline);
			
			String[] keywords = nextLine[3].split(";");
			Instance ins = new Instance(line, keywords);
			ins.postag();
			ArrayList<String> instance_dependency = new ArrayList<String>();
			for(String k : keywords){
				allkeywords.add(k.trim());
				Set<String> dependency = ins.getDependecy(k.trim());
				for(String d : dependency){
					System.out.println(d);
					//System.out.println(d);
					allDependency.add(d);
					instance_dependency.add(d);
				}
			}
			dependencyMap_train.put(documentID, instance_dependency);
			count++;
			documentID++;
		}
		
		
		//Second while loop for training file
		ArrayList<String> allDepedencyArray = new ArrayList<String>(allDependency);
		reader = new CSVReader(new FileReader(root + "filezilla.csv"));
		documentID = 0;
		count=0;
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in second while loop");		
			int[] feature = new int[allDependency.size()];
			instanceClass_train.put(documentID,  nextLine[2]);
			
			for(int i = 0; i < allDepedencyArray.size(); i++){
				if(dependencyMap_train.get(documentID).contains(allDepedencyArray.get(i))){
					feature[i] = 1;
				}
			}
			
			featureMap_train.put(documentID, feature);
			documentID++;
			count++;
		}
		
		//Third while loop for testing file
		documentID = 0;
		count=0;
		reader = new CSVReader(new FileReader(root + "prismstream.csv"));
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("Working on " + count + " instance in third while loop");
			String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
			
			//line = lemmatize(line, pipeline);
			//nextLine[3] = lemmatize( nextLine[3], pipeline);
			
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
				if(feature[i] != 0){
					int featureIndex = i + 1;
					fw1.write(featureIndex + ":" + feature[i] + " ");
				}
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
				if(feature[i] != 0){
					int featureIndex = i + 1;
					fw2.write(featureIndex + ":" + feature[i] + " ");
				}
			}
			fw2.write("\n");
		}
		fw2.flush();
		fw2.close();
	}

	
	private static String lemmatize(String line, StanfordCoreNLP pipeline){
		 Annotation document = pipeline.process(line);  
		 String lemmatized = "";
	        for(CoreMap sentence: document.get(SentencesAnnotation.class))
	        {    
	            for(CoreLabel token: sentence.get(TokensAnnotation.class))
	            {       
	                String word = token.get(TextAnnotation.class);      
	                String lemma = token.get(LemmaAnnotation.class); 
	                lemmatized = lemmatized + lemma + " ";
	            }
	        }
	        return lemmatized;
	}
}

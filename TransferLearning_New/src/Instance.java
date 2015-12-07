import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class Instance {
	
	
	private String sentence;
	private HashMap<String, String> postags;
	private ArrayList<String> sentenceSplit;
	private String[] keywords;
	private LinkedHashMap<String, HashSet<String>> keywords_dependency;
	private LinkedHashMap<String, Integer> feature;

	public Instance(String sentence, String[] keywords) {
		this.sentence = sentence;
		this.keywords = keywords;
		postags = new HashMap<String, String>();
	}

	public void postag() {
	
		String postagged_sentence = TF_NEW_Main.tagger.tagString(sentence);
		//System.out.println(postagged_sentence);
		for(String s : postagged_sentence.split(" ")){
			String word = s.substring(0, s.indexOf("_"));
			String tag = s.substring(s.indexOf("_") + 1);
			postags.put(word, tag);
		}
	} 

	public void generateFeature(){
		for(String keyword : keywords){
			HashSet<String> dependency = getDependecy(keyword);
			if(keywords_dependency.keySet().contains(keyword)){
				HashSet<String> old = keywords_dependency.get(keyword);
				dependency.addAll(old);
			}
			keywords_dependency.put(keyword, dependency);
		}
	}
	
	public HashSet<String> getDependecy(String keyword) {
		
		String[] array = sentence.split("\\s+");
		Tree parse = TF_NEW_Main.lp.apply(Sentence.toWordList(array));
		GrammaticalStructure gs = TF_NEW_Main.gsf.newGrammaticalStructure(parse);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		HashSet<String> keywordsDependency = new HashSet<String>();
		// String lemmatizedKeyword = lemmatize(keyword);
		for (TypedDependency t : tdl) {
			String d = t.toString();
			//System.out.println(d);
			String pair = d.substring(d.indexOf("(") + 1, d.indexOf(")"));
			//System.out.println(pair);
			String[] terms = pair.split(", ");
			String term1 = terms[0].trim().substring(0,terms[0].lastIndexOf("-"));
			String term2 = terms[1].trim().substring(0,terms[1].lastIndexOf("-"));
			//System.out.println(term1 + " " + term2);
			

			// Match keywords with the terms in the tuples, if matched, add
			// the
			// tuple into the arraylist
			String[] wordsplitted = keyword.split("\\s+");
			for (String key : wordsplitted) {
				if (term1.equals(key)) {
					String tag = postags.get(term2);
					if(tag == null) continue;
					if(tag.contains("NN")) tag = "NN";
					if(tag.contains("VB")) tag = "VB";
					if(tag.contains("JJ")) tag = "JJ";
					keywordsDependency.add(term1 + "_" + tag);
				}
				if (term2.equals(key)) {
					String tag = postags.get(term1);
					if(tag == null) continue;
					if(tag.contains("NN")) tag = "NN";
					if(tag.contains("VB")) tag = "VB";
					if(tag.contains("JJ")) tag = "JJ";
					keywordsDependency.add(term2 + "_" + tag);
				}
			}
		}
		return keywordsDependency;
	}
}

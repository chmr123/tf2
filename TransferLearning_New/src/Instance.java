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
	}

	public void postag() {
		MaxentTagger tagger = new MaxentTagger("taggers/left3words-distsim-wsj-0-18.tagger");
		String postagged_sentence = tagger.tagString(sentence);
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
		LexicalizedParser lp = LexicalizedParser.loadModel(
				"/home/mingrui/Desktop/englishPCFG.ser.gz", "-maxLength", "80",
				"-retainTmpSubcategories");
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		// Uncomment the following line to obtain original Stanford
		// Dependencies
		// tlp.setGenerateOriginalDependencies(true);
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		String[] array = sentence.split("\\s+");
		Tree parse = lp.apply(Sentence.toWordList(array));
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		HashSet<String> keywordsDependency = new HashSet<String>();
		// String lemmatizedKeyword = lemmatize(keyword);
		for (TypedDependency t : tdl) {
			String d = t.toString();
			String pair = d.substring(d.indexOf("(") + 1, d.indexOf("("));
			String[] terms = pair.split(",");
			String term1 = terms[0].trim();
			String term2 = terms[1].trim();

			// Match keywords with the terms in the tuples, if matched, add
			// the
			// tuple into the arraylist
			String[] wordsplitted = keyword.split(" ");
			for (String key : wordsplitted) {
				if (term1.equals(key)) {
					String tag = postags.get(term2);
					keywordsDependency.add(tag);
				}
				if (term2.equals(key)) {
					String tag = postags.get(term1);
					keywordsDependency.add(tag);
				}
			}
		}
		return keywordsDependency;
	}
}

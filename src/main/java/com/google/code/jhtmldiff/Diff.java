/**
 * Copyright (c) 2011 Nathan Herald, Rohland de Charmoy, Adam Gent
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */

package com.google.code.jhtmldiff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Diff {

	private StringBuilder content;
	private String oldText, newText;
	private String[] oldWords, newWords;
	Map<String, List<Integer>> wordIndices;
	private String[] specialCaseOpeningTags = new String[] { "<strong[\\>\\s]+", "<b[\\>\\s]+", "<i[\\>\\s]+",
			"<big[\\>\\s]+", "<small[\\>\\s]+", "<u[\\>\\s]+", "<sub[\\>\\s]+", "<sup[\\>\\s]+", "<strike[\\>\\s]+",
			"<s[\\>\\s]+" };
	private String[] specialCaseClosingTags = new String[] { "</strong>", "</b>", "</i>", "</big>", "</small>", "</u>",
			"</sub>", "</sup>", "</strike>", "</s>" };

	/**
	 * @param oldText
	 * @param newText
	 */
	public Diff(String oldText, String newText) {
		this.oldText = oldText;
		this.newText = newText;

		this.content = new StringBuilder();
	}

	/**
	 * Diffs
	 * @return diff, not null.
	 */
	public String build() {
		this.SplitInputsToWords();

		this.IndexNewWords();

		List<Operation> operations = this.Operations();

		for (Operation item : operations) {
			this.PerformOperation(item);
		}

		return this.content.toString();
	}

	private void IndexNewWords() {
		this.wordIndices = new HashMap<String, List<Integer>>();
		for (int i = 0; i < this.newWords.length; i++) {
			String word = this.newWords[i];

			if (this.wordIndices.containsKey(word)) {
				this.wordIndices.get(word).add(i);
			}
			else {
				this.wordIndices.put(word, new ArrayList<Integer>());
				this.wordIndices.get(word).add(i);
			}
		}
	}

	private void SplitInputsToWords() {
		this.oldWords = ConvertHtmlToListOfWords(this.Explode(this.oldText));
		this.newWords = ConvertHtmlToListOfWords(this.Explode(this.newText));
	}

	static boolean contains(Object[] os, Object o) {
		for (int i = 0; i < os.length; i++) {
			Object oi = os[i];
			if (o.equals(oi))
				return true;
		}
		return false;
	}
	
	static String join(String[] sa, String space) {
		StringBuilder sb = new StringBuilder();
		boolean skip = true;
		for (String s : sa) {
			if (! skip)
				sb.append(space);
			sb.append(s);
		}
		return sb.toString();
	}
	
	static String join(List<String> sa, String space) {
		StringBuilder sb = new StringBuilder();
		boolean skip = true;
		for (String s : sa) {
			if (! skip)
				sb.append(space);
			sb.append(s);
		}
		return sb.toString();
	}	
	
	private String[] ConvertHtmlToListOfWords(String[] characterString) {
		Mode mode = Mode.character;
		String current_word = "";
		List<String> words = new ArrayList<String>();
		for (String character : characterString) {
			switch (mode) {
				case character:

					if (this.IsStartOfTag(character)) {
						if (! "".equals(current_word)) {
							words.add(current_word);
						}

						current_word = "<";
						mode = Mode.tag;
					}
					else if (Pattern.matches("\\s", character)) {
						if (! "".equals(current_word)) {
							words.add(current_word);
						}
						current_word = character;
						mode = Mode.whitespace;
					}
					else {
						current_word += character;
					}

					break;
				case tag:
					if (this.IsEndOfTag(character)) {
						current_word += ">";
						words.add(current_word);
						current_word = "";

						if (IsWhiteSpace(character)) {
							mode = Mode.whitespace;
						}
						else {
							mode = Mode.character;
						}
					}
					else {
						current_word += character;
					}

					break;
				case whitespace:

					if (this.IsStartOfTag(character)) {
						if (! "".equals(current_word) ) {
							words.add(current_word);
						}
						current_word = "<";
						mode = Mode.tag;
					}
					else if (Pattern.matches("\\s", character)) {
						current_word += character;
					}
					else {
						if (! "".equals(current_word)) {
							words.add(current_word);
						}

						current_word = character;
						mode = Mode.character;
					}

					break;
				default:
					break;
			}

		}
		if ( ! "".equals(current_word) ) {
			words.add(current_word);
		}

		return words.toArray(new String[] {});
	}

	private boolean IsStartOfTag(String val) {
		return val == "<";
	}

	private boolean IsEndOfTag(String val) {
		return val == ">";
	}

	private boolean IsWhiteSpace(String value) {
		return Pattern.matches("\\s", value);
	}

	private String[] Explode(String value) {
		char[] ca = value.toCharArray();
		String[] s = new String[ca.length];
		for (int i = 0; i < ca.length; i++) {
			s[i] = ""+ ca[i];
		}
		return s;
	}

	private void PerformOperation(Operation operation) {
		switch (operation.Action) {
			case equal:
				this.ProcessEqualOperation(operation);
				break;
			case delete:
				this.ProcessDeleteOperation(operation, "diffdel");
				break;
			case insert:
				this.ProcessInsertOperation(operation, "diffins");
				break;
			case none:
				break;
			case replace:
				this.ProcessReplaceOperation(operation);
				break;
			default:
				break;
		}
	}

	private void ProcessReplaceOperation(Operation operation) {
		this.ProcessDeleteOperation(operation, "diffmod");
		this.ProcessInsertOperation(operation, "diffmod");
	}

	private static List<String> where(String[] sa, Func2<String, Integer, Boolean> f) {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < sa.length; i++) {
			Boolean b = f.apply(sa[i], i);
			if (b)
				a.add(sa[i]);
		}
		return a;
	}

	private static List<String> where(List<String> sa, Func2<String, Integer, Boolean> f) {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < sa.size(); i++) {
			Boolean b = f.apply(sa.get(i), i);
			if (b)
				a.add(sa.get(i));
		}
		return a;
	}

	private void ProcessInsertOperation(final Operation operation, String cssClass) {

		List<String> r = where(this.newWords, new Func2<String, Integer, Boolean>() {

			public Boolean apply(String f, Integer pos) {
				return pos >= operation.StartInNew && pos < operation.EndInNew;
			}

		});
		this.InsertTag("ins", cssClass, r);
	}

	private void ProcessDeleteOperation(final Operation operation, String cssClass) {
		List<String> r = where(this.oldWords, new Func2<String, Integer, Boolean>() {

			public Boolean apply(String f, Integer pos) {
				return pos >= operation.StartInOld && pos < operation.EndInOld;
			}

		});
		this.InsertTag("del", cssClass, r);
	}

	private void ProcessEqualOperation(final Operation operation) {
		List<String> r = where(this.newWords, new Func2<String, Integer, Boolean>() {

			public Boolean apply(String f, Integer pos) {
				return pos >= operation.StartInNew && pos < operation.EndInNew;
			}

		});
		this.content.append(join(r, ""));
	}

	// / <summary>
	// / This method encloses words within a specified tag (ins or del), and
	// adds this into "content",
	// / with a twist: if there are words contain tags, it actually creates
	// multiple ins or del,
	// / so that they don't include any ins or del. This handles cases like
	// / old: '<p>a</p>'
	// / new: '<p>ab</p><p>c</b>'
	// / diff result: '<p>a<ins>b</ins></p><p><ins>c</ins></p>'
	// / this still doesn't guarantee valid HTML (hint: think about diffing a
	// text containing ins or
	// / del tags), but handles correctly more cases than the earlier version.
	// /
	// / P.S.: Spare a thought for people who write HTML browsers. They live in
	// this ... every day.
	// / </summary>
	// / <param name="tag"></param>
	// / <param name="cssClass"></param>
	// / <param name="words"></param>
	private void InsertTag(String tag, String cssClass, List<String> words) {
		while (true) {
			if (words.size() == 0) {
				break;
			}
			final Diff self = this;
			String[] nonTags = ExtractConsecutiveWords(words, new Func<String, Boolean>() {

				public Boolean apply(String x) {
					return !self.IsTag(x);
				}
			});

			String specialCaseTagInjection = "";
			boolean specialCaseTagInjectionIsBefore = false;

			if (nonTags.length != 0) {
				String text = this.WrapText(join(nonTags, ""), tag, cssClass);

				this.content.append(text);
			}
			else {
				// Check if strong tag
				boolean specialMatch = false;
				for (String s : this.specialCaseOpeningTags) {
					if (Pattern.matches(s, words.get(0))) {
						specialMatch = true;
						break;
					}
				}
				if (specialMatch) {
					specialCaseTagInjection = "<ins class='mod'>";
					if (tag == "del") {
						words.remove(0);
					}
				}
				else if (contains(this.specialCaseClosingTags, words.get(0))) {
					specialCaseTagInjection = "</ins>";
					specialCaseTagInjectionIsBefore = true;
					if (tag == "del") {
						words.remove(0);
					}
				}

			}

			if (words.size() == 0 && specialCaseTagInjection.length() == 0) {
				break;
			}

			if (specialCaseTagInjectionIsBefore) {
				String[] e = this.ExtractConsecutiveWords(words, new Func<String, Boolean>() {

					public Boolean apply(String x) {
						return self.IsTag(x);
					}

				});
				this.content.append(specialCaseTagInjection + join(e, ""));
			}
			else {
				String[] e = this.ExtractConsecutiveWords(words, new Func<String, Boolean>() {

					public Boolean apply(String x) {
						return self.IsTag(x);
					}

				});
				this.content.append(join(e, "") + specialCaseTagInjection);
			}
		}
	}

	private String WrapText(String text, String tagName, String cssClass) {
		return "<"+tagName+ " class=\"" + cssClass + "\">" +text +"</"+tagName +">";
	}

	private String[] ExtractConsecutiveWords(List<String> words, Func<String, Boolean> condition) {
		Integer indexOfFirstTag = null;

		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);

			if (!condition.apply(word)) {
				indexOfFirstTag = i;
				break;
			}
		}

		if (indexOfFirstTag != null) {
			final Integer it = indexOfFirstTag;
			List<String> items = where(words, new Func2<String, Integer, Boolean>() {

				public Boolean apply(String f, Integer pos) {
					return pos >= 0 && pos < it;
				}
			});

			if (indexOfFirstTag > 0) {
				removeRange(words, 0, indexOfFirstTag);
			}
			return items.toArray(new String[] {});
		}
		else {
			final Integer it = words.size();
			List<String> items = where(words, new Func2<String, Integer, Boolean>() {

				public Boolean apply(String f, Integer pos) {
					return pos >= 0 && pos <= it;
				}
			});
			removeRange(words, 0, words.size());
			return items.toArray(new String[] {});
		}
	}

	private static void removeRange(List<?> list, int i, int j) {
		list.subList(i, i + j).clear();
	}

	private boolean IsTag(String item) {
		boolean isTag = IsOpeningTag(item) || IsClosingTag(item);
		return isTag;
	}

	private boolean IsOpeningTag(String item) {
		return Pattern.matches("^\\s*<[^>]+>\\s*$", item);
	}

	private boolean IsClosingTag(String item) {
		return Pattern.matches("^\\s*</[^>]+>\\s*$", item);
	}

	private List<Operation> Operations() {
		int positionInOld = 0, positionInNew = 0;
		List<Operation> operations = new ArrayList<Operation>();

		List<Diff.Match> matches = this.MatchingBlocks();

		matches.add(new Match(this.oldWords.length, this.newWords.length, 0));

		for (int i = 0; i < matches.size(); i++) {
			Diff.Match match = matches.get(i);

			boolean matchStartsAtCurrentPositionInOld = (positionInOld == match.StartInOld);
			boolean matchStartsAtCurrentPositionInNew = (positionInNew == match.StartInNew);

			Action action = Action.none;

			if (matchStartsAtCurrentPositionInOld == false && matchStartsAtCurrentPositionInNew == false) {
				action = Action.replace;
			}
			else if (matchStartsAtCurrentPositionInOld == true && matchStartsAtCurrentPositionInNew == false) {
				action = Action.insert;
			}
			else if (matchStartsAtCurrentPositionInOld == false && matchStartsAtCurrentPositionInNew == true) {
				action = Action.delete;
			}
			else // This occurs if the first few words are the same in both
					// versions
			{
				action = Action.none;
			}

			if (action != Action.none) {
				operations.add(new Operation(action, positionInOld, match.StartInOld, positionInNew, match.StartInNew));
			}

			if (match.Size != 0) {
				operations.add(new Operation(Action.equal, match.StartInOld, match.getEndInOld(), match.StartInNew,
						match.getEndInNew()));

			}

			positionInOld = match.getEndInOld();
			positionInNew = match.getEndInNew();
		}

		return operations;

	}

	private List<Match> MatchingBlocks() {
		List<Match> matchingBlocks = new ArrayList<Match>();
		this.FindMatchingBlocks(0, this.oldWords.length, 0, this.newWords.length, matchingBlocks);
		return matchingBlocks;
	}

	private void FindMatchingBlocks(int startInOld, int endInOld, int startInNew, int endInNew,
			List<Match> matchingBlocks) {
		Match match = this.FindMatch(startInOld, endInOld, startInNew, endInNew);

		if (match != null) {
			if (startInOld < match.StartInOld && startInNew < match.StartInNew) {
				this.FindMatchingBlocks(startInOld, match.StartInOld, startInNew, match.StartInNew, matchingBlocks);
			}

			matchingBlocks.add(match);

			if (match.getEndInOld() < endInOld && match.getEndInNew() < endInNew) {
				this.FindMatchingBlocks(match.getEndInOld(), endInOld, match.getEndInNew(), endInNew, matchingBlocks);
			}

		}
	}

	private Match FindMatch(int startInOld, int endInOld, int startInNew, int endInNew) {
		int bestMatchInOld = startInOld;
		int bestMatchInNew = startInNew;
		int bestMatchSize = 0;

		HashMap<Integer, Integer> matchLengthAt = new HashMap<Integer, Integer>();

		for (int indexInOld = startInOld; indexInOld < endInOld; indexInOld++) {
			HashMap<Integer, Integer> newMatchLengthAt = new HashMap<Integer, Integer>();

			String index = this.oldWords[indexInOld];

			if (!this.wordIndices.containsKey(index)) {
				matchLengthAt = newMatchLengthAt;
				continue;
			}

			for (int indexInNew : this.wordIndices.get(index)) {
				if (indexInNew < startInNew) {
					continue;
				}

				if (indexInNew >= endInNew) {
					break;
				}

				int newMatchLength = (matchLengthAt.containsKey(indexInNew - 1) ? matchLengthAt.get(indexInNew - 1) : 0) + 1;
				newMatchLengthAt.put(indexInNew, newMatchLength);

				if (newMatchLength > bestMatchSize) {
					bestMatchInOld = indexInOld - newMatchLength + 1;
					bestMatchInNew = indexInNew - newMatchLength + 1;
					bestMatchSize = newMatchLength;
				}
			}

			matchLengthAt = newMatchLengthAt;
		}

		return bestMatchSize != 0 ? new Match(bestMatchInOld, bestMatchInNew, bestMatchSize) : null;
	}

	interface Func<F, T> {

		T apply(F input);
	}

	interface Func2<F, G, T> {

		T apply(F f, G g);
	}

	class Match {

		public int StartInOld;
		public int StartInNew;
		public int Size;

		public Match(int startInOld, int startInNew, int size) {
			this.StartInOld = startInOld;
			this.StartInNew = startInNew;
			this.Size = size;
		}

		public int getEndInOld() {
			return this.StartInOld + this.Size;
		}

		public int getEndInNew() {
			return this.StartInNew + this.Size;
		}
	}

	class Operation {

		public Action Action;
		public int StartInOld;
		public int EndInOld;
		public int StartInNew;
		public int EndInNew;

		public Operation(Action action, int startInOld, int endInOld, int startInNew, int endInNew) {
			this.Action = action;
			this.StartInOld = startInOld;
			this.EndInOld = endInOld;
			this.StartInNew = startInNew;
			this.EndInNew = endInNew;
		}
	}

	static enum Mode {
		character, tag, whitespace,
	}

	static enum Action {
		equal, delete, insert, none, replace
	}

}

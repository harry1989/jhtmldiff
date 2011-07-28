package com.google.code.jhtmldiff;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class DiffTest {

	private TestDiff t;
	@Before
	public void setUp() throws Exception {
		t = new TestDiff();
	}
	

	
	@Test
	public void shouldDiff() throws Exception {
	    t.diff("a word is here", "a nother word is there");
	    t.shouldEqual("a<ins class=\"diffins\"> nother</ins> word is <del class=\"diffmod\">here</del><ins class=\"diffmod\">there</ins>");
	}
	
	@Test
	public void shouldInsertALetterAndSpace() throws Exception {
	    t.diff("a c", "a b c");
	    t.shouldEqual("a <ins class=\"diffins\">b </ins>c");
	}
	
	@Test
	public void shouldRemoveALetterAndASpace() throws Exception {
	    t.diff("a b c", "a c");
	    t.shouldEqual("a <del class=\"diffdel\">b </del>c");
	}
	
	@Test
	public void shouldChangeALetter() throws Exception {
	    t.diff("a b c", "a d c");
	    t.shouldEqual("a <del class=\"diffmod\">b</del><ins class=\"diffmod\">d</ins> c");
	}
	
	
	public static class TestDiff {
		private String result;
		public void diff(String a, String b) {
			result = new Diff(a, b).Build();
		}
		public void shouldEqual(String expected) {
			assertEquals("should equal", expected, result);
		}
	}

}
/*
# coding: utf-8
require File.dirname(__FILE__) + '/spec_helper'
require 'htmldiff'

class TestDiff
  extend HTMLDiff
end

describe "htmldiff" do
  
  it "should diff text" do
    
    diff = TestDiff.diff('a word is here', 'a nother word is there')
    diff.should == "a<ins class=\"diffins\"> nother</ins> word is <del class=\"diffmod\">here</del><ins class=\"diffmod\">there</ins>"
    
  end
  
  it "should insert a letter and a space" do
    diff = TestDiff.diff('a c', 'a b c')
    diff.should == "a <ins class=\"diffins\">b </ins>c"
  end
  
  it "should remove a letter and a space" do
    diff = TestDiff.diff('a b c', 'a c')
    diff.should == "a <del class=\"diffdel\">b </del>c"
  end
  
  it "should change a letter" do
    diff = TestDiff.diff('a b c', 'a d c')
    diff.should == "a <del class=\"diffmod\">b</del><ins class=\"diffmod\">d</ins> c"
  end

  it "should support Chinese" do
    diff = TestDiff.diff('这个是中文内容, Ruby is the bast', '这是中国语内容，Ruby is the best language.')
    diff.should == "这<del class=\"diffdel\">个</del>是中<del class=\"diffmod\">文</del><ins class=\"diffmod\">国语</ins>内<del class=\"diffmod\">容, Ruby</del><ins class=\"diffmod\">容，Ruby</ins> is the <del class=\"diffmod\">bast</del><ins class=\"diffmod\">best language.</ins>"
  end
  
end
*/
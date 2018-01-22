/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * REFERENCED CODE: http://d.hatena.ne.jp/ux00ff/20110612/1307886259
 */
package me.ilovedigitalmeister.data;

/**
 * 
 * @author kazuyuf
 */
public class TrieTreeMatcher  implements IMatcher {

    private CharTreeNode rootNode = null;

    public TrieTreeMatcher(String[] words){
        this.rootNode = new CharTreeNode(null);
        this.rootNode.addWords(words);
    }

    /*
    * When Trie Tree structure is created already, we simply can initiate the TrieTreeMatcher with the given instance
    */
    public TrieTreeMatcher(CharTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Perform matching again the given String
     * @param target as the String data to be searched again the Trie Tree data
     * @return true, if matches, otherwise false
     */
    @Override
    public boolean match(String target) {

        CharTreeNode currentNode = null;
        for(int i=0;i<target.length();i++){
            currentNode = this.rootNode;
            for(int k=0;i+k<target.length();k++){
                Character c = target.charAt(i+k);
                currentNode = currentNode.child.get(c);

                if(currentNode == null){
                    break;
                }

                if(currentNode.end){
                    return true;
                }
            }
        }
        return false;
    }
}

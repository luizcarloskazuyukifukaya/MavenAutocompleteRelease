/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kazuyuf
 */

public class CharTreeNode {
    boolean end = false;
    Character value;
    Map<Character,CharTreeNode> child = new HashMap<>();
    
    CharTreeNode(Character value){
        this.value = value;
    }

    void addWords(String[] words){
        for(String word : words){
            this.addWord(word);
        }
    }

    void addWord(String word){
        addWord(word,this);
    }

    static void addWord(String value, CharTreeNode parent){
        CharTreeNode current;

        if(value == null || value.length() <= 1){
            parent.end = true;
            return;
        }

        if(parent == null || parent.end){
            return;
        }

        Character firstLetter = value.charAt(0);
        if(parent.child.containsKey(firstLetter)){
            current = parent.child.get(firstLetter);
        }else{
            current = new CharTreeNode(firstLetter);
            parent.child.put(firstLetter,current);
        }
        current.addWord(value.substring(1,value.length()));
    }
}

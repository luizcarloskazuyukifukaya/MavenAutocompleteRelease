/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ilovedigitalmeister.data;

/**
 *
 * @author kazuyuf
 */
public class ProductNameInfo {
    private String id;
    private String name;
    
    public ProductNameInfo (
            String id, 
            String name ) {
        this.id = id;
        this.name = name;        
    }
    
    public ProductNameInfo( ProductNameInfo product ) {
        id = product.id;
        name = product.name;
    }
    
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }    
}

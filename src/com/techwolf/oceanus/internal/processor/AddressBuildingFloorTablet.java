package com.techwolf.oceanus.internal.processor;

public class AddressBuildingFloorTablet {
	private String name;
    private String label;
    private int startIndex;
    
    public AddressBuildingFloorTablet() {}
    
    public AddressBuildingFloorTablet(String name, String label, int startIndex) {
    	setName(name);
    	setLabel(label);
    	setStartIndex(startIndex);
    }
    
    public void setName(String value) {
    	name = value;
    }
    
    public String getName() {
    	return name;
    }
    
    public void setLabel(String value) {
    	label = value;
    }
    
    public String getLabel() {
    	return label;
    }
    
    public void setStartIndex(int value) {
    	startIndex = value;
    }
    
    public int getStartIndex() {
    	return startIndex;
    }
}

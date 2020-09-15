package com.techwolf.oceanus.internal.processor;

public class AddressStandardizeResult {
	private BuildingDetail building = new BuildingDetail();
    private String floor = "";
    private String tablet = "";
    private String lastLabel = "";
    
    public void reset() {
    	building.reset();
    	resetFloor();
    	resetTablet();
    	resetLastLabel();
    }
    
	public void setQu(String value) {
		building.setQu(value);
		setLastLabel("qu");
	}
	
	public String getQu() {
		return building.getQu();
	}
	
	public void resetQu() {
		building.resetQu();
		resetDanyuan();
	}

	public void setZuo(String value) {
		building.setZuo(value);
		setLastLabel("zuo");
	}
	
	public String getZuo() {
		return building.getZuo();
	}
	
	public void resetZuo() {
		building.resetZuo();
		resetDanyuan();
	}
	
	public void setLou(String value) {
		building.setLou(value);
		setLastLabel("lou");
	}
	
	public String getLou() {
		return building.getLou();
	}
	
	public void resetLou() {
		building.resetLou();
		resetDanyuan();
	}
	
	public void setDanyuan(String value) {
		building.setDanyuan(value);
		setLastLabel("danyuan");
	}
	
	public String getDanyuan() {
		return building.getDanyuan();
	}
	
	public void resetDanyuan() {
		building.resetDanyuan();
		resetFloor();
	}

	public void setFloor(String value) {
		floor = value;
		setLastLabel("floor");
	}
	
	public String getFloor() {
		return floor;
	}
	
	public void resetFloor() {
		floor = "";
		resetTablet();
	}

	public void setTablet(String value) {
		tablet = value;
		setLastLabel("tablet");
	}
	
	public String getTablet() {
		return tablet;
	}
	
	public void resetTablet() {
		tablet = "";
	}
	
	public void setLastLabel(String value) {
		lastLabel = value;
	}
	
	public String getLastLabel() {
		return lastLabel;
	}

	public void resetLastLabel() {
		lastLabel = "";
	}
	
    public void copyTo(AddressStandardizeResult another) {
    	if (!getQu().equals("")) {
    		another.setQu(getQu());
    	}
    	if (!getZuo().equals("")) {
    		another.setZuo(getZuo());
    	}
    	if (!getLou().equals("")) {
    		another.setLou(getLou());
    	}
    	if (!getDanyuan().equals("")) {
    		another.setDanyuan(getDanyuan());
    	}
    	if (!floor.equals("")) {
    		another.setFloor(floor);
    	}
    	if (!tablet.equals("")) {
    		another.setTablet(tablet);
    	}
    }
}

class BuildingDetail {
	private String qu = "";
	private String zuo = "";
	private String lou = "";
	private String danyuan = "";
	
	public void reset() {
		resetQu();
		resetZuo();
		resetLou();
		resetDanyuan();
	}
	
	public void setQu(String value) {
		qu = value;
	}
	
	public String getQu() {
		return qu;
	}
	
	public void resetQu() {
		qu = "";
	}
	
	public void setZuo(String value) {
		zuo = value;
	}
	
	public String getZuo() {
		return zuo;
	}	
	
	public void resetZuo() {
		zuo = "";
	}
	
	public void setLou(String value) {
		lou = value;
	}
	
	public String getLou() {
		return lou;
	}	
	
	public void resetLou() {
		lou = "";
	}
	
	public void setDanyuan(String value) {
		danyuan = value;
	}
	
	public String getDanyuan() {
		return danyuan;
	}
	
	public void resetDanyuan() {
		danyuan = "";
	}
}
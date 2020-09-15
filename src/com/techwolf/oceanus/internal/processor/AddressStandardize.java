package com.techwolf.oceanus.internal.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressStandardize {
	public List<AddressStandardizeResult> standardize(List<AddressBuildingFloorTablet> address) {
		address.sort((a, b) -> a.getStartIndex() - b.getStartIndex());
		List<AddressStandardizeResult> standard = new ArrayList<>();
		AddressStandardizeResult result = new AddressStandardizeResult();
		
		for (int ind = 0; ind < address.size(); ind += 1) {
			AddressBuildingFloorTablet addr = address.get(ind);
			String stdAddr = chooseMethod(addr.getName(), addr.getLabel());
			if (ind == 0) {
				result.reset();
			}
			putIntoResult(stdAddr, addr.getLabel(), result, standard);
			if (ind == address.size() - 1) {
				standard.add(result);
			}
		}
		return standard;
	}
	
	private String chooseMethod(String content, String label) {
		if (label.equals("building")) {
			return standardizeBuilding(content);
		}else if (label.equals("floor")) {
			return standardizeFloor(content);
		}else if (label.equals("tablet")) {
			return standardizeTablet(content);
		}else {
			return content;
		}
	}
	
	private void putIntoResult(String content, String label, AddressStandardizeResult result, List<AddressStandardizeResult> standard) {
		if (label.equals("building")) {
			String[] contentSplit = content.split(",");
			for (String ctn: contentSplit) {
				if (ctn.endsWith("门市")) {
					putSingleIntoResult(ctn, "tablet", result, standard);
				}else {
					putSingleIntoResult(ctn, label, result, standard);
				}
			}
		}else if (label.equals("floor")) {
			String[] contentSplit = content.split(",");
			for (String ctn: contentSplit) {
				putSingleIntoResult(ctn, label, result, standard);
			}
		}else if (label.equals("tablet")) {
			String[] contentSplit = content.split(",");
			for (String ctn: contentSplit) {
				if (ctn.endsWith("层")) {
					putSingleIntoResult(ctn, "floor", result, standard);
				}else {
					putSingleIntoResult(ctn, label, result, standard);
				}
			}
		}
	}
	
	private void putSingleIntoResult(String content, String label, AddressStandardizeResult result, List<AddressStandardizeResult> standard) {
		if (label.equals("building")) {
			if (content.endsWith("区")) {
				if (whetherCopy("qu", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetQu();
				}
				result.setQu(content);
			}else if (content.endsWith("座")) {
				if (whetherCopy("zuo", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetZuo();
				}
				result.setZuo(content);
			}else if (content.endsWith("单元")) {
				if (whetherCopy("danyuan", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetDanyuan();
				}
				result.setDanyuan(content);
			}else {
				if (whetherCopy("lou", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetLou();
				}
				result.setLou(content);
			}
		}else if (label.equals("floor")) {
			if (whetherCopy("floor", result.getLastLabel())) {
				AddressStandardizeResult truck = new AddressStandardizeResult();
				result.copyTo(truck);
				standard.add(truck);
				result.resetFloor();
			}
			result.setFloor(content);
		}else if (label.equals("tablet")) {
			if (whetherCopy("tablet", result.getLastLabel())) {
				AddressStandardizeResult truck = new AddressStandardizeResult();
				result.copyTo(truck);
				standard.add(truck);
				result.resetTablet();
			}
			result.setTablet(content);
		}
	}
	
	private boolean whetherCopy(String contentLabel, String resultLabel) {
		int contentLabelIndex = labelToInt(contentLabel);
		int resultLabelIndex = labelToInt(resultLabel);
		if (contentLabelIndex == 0 && resultLabelIndex == 0) {
			if (contentLabel == resultLabel) {
				return true;
			}else {
				return false;
			}
		}
		return contentLabelIndex <= resultLabelIndex;
	}
	
	private int labelToInt(String label) {
		int labelIndex = -1;
		if (label.equals("qu") || label.equals("zuo") || label.equals("lou")) {
			labelIndex = 0;
		}else if (label.equals("danyuan")) {
			labelIndex = 1;
		}else if (label.equals("floor")) {
			labelIndex = 2;
		}else if (label.equals("tablet")) {
			labelIndex = 3;
		}
		return labelIndex;
	}
	
   	public String standardizeBuilding(String building) {
		StringBuffer standard = new StringBuffer();
		if (building.endsWith("第")) {
			building = building.substring(0, building.length() - 2);
		}
		if (building.endsWith("座")) {
			// 处理 xx座
			String[] buildingSplit = building.split("[,，、.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				b = replaceCore(b);
				b = delDiAndZeroAtHead(b);
				if (!b.endsWith("座")) {
					b = b + "座";
				}
				standard.append(b + ",");
			}
		}else if (building.matches(".+[(单元)门]$")) {
			// 处理 xx单元
			if (building.matches("^[\\da-zA-Z]+#*-\\d+、\\d+单元$")) {
				// 处理特殊模式 xx栋-oo单元
				String[] buildingSplit = building.split("-");
				standard.append(standardizeBuilding(buildingSplit[0]) + ",");
				standard.append(standardizeBuilding(buildingSplit[1]) + ",");
			}else {
				String[] buildingSplit = building.split("[,，、.]");
				for (String b: buildingSplit) {
					if (b.equals("")) {
						continue;
					}
					b = replaceCore(b);
					b = delDiAndZeroAtHead(b);
					b = addStdBuildingSuffixForDanyuan(b);
					standard.append(b + ",");
				}
			}
		}else if (building.matches(".+[区地期块排组馆院]$")) {
			// 处理 xx区
			String[] buildingSplit = building.split("[,，、.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				String original = b;
				b = replaceCore(b);
				b = getCore(b, "[东西南北]{0,2}[\\dA-Z\\-]+");
				if (b.equals("")) {
					standard.append(original + ",");
				}else {
					b = b + "区";
					standard.append(b + ",");
				}
			}
		}else {
			// 处理 xx栋，默认未被上述模式匹配到的部分均为栋
			String[] buildingSplit = building.split("[,，、.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				String original = b;
				b = b.replaceAll("[【】]", "");
				if (b.matches("^[\\d一二三四五六七八九十a-zA-Z]+[#号]*(厂房|车间)$")) {
					if (b.endsWith("厂房")) {
						b = b.replaceFirst("[#号]*厂房", "号厂房");
					}else {
						b = b.replaceFirst("[#号]*车间", "号车间");
					}
					b = replaceCore(b);
					b = delDiAndZeroAtHead(b);
					standard.append(b + ",");
					continue;
				}
				if (b.matches("^[\\d一二三四五六七八九十a-zA-Z]+[#号]门市$")) {
					b = b.replaceFirst("#", "号");
					b = replaceCore(b);
					b = delDiAndZeroAtHead(b);
					standard.append(b + ",");
					continue;
				}
				if (b.matches("^[\\d一二三四五六七八九十a-zA-Z]+#-[\\d一二三四五六七八九十a-zA-Z]+[#号]门市$")) {
					String[] subs = b.split("-");
					standard.append(standardizeBuilding(subs[0]) + ",");
					standard.append(standardizeBuilding(subs[1]) + ",");
					continue;
				}
				if (b.matches("^[\\d一二三四五六七八九十a-zA-Z]+#-[\\d一二三四五六七八九十a-zA-Z]+[栋幢号]楼*$")) {
					String[] subs = b.split("-");
					standard.append(standardizeBuilding(subs[0]) + ",");
					standard.append(standardizeBuilding(subs[1]) + ",");
					continue;
				}
				if (b.matches("^[第附临甲乙丙丁东西南北]{0,2}[\\da-zA-Z一二三四五六七八九十\\-]+[#栋幢号主商(写字)(商业)(商住)(办公)]*楼*$")) {
					// 处理普通栋模式
					b = replaceCore(b);
					b = getCore(b, "[\\dA-Z\\-]+") + "栋";
					b = delDiAndZeroAtHead(b);
					standard.append(b + ",");
					continue;
				}
				standard.append(original + ",");
			}
		}

		standard = standard.deleteCharAt(standard.length() - 1);
		return standard.toString();
	}
	
	public String standardizeFloor(String floor) {
		StringBuffer standard = new StringBuffer();
		String[] floorSplit = floor.split("[,，、和]");
		for (String f: floorSplit) {
			if (f.equals("")) {
				continue;
			}
			if ( f.matches(".*[-至到].*")) {
				// 处理连续楼层，结果中包含关键字“至”，表示两边的楼层分别是起始楼层和终止楼层
				String[] subs = f.split("[-至到]");
				for (int s = 0; s < subs.length; s += 1) {
					if (!subs[s].equals("")) {
						if (s > 0 && !subs[s-1].equals("")) {
							// 生成中间层
							String startString = getCore(subs[s-1], "\\d+");
							String endString = getCore(subs[s], "\\d+");
							if (!startString.equals("") && !endString.equals("")) {
								int start = Integer.parseInt(startString);
								int end = Integer.parseInt(endString);
								for (int mid = start + 1; mid < end; mid += 1) {
									String subRes = standardizeFloor(Integer.toString(mid));
									standard.append(subRes + ",");
								}
							}
						}
						String subRes = standardizeFloor(subs[s]);
						standard.append(subRes + ",");
					}
				}
				continue;
			}
			f = f.replaceAll("[\\pP‘’“”]", "");  // 删除中英文标点符号
			String original = f;
			if (f.equals("底商") || f.equals("首层")) {
				standard.append("1层" + ",");
				continue;
			}
			if (f.matches("^[第东西南北]半*.+")) {
				f = f.replaceFirst("^[第东西南北]半*", "");
			}
			if (f.matches("地下负*.+")) {
				f = f.replaceFirst("地下负*", "负");
			}
			if (f.matches("^B[0-9]+[层楼]*$")) {
				f = f.replaceFirst("B", "负");
				f = addStdFloorSuffix(f);
				f = delDiAndZeroAtHead(f);
				standard.append(f + ",");
				continue;
			}
			if (f.matches("^[0-9]+F[层楼]*$")) {
				f = f.replaceFirst("F[层楼]*", "层");
				f = delDiAndZeroAtHead(f);
				standard.append(f + ",");
				continue;
			}
			if (f.matches("^负{0,1}[0-9a-zA-Z一二三四五六七八九十零壹贰叁肆伍陆柒捌玖拾]+[层楼]*$")) {
				f = replaceCore(f);
				f = delDiAndZeroAtHead(f);
				f = addStdFloorSuffix(f);
				standard.append(f + ",");
				continue;
			}
		    standard.append(original + ",");  // 未匹配到任何已知常见模式，返回原始字符串
		}
		standard = standard.deleteCharAt(standard.length() - 1);
		return standard.toString();
	}
	
	public String standardizeTablet(String tablet, boolean extend) {
		StringBuffer standard = new StringBuffer();
		tablet = tablet.replaceFirst("第", "");
		if (tablet.matches("^[附第临(自编)]*[\\da-zA-Z一二三四五六七八九十]+号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$")) {
			tablet = replaceCore(tablet);
			tablet = getCore(tablet, "[\\dA-Z]+") + "室";
			tablet = delDiAndZeroAtHead(tablet);
			if (extend) {
				// 根据 xx室 的模式，生成 oo层
				String coreNum = getCore(tablet, "[\\d]+");
				String extendFloor = getExtendFloor(coreNum);
				if (!extendFloor.equals("")) {
					standard.append(standardizeFloor(extendFloor) + ",");
				}
			}
			standard.append(tablet + ",");
		}else if (tablet.matches("^[a-zA-Z]{0,1}\\d{1}-\\d{1}号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{2}-\\d{2}号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{3}-\\d{3}号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{4}-\\d{4}号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$")) {
			// 处理特殊连续室模式
			String prefix = getCore(tablet, "^[a-zA-Z]");
			String[] tabletSplit = tablet.split("-");
			for (int t = 0; t < tabletSplit.length; t += 1) {
				if (tabletSplit[t].equals("")) {
					continue;
				}
				if (t > 0 && !tabletSplit[t-1].equals("")) {
					// 生成中间层
					String startString = getCore(tabletSplit[t-1], "\\d+");
					String endString = getCore(tabletSplit[t], "\\d+");
					if (!startString.equals("") && !endString.equals("")) {
						int start = Integer.parseInt(startString);
						int end = Integer.parseInt(endString);
						for (int mid = start + 1; mid < end; mid += 1) {
							if (!prefix.equals("")) {
								String subRes = standardizeTablet(prefix + Integer.toString(mid), extend);
								standard.append(subRes + ",");
							}else {
								String subRes = standardizeTablet(Integer.toString(mid), extend);
								standard.append(subRes + ",");
							}
						}
					}
				}
				if (t > 0 && !prefix.equals("")) {
					tabletSplit[t] = prefix + tabletSplit[t];
				}
				String res = standardizeTablet(tabletSplit[t], extend);
				standard.append(res + ",");
			}
		}else if (tablet.matches("^[a-zA-Z]{0,1}([\\d\\-]*[、,，]+[\\d\\-]*)+号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$")) {
			// 处理特殊非连续室模式
			String prefix = getCore(tablet, "^[a-zA-Z]");
			String[] tabletSplit = tablet.split("[、,，]");
			for (int t = 0; t < tabletSplit.length; t += 1) {
				if (tabletSplit[t].equals("")) {
					continue;
				}
				if (t > 0 && !prefix.equals("")) {
					tabletSplit[t] = prefix + tabletSplit[t];
				}
				String res = standardizeTablet(tabletSplit[t], extend);
				standard.append(res + ",");
			}
		}else {
			// 处理普通室模式
			String[] tabletSplit = tablet.split("[、,，]");
			for (String subt: tabletSplit) {
				if (subt.equals("")) {
					continue;
				}
				if (subt.matches(".*[至到].*")) {
					// 处理普通连续模式
					String[] subtSplit = subt.split("[至到]");
					for (int t = 0; t < subtSplit.length; t += 1) {
						if (subtSplit[t].equals("")) {
							continue;
						}
						if (t > 0 && !subtSplit[t-1].equals("")) {
							// 生成中间层
							String startString = getCore(subtSplit[t-1], "\\d+");
							String endString = getCore(subtSplit[t], "\\d+");
							if (!startString.equals("") && !endString.equals("")) {
								int start = Integer.parseInt(startString);
								int end = Integer.parseInt(endString);
								for (int mid = start + 1; mid < end; mid += 1) {
									String subRes = standardizeTablet(Integer.toString(mid), extend);
									standard.append(subRes + ",");
								}
							}
						}
						String res = standardizeTablet(subtSplit[t], extend);
						standard.append(res + ",");
					}
				}else {
					// 处理普通非连续模式
					String original = subt;
					if (subt.matches("^[附第临(自编)]*[\\da-zA-Z\\-]+号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*$")) {
						subt = replaceCore(subt);
						subt = getCore(subt, "[\\dA-Z\\-]+") + "室";
						subt = delDiAndZeroAtHead(subt);
						standard.append(subt + ",");
						continue;
					}
					subt = subt.replaceAll("之", "");
					if (subt.matches("^([附第临(自编)]*[\\da-zA-Z\\-]+号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*)+$")) {
						// 处理循环模式，与连续模式不同
						Pattern p = Pattern.compile("[附第临(自编)]*[\\da-zA-Z\\-]+号*[商(办公)(门面)]*[室间房铺户摊内]*[位间]*");
						Matcher m = p.matcher(subt);
						int t = 0;
						while (m.find()) {
							String res = standardizeTablet(m.group(0), t == 0 && extend);
							standard.append(res);  // 循环模式直接拼接成一个部分，中间不加间隔符
							t += 1;
						}
						standard.append(",");
						continue;
					}
					standard.append(original + ",");
				}
			}
		}
		standard = standard.deleteCharAt(standard.length() - 1);
		return standard.toString();
	}
	
	public String standardizeTablet(String tablet) {
		String standard = standardizeTablet( tablet, false );
		return standard;
	}
	
	private String delDiAndZeroAtHead(String content) {
		if (content.startsWith("第")) {
			content = content.replaceFirst("第", "");
		}
		if (content.matches("^[^\\d]*0+.+")) {
			String prefix = getCore(content, "^[^\\d]+");
			content = content.replaceFirst("^[^\\d]*0+-*", "");
			content = prefix + content;
		}
		return content;
	}
	
	private String replaceCore(String content) {
		content = content.toUpperCase();
		content = content.replaceAll("[〇零]", "0");
		content = content.replaceAll("[一壹]", "1");
		content = content.replaceAll("[二贰]", "2");
		content = content.replaceAll("[三叁]", "3");
		content = content.replaceAll("[四肆]", "4");
		content = content.replaceAll("[五伍]", "5");
		content = content.replaceAll("[六陆]", "6");
		content = content.replaceAll("[七柒]", "7");
		content = content.replaceAll("[八捌]", "8");
		content = content.replaceAll("[九玖]", "9");
		int ind = content.indexOf("十");
		int last = content.length() - 1;
		if (ind == 0 || (ind > 0 && !coreNumContain(content, ind-1, ind))) {
			if (ind == last || (ind < last && !coreNumContain(content, ind+1, ind+2))) {
				content = content.replaceFirst("十", "10");
			}else {
				content = content.replaceFirst("十", "1");
			}
		}else {
			if (ind == last || (ind < last && !coreNumContain(content, ind+1, ind+2))) {
				content = content.replaceFirst("十", "0");
			}else {
				content = content.replaceFirst("十", "");
			}
		}
		return content;
	}
	
	private boolean coreNumContain(String coreChar, int start, int end) {
		int indicator = "0123456789".indexOf(coreChar.substring(start, end));
		if (indicator == -1) {
			return false;
		}else {
			return true;
		}
	}

	private String addStdFloorSuffix(String content) {
		if (content.endsWith("楼")) {
			content = content.replaceFirst("楼", "层");
		}else if (!content.endsWith("层")) {
			content = content + "层";
		}
		return content;
	}
	
	private String addStdBuildingSuffixForDanyuan(String content) {
		if (content.endsWith("门")) {
			content = content.replaceFirst("号*门", "单元");
		}else if (!content.endsWith("单元")) {
			content = content + "单元";
		}
		return content;
	}
	
	private String getCore(String content, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		if (m.find()) {
			return m.group(0);
		}else {
			return "";
		}
	}
	
	private String getExtendFloor(String content) {
		StringBuffer extendFloor = new StringBuffer();
		if (content.length() == 3) {
			extendFloor.append(content.substring(0, 1));
		}else if (content.length() == 4) {
			extendFloor.append(content.substring(0, 2));
		}else if (content.length() == 5 && (content.startsWith("8") || content.startsWith("6"))) {
			extendFloor.append(content.substring(1, 3));
		}else if (content.length() >= 5) {
			extendFloor.append(content.substring(0, 2));
		}
		return extendFloor.toString();
	}
	
	public void standardizeFloorExample() {
		String[] floorSamples = new String[] {"2层", "15层", "二层", "0二层", "底商", "五楼", "3-7层", "负一层", 
				"首层", "1-2楼", "北半层", "3组", "壹层", "一、二、三层", "一间", "至3层", "F铺", ",2楼", "地下一层", 
				"全层", "二车间一层", "一至四层", "主楼", "4至33层", "6F", "1#楼", "整层", "南四层", "、三楼", "【4】层", 
				"B1层", "地下负一楼", "第六层", "二首层", "4F层"};
		for (String sm: floorSamples) {
			String res = standardizeFloor(sm);
			System.out.println(sm + " --> " + res);
		}
	}

	public void standardizeBuildingExample() {
		String[] buildingSamples = new String[] {"西座", "F座", "、B座", "十八座", "负座", "2单元", "8号门", 
				"B2单元", "1-2单元", "、102单元", "西单元", "04、05单元", "A43-3、4单元", "A区", "二区", "b4区", 
				"北区", "F2-001-1货区", "K3地块第", "K4地", "A2地块7栋", "一期", "1-13地块", "六街区", "C馆", 
				"103地区", "64号小区", "二排", "17-27号院", "6排", "10幢", "19号", "B栋", "1#、2#楼", "六号楼", 
				"1栋", "A栋", "61栋", "B幢", "C1主楼", "104店", "4号房、4号", "甲8号", "GJ-15号楼", "S430号", 
				"东10", "1号写字楼", "1号商住楼", "综合楼", "1-2", "、8幢", "B1栋", "附22号", "8-110", "2-49号", 
				"5号铺", "西1幢", "裙楼", "7#厂房", "三车间", "A商业", "一、二楼", "C1-6号楼", "26号门市", "二十一号办公楼", 
				"1-2幢", "A-2栋", "K4-1-6幢", "附属楼", "A1509", "1#-2幢", "乙26号", "6套房", "4#商", "南02号", 
				"1-4-602门厅", "第一幢", "附6、附7号", "38.39号楼", "4#-一号门市", "西南楼", "临1号", "11【幢"};
		for (String sm: buildingSamples) {
			String res = standardizeBuilding(sm);
			System.out.println(sm + " --> " + res);
		}
	}
	
	public void standardizeTabletExample() {
		String[] tabletSamples = new String[] {"104室", "A203、204-1、204-2、205号铺位", "106-109号", "105号", 
				"0188", "101", "226号10-2", "A03", "5号", "A601-602-C", "819房", "2002H室", "C-070-2", 
				"26B铺", "101内-01号", "24-B-1", "A01号", "4-4-411室", "B-11号1-2号", "C-073号铺", "01办公室", 
				"D2054柜位", "312、313、314", "109卡", "601内611室", "B108号商铺", "07、08摊位", "10-A", "2号附40号", 
				"A002S205F1001专", "D号", "1506-1509号", "603、633", "101内", "715-03室", "A508-A52", "L2-047号", 
				"3-4、16-18号", "133-134a", "1-1818室", "0-108A商铺", "301-310、319-320室", "1906室012号", "附2", 
				"11-3门面", "101号房之16房A125", "17#901室", "A4179W192房间", "附11号", "1601房自编03房", "107户"};
		for (String sm: tabletSamples) {
			String res = standardizeTablet(sm);
			String resEx = standardizeTablet(sm, true);
			System.out.println(sm + " --> " + res + " / " + resEx);
		}
	}
	
	public void standardizeExample() {
		String raw = "A区1号楼2，3单元5-7层701、702号，3幢3层A401-405号";
		List<AddressBuildingFloorTablet> sample = new ArrayList<AddressBuildingFloorTablet>();
		AddressBuildingFloorTablet part1 = new AddressBuildingFloorTablet("A区", "building", 10);
		AddressBuildingFloorTablet part2 = new AddressBuildingFloorTablet("1号楼", "building", 12);
		AddressBuildingFloorTablet part3 = new AddressBuildingFloorTablet("2，3单元", "building", 15);
		AddressBuildingFloorTablet part4 = new AddressBuildingFloorTablet("5-7层", "floor", 20);
		AddressBuildingFloorTablet part5 = new AddressBuildingFloorTablet("701、702号", "tablet", 24);
		AddressBuildingFloorTablet part6 = new AddressBuildingFloorTablet("，3幢", "building", 32);
		AddressBuildingFloorTablet part7 = new AddressBuildingFloorTablet("4层", "floor", 34);
		AddressBuildingFloorTablet part8 = new AddressBuildingFloorTablet("A401-405号", "tablet", 36);
		sample.add(part1);
		sample.add(part2);
		sample.add(part3);
		sample.add(part4);
		sample.add(part5);
		sample.add(part6);
		sample.add(part7);
		sample.add(part8);
		List<AddressStandardizeResult> standard = standardize(sample);
		System.out.println("raw Address: " + raw);
		for (AddressStandardizeResult result: standard) {
			System.out.println(result.hashCode() + " | qu: " + result.getQu());
			System.out.println(result.hashCode() + " | zuo: " + result.getZuo());
			System.out.println(result.hashCode() + " | lou: " + result.getLou());
			System.out.println(result.hashCode() + " | danyuan: " + result.getDanyuan());
			System.out.println(result.hashCode() + " | floor: " + result.getFloor());
			System.out.println(result.hashCode() + " | tablet: " + result.getTablet());
		}
	}
	
	public void help() {
		String helpInfo = 
				"API document:\n"
				+ "\tString standardizeBuilding(String addr): 标准化building，解析成功则返回x区、x座、x单元、x栋，否则返回原值，\n"
				+ "\t\t示例参见standardizeBuildingExample();\n"
				+ "\tString standardizeFloor(String addr): 标准化floor，解析成功返回x层，否则返回原值；可以解析连续楼层，返回关键字\n"
				+ "\t\t‘至’，表示两边的楼层是起始和终点楼层，示例参见standardizeFloorExample;\n"
				+ "\tString standarddizeTablet(String addr, boolean extend): 标准化tablet，解析成功返回x室，否则返回原值；\n"
				+ "\t\t可以解析连续楼层，返回关键字‘至’，表示两边的门牌是起始和终点门牌；extend关键字控制是否尝试生成楼层，置为true则尝试\n"
				+ "\t\t从当前门牌中解析出对应的楼层，但不保证一定解析成功，应在没有FLOOR时调用，示例参见standardizeTabletExample;\n"
				+ "\tString standarddizeTablet(String addr): 标准化tablet，解析成功返回x室，否则返回原值；可以解析连续楼层，返回关\n"
				+ "\t\t键字‘至’，表示两边的门牌是起始和终点门牌；默认不尝试生成楼层，示例参见standardizeTabletExample。\n";
		System.out.println(helpInfo);
	}
	
	public static void main(String[] args) {
		AddressStandardize agent = new AddressStandardize();
		
		agent.help();
		agent.standardizeBuildingExample();
		agent.standardizeFloorExample();
		agent.standardizeTabletExample();
		agent.standardizeExample();
	}

}

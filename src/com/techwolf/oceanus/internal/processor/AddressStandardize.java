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
				if (ctn.endsWith("����")) {
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
				if (ctn.endsWith("��")) {
					putSingleIntoResult(ctn, "floor", result, standard);
				}else {
					putSingleIntoResult(ctn, label, result, standard);
				}
			}
		}
	}
	
	private void putSingleIntoResult(String content, String label, AddressStandardizeResult result, List<AddressStandardizeResult> standard) {
		if (label.equals("building")) {
			if (content.endsWith("��")) {
				if (whetherCopy("qu", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetQu();
				}
				result.setQu(content);
			}else if (content.endsWith("��")) {
				if (whetherCopy("zuo", result.getLastLabel())) {
					AddressStandardizeResult truck = new AddressStandardizeResult();
					result.copyTo(truck);
					standard.add(truck);
					result.resetZuo();
				}
				result.setZuo(content);
			}else if (content.endsWith("��Ԫ")) {
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
		if (building.endsWith("��")) {
			building = building.substring(0, building.length() - 2);
		}
		if (building.endsWith("��")) {
			// ���� xx��
			String[] buildingSplit = building.split("[,����.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				b = replaceCore(b);
				b = delDiAndZeroAtHead(b);
				if (!b.endsWith("��")) {
					b = b + "��";
				}
				standard.append(b + ",");
			}
		}else if (building.matches(".+[(��Ԫ)��]$")) {
			// ���� xx��Ԫ
			if (building.matches("^[\\da-zA-Z]+#*-\\d+��\\d+��Ԫ$")) {
				// ��������ģʽ xx��-oo��Ԫ
				String[] buildingSplit = building.split("-");
				standard.append(standardizeBuilding(buildingSplit[0]) + ",");
				standard.append(standardizeBuilding(buildingSplit[1]) + ",");
			}else {
				String[] buildingSplit = building.split("[,����.]");
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
		}else if (building.matches(".+[�����ڿ������Ժ]$")) {
			// ���� xx��
			String[] buildingSplit = building.split("[,����.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				String original = b;
				b = replaceCore(b);
				b = getCore(b, "[�����ϱ�]{0,2}[\\dA-Z\\-]+");
				if (b.equals("")) {
					standard.append(original + ",");
				}else {
					b = b + "��";
					standard.append(b + ",");
				}
			}
		}else {
			// ���� xx����Ĭ��δ������ģʽƥ�䵽�Ĳ��־�Ϊ��
			String[] buildingSplit = building.split("[,����.]");
			for (String b: buildingSplit) {
				if (b.equals("")) {
					continue;
				}
				String original = b;
				b = b.replaceAll("[����]", "");
				if (b.matches("^[\\dһ�����������߰˾�ʮa-zA-Z]+[#��]*(����|����)$")) {
					if (b.endsWith("����")) {
						b = b.replaceFirst("[#��]*����", "�ų���");
					}else {
						b = b.replaceFirst("[#��]*����", "�ų���");
					}
					b = replaceCore(b);
					b = delDiAndZeroAtHead(b);
					standard.append(b + ",");
					continue;
				}
				if (b.matches("^[\\dһ�����������߰˾�ʮa-zA-Z]+[#��]����$")) {
					b = b.replaceFirst("#", "��");
					b = replaceCore(b);
					b = delDiAndZeroAtHead(b);
					standard.append(b + ",");
					continue;
				}
				if (b.matches("^[\\dһ�����������߰˾�ʮa-zA-Z]+#-[\\dһ�����������߰˾�ʮa-zA-Z]+[#��]����$")) {
					String[] subs = b.split("-");
					standard.append(standardizeBuilding(subs[0]) + ",");
					standard.append(standardizeBuilding(subs[1]) + ",");
					continue;
				}
				if (b.matches("^[\\dһ�����������߰˾�ʮa-zA-Z]+#-[\\dһ�����������߰˾�ʮa-zA-Z]+[������]¥*$")) {
					String[] subs = b.split("-");
					standard.append(standardizeBuilding(subs[0]) + ",");
					standard.append(standardizeBuilding(subs[1]) + ",");
					continue;
				}
				if (b.matches("^[�ڸ��ټ��ұ��������ϱ�]{0,2}[\\da-zA-Zһ�����������߰˾�ʮ\\-]+[#����������(д��)(��ҵ)(��ס)(�칫)]*¥*$")) {
					// ������ͨ��ģʽ
					b = replaceCore(b);
					b = getCore(b, "[\\dA-Z\\-]+") + "��";
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
		String[] floorSplit = floor.split("[,������]");
		for (String f: floorSplit) {
			if (f.equals("")) {
				continue;
			}
			if ( f.matches(".*[-����].*")) {
				// ��������¥�㣬����а����ؼ��֡���������ʾ���ߵ�¥��ֱ�����ʼ¥�����ֹ¥��
				String[] subs = f.split("[-����]");
				for (int s = 0; s < subs.length; s += 1) {
					if (!subs[s].equals("")) {
						if (s > 0 && !subs[s-1].equals("")) {
							// �����м��
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
			f = f.replaceAll("[\\pP��������]", "");  // ɾ����Ӣ�ı�����
			String original = f;
			if (f.equals("����") || f.equals("�ײ�")) {
				standard.append("1��" + ",");
				continue;
			}
			if (f.matches("^[�ڶ����ϱ�]��*.+")) {
				f = f.replaceFirst("^[�ڶ����ϱ�]��*", "");
			}
			if (f.matches("���¸�*.+")) {
				f = f.replaceFirst("���¸�*", "��");
			}
			if (f.matches("^B[0-9]+[��¥]*$")) {
				f = f.replaceFirst("B", "��");
				f = addStdFloorSuffix(f);
				f = delDiAndZeroAtHead(f);
				standard.append(f + ",");
				continue;
			}
			if (f.matches("^[0-9]+F[��¥]*$")) {
				f = f.replaceFirst("F[��¥]*", "��");
				f = delDiAndZeroAtHead(f);
				standard.append(f + ",");
				continue;
			}
			if (f.matches("^��{0,1}[0-9a-zA-Zһ�����������߰˾�ʮ��Ҽ��������½��ƾ�ʰ]+[��¥]*$")) {
				f = replaceCore(f);
				f = delDiAndZeroAtHead(f);
				f = addStdFloorSuffix(f);
				standard.append(f + ",");
				continue;
			}
		    standard.append(original + ",");  // δƥ�䵽�κ���֪����ģʽ������ԭʼ�ַ���
		}
		standard = standard.deleteCharAt(standard.length() - 1);
		return standard.toString();
	}
	
	public String standardizeTablet(String tablet, boolean extend) {
		StringBuffer standard = new StringBuffer();
		tablet = tablet.replaceFirst("��", "");
		if (tablet.matches("^[������(�Ա�)]*[\\da-zA-Zһ�����������߰˾�ʮ]+��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$")) {
			tablet = replaceCore(tablet);
			tablet = getCore(tablet, "[\\dA-Z]+") + "��";
			tablet = delDiAndZeroAtHead(tablet);
			if (extend) {
				// ���� xx�� ��ģʽ������ oo��
				String coreNum = getCore(tablet, "[\\d]+");
				String extendFloor = getExtendFloor(coreNum);
				if (!extendFloor.equals("")) {
					standard.append(standardizeFloor(extendFloor) + ",");
				}
			}
			standard.append(tablet + ",");
		}else if (tablet.matches("^[a-zA-Z]{0,1}\\d{1}-\\d{1}��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{2}-\\d{2}��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{3}-\\d{3}��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$") 
				|| tablet.matches("^[a-zA-Z]{0,1}\\d{4}-\\d{4}��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$")) {
			// ��������������ģʽ
			String prefix = getCore(tablet, "^[a-zA-Z]");
			String[] tabletSplit = tablet.split("-");
			for (int t = 0; t < tabletSplit.length; t += 1) {
				if (tabletSplit[t].equals("")) {
					continue;
				}
				if (t > 0 && !tabletSplit[t-1].equals("")) {
					// �����м��
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
		}else if (tablet.matches("^[a-zA-Z]{0,1}([\\d\\-]*[��,��]+[\\d\\-]*)+��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$")) {
			// ���������������ģʽ
			String prefix = getCore(tablet, "^[a-zA-Z]");
			String[] tabletSplit = tablet.split("[��,��]");
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
			// ������ͨ��ģʽ
			String[] tabletSplit = tablet.split("[��,��]");
			for (String subt: tabletSplit) {
				if (subt.equals("")) {
					continue;
				}
				if (subt.matches(".*[����].*")) {
					// ������ͨ����ģʽ
					String[] subtSplit = subt.split("[����]");
					for (int t = 0; t < subtSplit.length; t += 1) {
						if (subtSplit[t].equals("")) {
							continue;
						}
						if (t > 0 && !subtSplit[t-1].equals("")) {
							// �����м��
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
					// ������ͨ������ģʽ
					String original = subt;
					if (subt.matches("^[������(�Ա�)]*[\\da-zA-Z\\-]+��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*$")) {
						subt = replaceCore(subt);
						subt = getCore(subt, "[\\dA-Z\\-]+") + "��";
						subt = delDiAndZeroAtHead(subt);
						standard.append(subt + ",");
						continue;
					}
					subt = subt.replaceAll("֮", "");
					if (subt.matches("^([������(�Ա�)]*[\\da-zA-Z\\-]+��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*)+$")) {
						// ����ѭ��ģʽ��������ģʽ��ͬ
						Pattern p = Pattern.compile("[������(�Ա�)]*[\\da-zA-Z\\-]+��*[��(�칫)(����)]*[�Ҽ䷿�̻�̯��]*[λ��]*");
						Matcher m = p.matcher(subt);
						int t = 0;
						while (m.find()) {
							String res = standardizeTablet(m.group(0), t == 0 && extend);
							standard.append(res);  // ѭ��ģʽֱ��ƴ�ӳ�һ�����֣��м䲻�Ӽ����
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
		if (content.startsWith("��")) {
			content = content.replaceFirst("��", "");
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
		content = content.replaceAll("[����]", "0");
		content = content.replaceAll("[һҼ]", "1");
		content = content.replaceAll("[����]", "2");
		content = content.replaceAll("[����]", "3");
		content = content.replaceAll("[����]", "4");
		content = content.replaceAll("[����]", "5");
		content = content.replaceAll("[��½]", "6");
		content = content.replaceAll("[����]", "7");
		content = content.replaceAll("[�˰�]", "8");
		content = content.replaceAll("[�ž�]", "9");
		int ind = content.indexOf("ʮ");
		int last = content.length() - 1;
		if (ind == 0 || (ind > 0 && !coreNumContain(content, ind-1, ind))) {
			if (ind == last || (ind < last && !coreNumContain(content, ind+1, ind+2))) {
				content = content.replaceFirst("ʮ", "10");
			}else {
				content = content.replaceFirst("ʮ", "1");
			}
		}else {
			if (ind == last || (ind < last && !coreNumContain(content, ind+1, ind+2))) {
				content = content.replaceFirst("ʮ", "0");
			}else {
				content = content.replaceFirst("ʮ", "");
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
		if (content.endsWith("¥")) {
			content = content.replaceFirst("¥", "��");
		}else if (!content.endsWith("��")) {
			content = content + "��";
		}
		return content;
	}
	
	private String addStdBuildingSuffixForDanyuan(String content) {
		if (content.endsWith("��")) {
			content = content.replaceFirst("��*��", "��Ԫ");
		}else if (!content.endsWith("��Ԫ")) {
			content = content + "��Ԫ";
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
		String[] floorSamples = new String[] {"2��", "15��", "����", "0����", "����", "��¥", "3-7��", "��һ��", 
				"�ײ�", "1-2¥", "�����", "3��", "Ҽ��", "һ����������", "һ��", "��3��", "F��", ",2¥", "����һ��", 
				"ȫ��", "������һ��", "һ���Ĳ�", "��¥", "4��33��", "6F", "1#¥", "����", "���Ĳ�", "����¥", "��4����", 
				"B1��", "���¸�һ¥", "������", "���ײ�", "4F��"};
		for (String sm: floorSamples) {
			String res = standardizeFloor(sm);
			System.out.println(sm + " --> " + res);
		}
	}

	public void standardizeBuildingExample() {
		String[] buildingSamples = new String[] {"����", "F��", "��B��", "ʮ����", "����", "2��Ԫ", "8����", 
				"B2��Ԫ", "1-2��Ԫ", "��102��Ԫ", "����Ԫ", "04��05��Ԫ", "A43-3��4��Ԫ", "A��", "����", "b4��", 
				"����", "F2-001-1����", "K3�ؿ��", "K4��", "A2�ؿ�7��", "һ��", "1-13�ؿ�", "������", "C��", 
				"103����", "64��С��", "����", "17-27��Ժ", "6��", "10��", "19��", "B��", "1#��2#¥", "����¥", 
				"1��", "A��", "61��", "B��", "C1��¥", "104��", "4�ŷ���4��", "��8��", "GJ-15��¥", "S430��", 
				"��10", "1��д��¥", "1����ס¥", "�ۺ�¥", "1-2", "��8��", "B1��", "��22��", "8-110", "2-49��", 
				"5����", "��1��", "ȹ¥", "7#����", "������", "A��ҵ", "һ����¥", "C1-6��¥", "26������", "��ʮһ�Ű칫¥", 
				"1-2��", "A-2��", "K4-1-6��", "����¥", "A1509", "1#-2��", "��26��", "6�׷�", "4#��", "��02��", 
				"1-4-602����", "��һ��", "��6����7��", "38.39��¥", "4#-һ������", "����¥", "��1��", "11����"};
		for (String sm: buildingSamples) {
			String res = standardizeBuilding(sm);
			System.out.println(sm + " --> " + res);
		}
	}
	
	public void standardizeTabletExample() {
		String[] tabletSamples = new String[] {"104��", "A203��204-1��204-2��205����λ", "106-109��", "105��", 
				"0188", "101", "226��10-2", "A03", "5��", "A601-602-C", "819��", "2002H��", "C-070-2", 
				"26B��", "101��-01��", "24-B-1", "A01��", "4-4-411��", "B-11��1-2��", "C-073����", "01�칫��", 
				"D2054��λ", "312��313��314", "109��", "601��611��", "B108������", "07��08̯λ", "10-A", "2�Ÿ�40��", 
				"A002S205F1001ר", "D��", "1506-1509��", "603��633", "101��", "715-03��", "A508-A52", "L2-047��", 
				"3-4��16-18��", "133-134a", "1-1818��", "0-108A����", "301-310��319-320��", "1906��012��", "��2", 
				"11-3����", "101�ŷ�֮16��A125", "17#901��", "A4179W192����", "��11��", "1601���Ա�03��", "107��"};
		for (String sm: tabletSamples) {
			String res = standardizeTablet(sm);
			String resEx = standardizeTablet(sm, true);
			System.out.println(sm + " --> " + res + " / " + resEx);
		}
	}
	
	public void standardizeExample() {
		String raw = "A��1��¥2��3��Ԫ5-7��701��702�ţ�3��3��A401-405��";
		List<AddressBuildingFloorTablet> sample = new ArrayList<AddressBuildingFloorTablet>();
		AddressBuildingFloorTablet part1 = new AddressBuildingFloorTablet("A��", "building", 10);
		AddressBuildingFloorTablet part2 = new AddressBuildingFloorTablet("1��¥", "building", 12);
		AddressBuildingFloorTablet part3 = new AddressBuildingFloorTablet("2��3��Ԫ", "building", 15);
		AddressBuildingFloorTablet part4 = new AddressBuildingFloorTablet("5-7��", "floor", 20);
		AddressBuildingFloorTablet part5 = new AddressBuildingFloorTablet("701��702��", "tablet", 24);
		AddressBuildingFloorTablet part6 = new AddressBuildingFloorTablet("��3��", "building", 32);
		AddressBuildingFloorTablet part7 = new AddressBuildingFloorTablet("4��", "floor", 34);
		AddressBuildingFloorTablet part8 = new AddressBuildingFloorTablet("A401-405��", "tablet", 36);
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
				+ "\tString standardizeBuilding(String addr): ��׼��building�������ɹ��򷵻�x����x����x��Ԫ��x�������򷵻�ԭֵ��\n"
				+ "\t\tʾ���μ�standardizeBuildingExample();\n"
				+ "\tString standardizeFloor(String addr): ��׼��floor�������ɹ�����x�㣬���򷵻�ԭֵ�����Խ�������¥�㣬���عؼ���\n"
				+ "\t\t����������ʾ���ߵ�¥������ʼ���յ�¥�㣬ʾ���μ�standardizeFloorExample;\n"
				+ "\tString standarddizeTablet(String addr, boolean extend): ��׼��tablet�������ɹ�����x�ң����򷵻�ԭֵ��\n"
				+ "\t\t���Խ�������¥�㣬���عؼ��֡���������ʾ���ߵ���������ʼ���յ����ƣ�extend�ؼ��ֿ����Ƿ�������¥�㣬��Ϊtrue����\n"
				+ "\t\t�ӵ�ǰ�����н�������Ӧ��¥�㣬������֤һ�������ɹ���Ӧ��û��FLOORʱ���ã�ʾ���μ�standardizeTabletExample;\n"
				+ "\tString standarddizeTablet(String addr): ��׼��tablet�������ɹ�����x�ң����򷵻�ԭֵ�����Խ�������¥�㣬���ع�\n"
				+ "\t\t���֡���������ʾ���ߵ���������ʼ���յ����ƣ�Ĭ�ϲ���������¥�㣬ʾ���μ�standardizeTabletExample��\n";
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

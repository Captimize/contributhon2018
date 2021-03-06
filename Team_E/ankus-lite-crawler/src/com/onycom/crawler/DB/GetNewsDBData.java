package com.onycom.crawler.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;

public class GetNewsDBData {
	public static void main(String args[]){
		GetNewsDBData get = new GetNewsDBData();
		get.core();
	}
	public void core(){
//		String sql = "select gendate, title, contents from sbsnews where gendate > '2013-07-01' and gendate < '2013-10-01' group by title";
		String keyword = "석화";
		
		String keyList[] = keyword.split(",");
		
//		HashMap
		for(int i = 0; i < keyList.length; i++){
			
//			String sql = "select gendate, title from blogdata where searchkeyword = '"+keyList[i]+"' group by title";
			String sql = "SELECT doc_cret_dt, doc_sj FROM blog_info WHERE srch_kwrd = '"+keyword+"' group by doc_sj";
			System.out.println(sql);
			
			
			DBConnect dbconn = new DBConnect();
			ResultSet rs = dbconn.getDBData(sql);
			
			System.out.println("SQL Process Finished...");
			StringBuffer buf = new StringBuffer();
			ArrayList<String> list = new ArrayList<String>();
			
			int cnt = 0;
			
			Dictionary dic = new Dictionary();
			
			HashMap<String, Integer> badMap = dic.getDicMap("bad.txt");
			
			ArrayList<String> cList = new ArrayList<String>();
			ArrayList<String> nList = new ArrayList<String>();
			
			try {
				NLP nlp = new NLP();
				while(rs.next()){
					boolean isNormal = false;
					boolean isCaution = false;
					buf = new StringBuffer();
					buf.append(rs.getString(1));
					buf.append("\t");
					
					String extractNoun = nlp.extractNoun(rs.getString(2).replace(": 네이버 블로그", ""));
					String titleList[] = extractNoun.split(",");
					
					int len = titleList.length;
					
					for(int j = 0; j < len ; j++){
						if(rs.getString(2).contains(keyList[i])){
							isNormal = true;
						}
						if(badMap.containsKey(titleList[j])){
							isCaution = true;
						}
					}
					buf.append(rs.getString(2));
					buf.append("\t");
					buf.append(extractNoun);
					
					if(isNormal && !isCaution){
						nList.add(buf.toString());
					} else if( isNormal && isCaution){
						cList.add(buf.toString());
					}
					
//					buf.append("\t");
//					buf.append(nlp.extractBag(rs.getString(3)));
					
					if(cnt % 100 == 0){
						System.out.println(buf.toString());
						System.out.println(cnt+"\t:Process Finished...");
					}
					cnt++;
				}
				CSVFileWriter cw = new CSVFileWriter();
				cw.outputFile(nList, keyList[i]+"-blog-normal.csv");
				cw.outputFile(cList, keyList[i]+"-blog-caution.csv");
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}

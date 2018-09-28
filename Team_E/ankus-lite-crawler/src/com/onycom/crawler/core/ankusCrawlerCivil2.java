package com.onycom.crawler.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.analysis.NewsClassifier;
import com.onycom.crawler.common.NLP;

public class ankusCrawlerCivil2 {
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
	}
	
	Connection conn;
	public void DBInput(HashMap<String, String> map){
		DBConnect db = new DBConnect();
		/**
		 * CREATE TABLE civildata(
rno bigint AUTO_INCREMENT(1,1),
GENDATE timestamp,
ORILINK character varying(4096),
TITLE character varying(4096),
CONTENTS character varying(4096),
KEYWORDS character varying(4096) COLLATE utf8_bin 
) COLLATE utf8_bin ;


		 * 
		 * 
		 */
		Statement stmt;
		ResultSet rs;
			
//		resMap.put("title", at.text());
//		resMap.put("provider", newsProvider);
//		resMap.put("link", link);
//		resMap.put("date", t11.text());
//		resMap.put("content", abc.text());
//		resMap.put("exist", "1");
//		
		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO civildata2(gendate, orilink, title, contents, keywords) VALUES (?,?,?,?,?)");
			
//			stmt = conn.createStatement();
//			stmt.execute(
//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
			pstmt.setString(1, map.get("date"));
			pstmt.setString(2, map.get("link"));
			pstmt.setString(3, map.get("title"));
			pstmt.setString(4, map.get("content"));
			pstmt.setString(5, map.get("keyList"));
			pstmt.executeUpdate();
			
			pstmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws ClientProtocolException, IOException {
		
		// 1. 媛��졇�삤湲곗?�� �떆媛� 李띻�?
		System.out.println(" Start Date : " + getCurrentData());
		
		ankusCrawlerCivil2 ac = new ankusCrawlerCivil2();
		
		
		//sid1 = ?��꾩빞, sid2 = �꽭?���遺꾩빞, oid = �돱�뒪�젣?�듭�?, aid = ?��몄꽌踰덊?��(10�옄?���?)
//		sid1= ?��꾩빞 101(寃쎌?��), 102(�궗�쉶), 103(�깮�솢/?��명솕)
//		055:SBS
//		056:KBS
//		214:MBC
		String sid1 = "101";
		String oid = "001";
		String addr = "http://www.ciss.go.kr/www/selectBbsNttView.do?key=70&bbsNo=81&nttNo=????&searchCtgry=&searchCnd=all&searchKrwd=&pageIndex=63&pageUnit=10&integrDeptCode=";
		
		//KBS
//		http://news.naver.com/main/read.nhn?mode=LPOD&mid=sec&oid=056&aid=0010363277
		
		//9999999999
		//0000000098
		int start = 2328;
		int end = 27405;
				
		
		for(int i = start ; i < end ; i++){
			String addrI = ac.getAddr(addr, i);
			System.out.println(addrI);
			BufferedReader br = ac.crawler(addrI);
			
			StringBuffer sb = new StringBuffer();
			 
			String line = null;
			
			while((line = br.readLine())!=null){
//				System.out.println(line);
				sb.append(line+"\r\n");
			}
			
			Document doc = Jsoup.parse(sb.toString());
			Element eTitle = doc.getElementsByTag("td").addClass("subject").first();
			Element eDate = doc.getElementsByTag("td").get(4);
			Element eContents = doc.getElementsByTag("td").get(5);
			
			
			Elements eList = doc.getAllElements();
			
			System.out.println("title:\t"+eTitle.text());
			System.out.println("Date:\t"+eDate.text());
			System.out.println("Contents:\t"+eContents.text());
			
			HashMap<String, String> resMap = new HashMap<String, String>();
			
			resMap.put("title", eTitle.text());
			resMap.put("date", eDate.text());
			resMap.put("content", eContents.text());
			
			NLP nlp = new NLP();
			
			resMap.put("keyList", nlp.extractNoun(eContents.text()));
			resMap.put("link", addrI);
			
			ac.DBInput(resMap);
			
//			for(int j = 0; j < eList.size(); j++){
//				System.out.println("className:\t"+eList.get(j).className());
//				System.out.println(eList.get(j).text());
//			}
			
//			HashMap<String, String> resMap = new HashMap<String, String>();
//			System.out.println(e.text());
//			
//			if(!e.text().contains("?��?��?���? 개편 ?��?��")){
//			
//				String title = e.text().split("?���?")[1].split("?��?��?��")[0];
//				
//				
//				
//				System.out.println(title);
//				
//				String date = e.text().split("?��?��?��")[1].split("조회?��")[0].replaceAll(" ", "");
//				System.out.println(date);
//				
//				
//				
//				if(e.text().split(title).length > 2){
//					
//					String content = e.text().split(title)[2];
//					System.out.println(content);
//					resMap.put("title", title);
//					resMap.put("date", date);
//					resMap.put("content", content);
//					resMap.put("link", addrI);
//					
//					ac.DBInput(resMap);
//					System.out.println("DBINput");
//				}
//			}
//			System.out.println(e.text().split("?��?��?��")[1].split("조회?��")[1]);
		    //ac.removeHTML("/Users/ankus/Documents/workspace2/ankus crawler/html/",sid1+"_"+oid+"_"+ new String().valueOf(i));
		}
		

//	    
//	    // 12. �뼹留덈�? 嫄몃졇��? 李띿뼱蹂?��?��
//	    System.out.println(" End Date : " + getCurrentData());
	}
	
	public void removeHTML(String targetPath, String fileName){
		FileReader fr;
		BufferedReader br;
		
		FileWriter fw;
		BufferedWriter bw;
		
		try {
			fr = new FileReader(targetPath+"/"+fileName+".html");
			br = new BufferedReader(fr);
			
			fw = new FileWriter(targetPath+"/"+fileName+".txt");
			bw = new BufferedWriter(fw);
			String line = null;
			StringBuffer sb = new StringBuffer();
			String regExp = "!\"#[$]%&\\(\\)\\{\\}@`[*]:[+];-.<>,\\^~|'\\[\\]";
			
			while((line = br.readLine())!=null){
//				bw.append(line.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
				line = line.replaceAll("<[^>]*>", "");
				line = line.replaceAll("[a-zA-Z]", "");
				line = line.replaceAll("[0-9]", "");
				System.out.println("pre: "+line);
				line = spCharRid(line);
				System.out.println("aft: "+line);
				sb.append(line);
				sb.append("\r\n");
			}
			
			 Document doc = Jsoup.parse(sb.toString());
//			 Document doc = Jsoup.parse(html);

			 System.out.println(doc.text());
//			 System.out.println(doc.textNodes());
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getAddr(String addr, int pageNum){
		String pnStr = new String().valueOf(pageNum);
		int len = pnStr.length();
		int zeroLen = 10 - len;
		
		StringBuffer zeroStr = new StringBuffer();
		for(int i = 0 ; i < zeroLen ; i++){
			zeroStr.append("0");
		}
		//9999999999
		//1234567890
		
		zeroStr.append(pnStr);
		addr = addr.replace("????", zeroStr.toString());
		if(pageNum % 100 == 0)
			System.out.println(addr);
		return addr;
	}
	public String spCharRid(String strInput){
//		  System.out.println("@spCharRid original: "+ strInput);
		  String strWork = strInput;
		  String[] spChars = {
				    "`", "-", "=", ";", "'", "/", "~", "!", "@", 
				    "#", "$", "%", "^", "&", "|", ":", "<", ">", 
				    //"\",
				    "*", 
				    "+", 
				    "{", 
				    "}",",","[","]","_","\"","\\",
				    "?","(",")",
				    "."
				  };
		  
		  int spCharLen = spChars.length; 
		  
		   for(int i = 0; i < spCharLen; i++){
//		    System.out.println("@for @proceed : "+i);
//		    System.out.println("@spCharRid @target is : " + spChars[i]);
		    strWork = strWork.replace(spChars[i], "");
//		    System.out.println("@spCharRid @replaceAll: "+ strWork);
		   }

//		  System.out.println("@spCharRid output  : "+ strWork);
		  return strWork;
		 }
	public BufferedReader crawler(String addr){
		HttpPost http = new HttpPost(addr);

	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹�? �깮�꽦
	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣?���? Response 媛앹껜��? �떞�쓬
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
			// 5. Response 諛쏆�? �뜲�씠�꽣 以�, DOM �뜲�씠�꽣?���? 媛��졇�� Entity�뿉 �떞�쓬
		    HttpEntity entity = response.getEntity();
		    
		    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 ?�⑦?��?�� ���엯�쓣 媛��졇�� �떞?�� Charset�쓣 媛��졇�샂 
		    ContentType contentType = ContentType.getOrDefault(entity);
//		    System.out.println(entity.toString());
	        Charset charset = contentType.getCharset();
//	        System.out.println(charset.name());
	        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?)
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return retBr;
	}
	
	public void saveHtml(BufferedReader br, String targetPath, String fileName){
		FileWriter fw;
		try {
			fw = new FileWriter(targetPath +"/" + fileName+".html");
			BufferedWriter bw = new BufferedWriter(fw);
			
			String line = "";
		    while((line=br.readLine()) != null){
		    	bw.append(line+"\n");
		    	bw.flush();
		    	fw.flush();
		    }
		    
		    bw.close();
		    fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void originalBackup(){
//		// 2. 媛��졇�삱 HTTP 二쇱?�� �꽭�똿
//	    HttpPost http = new HttpPost("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more");
//
//	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹�? �깮�꽦
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    
//	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣?���? Response 媛앹껜��? �떞�쓬
//	    HttpResponse response = httpClient.execute(http);
//	    
//	    // 5. Response 諛쏆�? �뜲�씠�꽣 以�, DOM �뜲�씠�꽣?���? 媛��졇�� Entity�뿉 �떞�쓬
//	    HttpEntity entity = response.getEntity();
//	    
//	    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 ?�⑦?��?�� ���엯�쓣 媛��졇�� �떞?�� Charset�쓣 媛��졇�샂 
//	    ContentType contentType = ContentType.getOrDefault(entity);
//        Charset charset = contentType.getCharset();
//        
//        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?) 
//	    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
//	    
//	    // 8. 媛��졇�삩 DOM �뜲�씠�꽣?���? �떞湲곗?���븳 洹몃�?
//	    StringBuffer sb = new StringBuffer();
//	    
//	    // 9. DOM �뜲�씠�꽣 媛��졇�삤湲�
//	    String line = "";
//	    while((line=br.readLine()) != null){
//	    	sb.append(line+"\n");
//	    }
//	    
//	    // 10. 媛��졇�삩 �븘?��꾨떎�슫 DOM�쓣 蹂댁?��
//	    System.out.println(sb.toString());
//	    
//	    // 11. Jsoup�쑝濡� �뙆�떛�빐蹂댁?��.
//	    Document doc = Jsoup.parse(sb.toString());
//	    
//	    // 李멸?? - Jsoup�뿉�꽌 �젣?�듯�?�뒗 Connect 泥섎?��
//	    Document doc2 = Jsoup.connect("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more").get();
////	    System.out.println(doc2.data());
	}
}

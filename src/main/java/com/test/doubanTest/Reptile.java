package com.test.doubanTest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.test.pojo.Book;

public class Reptile {
	//用于存书
	private static List<Book> storeBooks = new ArrayList<Book>(3000);
	
	public static void main(String[] args) {

		ArrayList<String> bookUrls = new ArrayList<String>();
		//分别抓取：互联网、编程、算法
		
		bookUrls = downloadBookUrl("互联网");
		getBookInfo(bookUrls);

		bookUrls = downloadBookUrl("编程");
		getBookInfo(bookUrls);

		bookUrls = downloadBookUrl("算法");
		getBookInfo(bookUrls);
		
		List<Book> books = sort(storeBooks);
		
		writeExcel(books);

	}

	/**
	 * 抓取每本书的info
	 * 
	 * @param ArrayList<String>
	 */
	public static void getBookInfo(ArrayList<String> bookUrls) {
		Map<String, String> cookies = new HashMap<String, String>();
		//book.douban.com
		cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");
		cookies.put("__utmb", "81379588.1.10.1478780180");
		cookies.put("__utmc", "81379588");
		cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
		cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
		cookies.put("_pk_ses.100001.3ac3", "*");
		//douban.com
		cookies.put("bid", "MvEsSVNL_Nc");
		//read.douban.com
		cookies.put("_ga", "GA1.3.117318709.1478747468");
		cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
		cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
				+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
				+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
		//www.douban.com
		cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
		cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
				+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
				+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");

		int count = 0;
		for (String url : bookUrls) {
			try {
				Document doc = Jsoup.connect(url)
						.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
						.timeout(3000).get();
				Elements titleElement = doc.select("[property=v:itemreviewed]");
				Elements scoreElement = doc.select("strong");
				Elements ratingSum = doc.getElementsByClass("rating_sum").select("a").select("span");
				Element authorElement = doc.getElementById("info");
				Element pressElement = doc.getElementById("info");

				// 书名
				String title = titleElement.text();
				// 评分
				String score = scoreElement.html();
				// 评价人数
				String rating_sum = ratingSum.html();
				// 作者
				String author = authorElement.text();
				if (author.indexOf("作者:") > -1) {
					author = pressElement.text().split("作者:")[1].split("出版社:")[0].trim();
				} else {
					author = "";
				}
				// 出版社
				String press = pressElement.text();
				if (press.indexOf("出版社:") > -1) {
					press = pressElement.text().split("出版社:")[1].split(" ")[1];
				} else {
					press = "";
				}
				// 出版日期
				String date = pressElement.text();
				if (date.indexOf("出版年:") > -1) {
					date = pressElement.text().split("出版年:")[1].split(" ")[1];
				} else {
					date = "";
				}
				// 价格
				String price = pressElement.text();
				if (price.indexOf("定价:") > -1) {
					price = pressElement.text().split("定价:")[1].split(" ")[1];
					if (price.equals("CNY")) {
						price = pressElement.text().split("定价:")[1].split(" ")[2];
					}
				} else {
					price = "";
				}

				System.out.println(title);
				System.out.println(score);
				System.out.println(rating_sum);
				System.out.println(author);
				System.out.println(press);
				System.out.println(date);
				System.out.println(price);
				// 评价人数大于1000插入数据到数据库
				if (!rating_sum.equals("") && Integer.parseInt(rating_sum) >= 1000) {
					Book book = new Book();
					book.setOrder(count+1);
					book.setTitle(title);
					book.setGrade(score);
					book.setNumber(rating_sum);
					book.setAuthor(author);
					book.setPress(press);
					book.setDate(date);
					book.setPrice(price);
					storeBooks.add(book);
					System.out.println(++count);
				}
				// 睡眠防止ip被封
				try {
					System.out.println("睡眠1秒");
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存书的url
	 * 
	 * @param keyWord
	 * @return
	 */
	public static ArrayList<String> downloadBookUrl(String keyWord) {
		ArrayList<String> bookUrls = new ArrayList<String>();
		int index = 0;
		try {
			Map<String, String> cookies = new HashMap<String, String>();
			//book.douban.com
			cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");
			cookies.put("__utmb", "81379588.1.10.1478780180");
			cookies.put("__utmc", "81379588");
			cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
			cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
			cookies.put("_pk_ses.100001.3ac3", "*");
			//douban.com
			cookies.put("bid", "MvEsSVNL_Nc");
			//read.douban.com
			cookies.put("_ga", "GA1.3.117318709.1478747468");
			cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
			cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
					+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
					+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
			//www.douban.com
			cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
			cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
					+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
					+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");
			
			while (true) {
				// 获取cookies

				Document doc = Jsoup.connect("https://book.douban.com/tag/" + keyWord + "?start=" + index + "&type=T")
						.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
						.timeout(3000).get();
				Elements newsHeadlines = doc.select("ul").select("h2").select("a");
				System.out.println("本页：  " + newsHeadlines.size());
				for (Element e : newsHeadlines) {
					System.out.println(e.attr("href"));
					bookUrls.add(e.attr("href"));
				}
				index += newsHeadlines.size();
				System.out.println("共抓取url个数：" + index);
				if (newsHeadlines.size() == 0) {
					System.out.println("end");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bookUrls;
	}
	/**
	 * 将书籍集合按照评分排序取前100本
	 * @param list 待排序的集合
	 * @return
	 */
	
	public static List<Book> sort(List<Book> list){
		Collections.sort(list,new Comparator<Book>() {

			public int compare(Book o1, Book o2) {
				return (int) (Double.parseDouble(o2.getGrade())-Double.parseDouble(o1.getGrade()));
			}
		});
		List<Book> books = new ArrayList<Book>(100);
		System.arraycopy(list, 0, books, 0, 100);
		return books;
	}
	
	/**
	 * 将数据打印成为excel表格
	 * @param list 待输出的数据集合
	 */
    public static void writeExcel(List<Book> list)
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        //生成一个sheet1
        HSSFSheet sheet = wb.createSheet("sheet1");
        //为sheet1生成第一行，用于放表头信息
        HSSFRow row = sheet.createRow(0);
        
        //第一行的第一个单元格的值为  ‘序号’
        HSSFCell cell = row.createCell((short)0);
        cell.setCellValue("序号");
        cell = row.createCell((short)1);
        cell.setCellValue("书名");
        cell = row.createCell((short)2);
        cell.setCellValue("评分");
        cell = row.createCell((short)3);
        cell.setCellValue("评论人数");
        cell = row.createCell((short)4);
        cell.setCellValue("作者");
        cell = row.createCell((short)5);
        cell.setCellValue("出版社");
        cell = row.createCell((short)6);
        cell.setCellValue("出版时间");
        cell = row.createCell((short)7);
        cell.setCellValue("价格");
        
        
        //获得List中的数据，并将数据放到Excel中
        for(int i=0;i<list.size();i++)
        {
            Book book = list.get(i);
           //数据每增加一行，表格就再生成一行              
            row = sheet.createRow(i+1);
            //第一个单元格，放序号随着i的增加而增加            
            cell = row.createCell((short)0);
            cell.setCellValue(i+1);
            //存入图书数据
            cell = row.createCell((short)1);
            cell.setCellValue(book.getTitle());
            cell = row.createCell((short)2);
            cell.setCellValue(book.getGrade());
            cell = row.createCell((short)3);
            cell.setCellValue(book.getNumber());
            cell = row.createCell((short)4);
            cell.setCellValue(book.getAuthor());
            cell = row.createCell((short)5);
            cell.setCellValue(book.getPress());
            cell = row.createCell((short)6);
            cell.setCellValue(book.getDate());
            cell = row.createCell((short)7);
            cell.setCellValue(book.getPrice());
            
        }
        
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            wb.write(os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        byte[] content = os.toByteArray();
        
        File file = new File("c:/books.xls");//Excel文件生成后存储的位置。
        
        OutputStream fos  = null;
        
        try
        {
            fos = new FileOutputStream(file);
            
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }    
    }
}
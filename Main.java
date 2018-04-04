
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Main {
	public static void main(String argv[]){
		try{
			String filepath = "xml_wynik.xml";
			String txtfile = "022018 HURT EUR.txt";
			String cennikName = "CENA HURT EUR 022018";
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
			String a = "1,22";
			double b = Double.valueOf(a.replace(",", "."));
			
			System.out.println(b);
			System.out.println(String.valueOf(b).replace(".", ","));
			HashMap<String,String> priceMap = getPriceMap(txtfile);
			System.out.println(priceMap.size() + " | " + priceMap);
			System.out.println( );
			
			Node zmianyCen = doc.getElementsByTagName("ZMIANY_CEN_SPRZEDAZY").item(1);
			NodeList cenniki = zmianyCen.getChildNodes().item(0).getChildNodes().item(0).getChildNodes();
			
			System.out.println(cennikName);
			
			int index = -1;
			System.out.println("|||||");
			for(int i = 0 ; i < cenniki.getLength(); ++i){
				System.out.println("|"+cenniki.item(i).getAttributes().getNamedItem("NAZWA").getTextContent()+"|");
				if(cenniki.item(i).getAttributes().getNamedItem("NAZWA").getTextContent().equals(cennikName)){
					index = i;
					System.out.println(i);
				}
			}
			
			System.out.println(index);
			NodeList properModifications = cenniki.item(index).getChildNodes().item(0).getChildNodes();
			for(int i = 0 ; i < properModifications.getLength(); ++i){
				Node node = properModifications.item(i);
				String productName = node.getAttributes().getNamedItem("INDEKS_ARTYKULU").getTextContent().toUpperCase();
				if(priceMap.containsKey(productName.toUpperCase())){
					String price = priceMap.get(productName.toUpperCase());
					double cenaSprzedazyNetto = Double.valueOf(price.replace(",", "."));
					double cenaZakupu = Double.valueOf(node.getAttributes().getNamedItem("CENA_ZAKUPU_NETTO").getTextContent().replace(",", "."));
					
					node.getAttributes().getNamedItem("CENA_SPRZEDAZY_NETTO").setTextContent(price);
					node.getAttributes().getNamedItem("CENA_SPRZEDAZY_BRUTTO").setTextContent(String.valueOf(round(cenaSprzedazyNetto*1.22, 2)).replace(".", ","));
					
					priceMap.remove(productName);
				}else{
					
				}
			}
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("xml_wynik.xml"));
			trans.transform(source, result);
			
			System.out.println(priceMap.size() + " | " + priceMap);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static HashMap<String, String> getPriceMap(String filePath){
		HashMap<String,String> result = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			while((line = br.readLine()) !=null){
				String[] pair = line.split("\t");
				pair[1] = pair[1].substring(0, pair[1].length() - 4);
				if(pair[1].length() > 6){
					pair[1] = pair[1].substring(0, pair[1].length() - 7) + pair[1].substring(pair[1].length() - 6, pair[1].length());
				}
				result.put(pair[0].toUpperCase(), (pair[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
}

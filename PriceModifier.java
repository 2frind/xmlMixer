import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.management.loading.PrivateClassLoader;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class PriceModifier {

	HashMap<String, String> priceMap;
	
	private JFrame frame;
	private JTable xlsContent;
	private JLabel lblNewLabel;
	private JComboBox comboBox;
	
	private String log = "";
	private NodeList cenniki;
	Document doc;
	
	private JButton btnNewButton;
	private JLabel lblNameNewXml;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PriceModifier window = new PriceModifier();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PriceModifier() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 618, 590);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 77, 158, 228);
		frame.getContentPane().add(scrollPane);
		
		String columnNames[] = {"ID", "Cena"};
		DefaultTableModel dtm = new DefaultTableModel(columnNames, 0);
		xlsContent = new JTable(dtm);
		scrollPane.setViewportView(xlsContent);
		
		JButton btnSelectXlsFile = new JButton("Select xls file");
		btnSelectXlsFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setTable();
			}
		});
		btnSelectXlsFile.setBounds(26, 29, 158, 25);
		frame.getContentPane().add(btnSelectXlsFile);
		lblNewLabel = new JLabel("Started");
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel.setBounds(443, 33, 145, 217);
		frame.getContentPane().add(lblNewLabel);
		
		comboBox = new JComboBox();
		comboBox.setBounds(196, 82, 224, 25);
		frame.getContentPane().add(comboBox);
		
		JButton btnSelectXmlFile = new JButton("Select xml file");
		btnSelectXmlFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setComboBox();
			}
		});
		btnSelectXmlFile.setBounds(196, 29, 224, 25);
		frame.getContentPane().add(btnSelectXmlFile);
		
		btnNewButton = new JButton("Set prices");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPrices();
			}
		});
		btnNewButton.setBounds(27, 385, 251, 25);
		frame.getContentPane().add(btnNewButton);
		
		lblNameNewXml = new JLabel("Name new xml: ");
		lblNameNewXml.setBounds(26, 349, 104, 16);
		frame.getContentPane().add(lblNameNewXml);
		
		textField = new JTextField();
		textField.setBounds(128, 346, 189, 22);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
	}
	
	/*
	 * Loads xls file and shows content in the table
	 */
	private void setTable(){
		priceMap = new HashMap<String, String>();
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*xls", "xls");
		jfc.setFileFilter(filter);
		addToLog("opened filechooser ");
		int returnVal = jfc.showOpenDialog(null);
		try{
			if(returnVal == JFileChooser.APPROVE_OPTION){
				addToLog("file chosen");
				File selectedFile = jfc.getSelectedFile();
				HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(selectedFile));
				HSSFSheet sheet = workbook.getSheetAt(0);
				Cell id_cell, price_cell;
				int numberOfRows = sheet.getLastRowNum();
				String id, price = "";
				for(int i = 0 ; i <= numberOfRows ; ++i){
					id_cell = sheet.getRow(i).getCell(0);
					price_cell = sheet.getRow(i).getCell(1);
					id = id_cell.getStringCellValue();
					switch(price_cell.getCellType()){
					case Cell.CELL_TYPE_STRING:
						price = price_cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_NUMERIC:
						price = String.valueOf(price_cell.getNumericCellValue());
						break;
					}
					
					if(checkIDPrice(id, price)){
						priceMap.put(id.toUpperCase(), price);
					}
				}
				workbook.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		String[] columnNames = {"Id", "Price"};
		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		for(Map.Entry<String, String> entry : priceMap.entrySet()){
			model.addRow( new Object[]{entry.getKey(), entry.getValue()} );
		}
		xlsContent.setModel(model);
		
	}
	
	/*
	 * Loads xml file and sets combobox with infile prices
	 */
	private void setComboBox(){
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.xml", "xml");
		jfc.setFileFilter(filter);
		addToLog("opened filechooser2 ");
		int returnVal = jfc.showOpenDialog(null);
		try{
			if(returnVal == JFileChooser.APPROVE_OPTION){
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				String path = jfc.getSelectedFile().getPath();
				addToLog(path);
				doc = docBuilder.parse(path);
				cenniki = doc.getElementsByTagName("ZMIANY_CEN_SPRZEDAZY").item(1).getChildNodes().item(0).getChildNodes().item(0).getChildNodes();
				String[] list = nodeListToStringList(cenniki);
				comboBox.removeAllItems();
				for(String current : list){
					comboBox.addItem(current);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * Creates new xml file with prices changed according to xls file and in-program selection
	 * Additionally sets table with records not added to the xml file
	 */
	
	private void setPrices(){
		addToLog("seting prices");
		int index = comboBox.getSelectedIndex();
		if(cenniki != null){
			NodeList properModifications = cenniki.item(index).getChildNodes().item(0).getChildNodes();
			addToLog("modifications are to be made");
			for(int i = 0 ; i < properModifications.getLength(); ++i){
				Node node = properModifications.item(i);
				String productName = node.getAttributes().getNamedItem("INDEKS_ARTYKULU").getTextContent().toUpperCase();
				if(priceMap.containsKey(productName.toUpperCase())){
					String price = priceMap.get(productName.toUpperCase());
					double cenaSprzedazyNetto = Double.valueOf(price.replace(",", "."));
					node.getAttributes().getNamedItem("CENA_SPRZEDAZY_NETTO").setTextContent(price);
					node.getAttributes().getNamedItem("CENA_SPRZEDAZY_BRUTTO").setTextContent(String.valueOf(round(cenaSprzedazyNetto*1.22, 2)).replace(".", ","));
					priceMap.remove(productName.toUpperCase());
				}else{
					
				}
			}
			String resultFile = textField.getText() + ".xml";
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans;
			try {
				trans = tf.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(resultFile));
				trans.transform(source, result);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		String[] columnNames = {"Id", "Price"};
		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		for(Map.Entry<String, String> entry : priceMap.entrySet()){
			model.addRow( new Object[]{entry.getKey(), entry.getValue()} );
		}
		xlsContent.setModel(model);
	}
	/*
	 * Checks if pair of 2 Strings represents ID and Price
	 * Empty Strings and non-numerical prices are not allowed
	 */
	private boolean checkIDPrice(String id, String price){
		boolean result = true;
		price.replace(",", ".");
		if(id.compareTo("") == 0 || price.compareTo("") == 0 || !isNumeric(price)) result = false;
		return result;
	}
	
	/*
	 * Checks if given String can be turn in to numerical value
	 */
	private static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?"); 
	}
	
	private String[] nodeListToStringList(NodeList nodeList){
		ArrayList<String> result = new ArrayList<String>();
		for(int i = 0 ; i < nodeList.getLength() ; ++i){
			result.add(nodeList.item(i).getAttributes().getNamedItem("NAZWA").getTextContent());
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	/*
	 * Adds String to window debug log
	 */
	private void addToLog(String entry){
		log = entry + "<br/>" + log;
		lblNewLabel.setText("<html>" + log + "</html>");
	}
}

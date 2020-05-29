package com.build.analyzer.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;


public class CSVReaderWriter {

	public void writeListBean(List<LongEntity> fixdata,String csvfilepath)
			throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

	  
        try { 
  
            // Creating writer class to generate 
            // csv file 
            FileWriter writer = new 
                       FileWriter(csvfilepath); 
  
 
  
            // Create Mapping Strategy to arrange the  
            // column name in order 
            ColumnPositionMappingStrategy mappingStrategy= 
                        new ColumnPositionMappingStrategy(); 
            mappingStrategy.setType(LongEntity.class); 
  
            // Arrange column name as provided in below array. 
            String[] columns = new String[]  
                    { "lValue"}; 
            mappingStrategy.setColumnMapping(columns); 
           
  
            // Createing StatefulBeanToCsv object 
            StatefulBeanToCsvBuilder<LongEntity> builder= 
                        new StatefulBeanToCsvBuilder(writer); 
            StatefulBeanToCsv beanWriter =  
            builder.withMappingStrategy(mappingStrategy).build(); 

            // Write list to StatefulBeanToCsv object 
            beanWriter.write(fixdata); 
  
            // closing the writer object 
            writer.close(); 
        } 
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
 
	}
	
}

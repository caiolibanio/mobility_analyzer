package mobility.mobility_analyzer;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import mobility.core.SocioData;
import mobility.service.SocioDataService;

public class SocioDataTest {
	
	private SocioDataService serviceSocioData = new SocioDataService();

    

	@Before
	public void setUp() {
		
	}

	@Test
	public void testTableValues() throws IOException {
		readXLSFile();
	}
	
	private void readXLSFile() throws IOException {
		List<SocioData> listDB = serviceSocioData.findAll();
		
		InputStream ExcelFileToRead = new FileInputStream("socio_data_selected_to_test.xls");
		HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

		HSSFSheet sheet=wb.getSheetAt(0);
		HSSFRow row; 
		HSSFCell cell;

		Iterator rows = sheet.rowIterator();
		List<Object> listRows = null;

		while (rows.hasNext()) {
			listRows = new ArrayList<Object>();
			row=(HSSFRow) rows.next();
			Iterator cells = row.cellIterator();
			
			while (cells.hasNext()) {
				
				
				cell=(HSSFCell) cells.next();
		
				if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
				{
					
					listRows.add(cell.getStringCellValue());
//					System.out.print(cell.getStringCellValue()+" ");
				}
				else if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
				{
					listRows.add(cell.getNumericCellValue());
//					System.out.print(cell.getNumericCellValue()+" ");
				}
				else
				{
					//U Can Handel Boolean, Formula, Errors
				}
			}
			
			compareWithSocioDataEntity(listRows, listDB);
		}
	
	}

	private void compareWithSocioDataEntity(List<Object> listRowsFile, List<SocioData> listDB) {
		for(SocioData data : listDB){
			if(data.getCode().equals((String) listRowsFile.get(0))){
				checkConsistency(data, listRowsFile);
				break;
			}
		}
		
	}

	private void checkConsistency(SocioData data, List<Object> listRowsFile) {
		assertEquals((String)listRowsFile.get(0), data.getCode());  
		assertEquals((String)listRowsFile.get(1), data.getName());  
		assertEquals((Double)listRowsFile.get(2), data.getAge_structure_all_ages(), 0.01);  
		assertEquals((Double)listRowsFile.get(3), data.getAge_structure_0_15(), 0.01);
		assertEquals((Double)listRowsFile.get(4), data.getAge_structure_16_29(), 0.01);
		assertEquals((Double)listRowsFile.get(5), data.getAge_structure_30_44(), 0.01);
		assertEquals((Double)listRowsFile.get(6), data.getAge_structure_45_64(), 0.01);
		assertEquals((Double)listRowsFile.get(7), data.getAge_structure_65_plus(), 0.01);
		assertEquals((Double)listRowsFile.get(8), data.getAge_structure_working_age(), 0.01);
		assertEquals((Double)listRowsFile.get(9), data.getPersons_per_hectare(), 0.01);
		assertEquals((Double)listRowsFile.get(10), data.getAll_households(), 0.01);
		assertEquals((Double)listRowsFile.get(11), data.getCouple_household_with_dependent_children(), 0.01);
		assertEquals((Double)listRowsFile.get(12), data.getCouple_household_without_dependent_children(), 0.01);
		assertEquals((Double)listRowsFile.get(13), data.getOne_person_household(), 0.01);
		assertEquals((Double)listRowsFile.get(14), data.getWhite(), 0.01);
		assertEquals((Double)listRowsFile.get(15), data.getMixed_multiple_ethnic_groups(), 0.01);
		assertEquals((Double)listRowsFile.get(16), data.getAsian_asian_british(), 0.01);
		assertEquals((Double)listRowsFile.get(17), data.getBlack_african_caribbean_black_british(), 0.01);
		assertEquals((Double)listRowsFile.get(18), data.getUnited_kingdom(), 0.01);
		assertEquals((Double)listRowsFile.get(19), data.getNot_united_kingdom(), 0.01);
		assertEquals((Double)listRowsFile.get(20), data.getMedian_price(), 0.01);
		assertEquals((Double)listRowsFile.get(21), data.getSales(), 0.01);
		assertEquals((Double)listRowsFile.get(22), data.getEconomically_active_total(), 0.01);
		assertEquals((Double)listRowsFile.get(23), data.getEconomically_inactive_total(), 0.01);
		assertEquals((Double)listRowsFile.get(24), data.getEconomically_active_employee(), 0.01);
		assertEquals((Double)listRowsFile.get(25), data.getEconomically_active_self_employed(), 0.01);
		assertEquals((Double)listRowsFile.get(26), data.getEconomically_active_unemployed(), 0.01);
		assertEquals((Double)listRowsFile.get(27), data.getEconomically_active_full_time_student(), 0.01);
		assertEquals((Double)listRowsFile.get(28), data.getEmployment_rate(), 0.01);
		assertEquals((Double)listRowsFile.get(29), data.getUnemployment_rate(), 0.01);
		assertEquals((Double)listRowsFile.get(30), data.getNo_qualifications(), 0.01);
		assertEquals((Double)listRowsFile.get(31), data.getHighest_level_of_qualification_level_1_qualifications(), 0.01);
		assertEquals((Double)listRowsFile.get(32), data.getHighest_level_of_qualification_level_2_qualifications(), 0.01);
		assertEquals((Double)listRowsFile.get(33), data.getHighest_level_of_qualification_apprenticeship(), 0.01);
		assertEquals((Double)listRowsFile.get(34), data.getHighest_level_of_qualification_level_3_qualifications(), 0.01);
		assertEquals((Double)listRowsFile.get(35), data.getHighest_level_of_qualification_level_4_qualifications_and_abov(), 0.01);
		assertEquals((Double)listRowsFile.get(36), data.getDay_to_day_activities_limited_a_lot(), 0.01);
		assertEquals((Double)listRowsFile.get(37), data.getDay_to_day_activities_limited_a_little(), 0.01);
		assertEquals((Double)listRowsFile.get(38), data.getDay_to_day_activities_not_limited(), 0.01);
		assertEquals((Double)listRowsFile.get(39), data.getVery_good_or_good_health(), 0.01);
		assertEquals((Double)listRowsFile.get(40), data.getNo_cars_or_vans_in_household(), 0.01);
		assertEquals((Double)listRowsFile.get(41), data.get_1_car_or_van_in_household(), 0.01);
		assertEquals((Double)listRowsFile.get(42), data.get_2_cars_or_vans_in_household(), 0.01);
		assertEquals((Double)listRowsFile.get(43), data.get_3_cars_or_vans_in_household(), 0.01);
		assertEquals((Double)listRowsFile.get(44), data.get_4_or_more_cars_or_vans_in_household(), 0.01);
		
		
		
	}
}

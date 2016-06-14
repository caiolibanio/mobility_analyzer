package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.Point;
import mobility.core.SocioData;

public class SocialDataDAO implements IDAO<SocioData> {

	public SocioData findByCode(String code){
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM socio_data WHERE code = ?";

        SocioData socioData = null;

        try {
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, code);
            rs = pstm.executeQuery();
            
            List<SocioData> list = setSocioAttributes(rs);
            socioData = list.get(0);
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conexao.close(conn, pstm, rs);
        }
        return socioData;
	}

	private List<SocioData> setSocioAttributes(ResultSet rs) throws SQLException {
		SocioData socioData = null;
		List<SocioData> list = new ArrayList<SocioData>();
		while (rs.next()) {
			socioData = new SocioData();
			socioData.setCode(rs.getString("code"));
			socioData.setName(rs.getString("name"));
			socioData.setAge_structure_all_ages(rs.getInt("age_structure_all_ages"));
			socioData.setAge_structure_0_15(rs.getInt("age_structure_0_15"));
			socioData.setAge_structure_16_29(rs.getInt("age_structure_16_29"));
			socioData.setAge_structure_30_44(rs.getInt("age_structure_30_44"));
			socioData.setAge_structure_45_64(rs.getInt("age_structure_45_64"));
			socioData.setAge_structure_65_plus(rs.getInt("age_structure_65_plus"));
			socioData.setAge_structure_working_age(rs.getInt("age_structure_working_age"));
			socioData.setPersons_per_hectare(rs.getDouble("persons_per_hectare"));
			socioData.setAll_households(rs.getInt("all_households"));
			socioData.setCouple_household_with_dependent_children(rs.getInt("couple_household_with_dependent_children"));
			socioData.setCouple_household_without_dependent_children(rs.getInt("couple_household_without_dependent_children"));
			socioData.setOne_person_household(rs.getInt("one_person_household"));
			socioData.setWhite(rs.getInt("white"));
			socioData.setMixed_multiple_ethnic_groups(rs.getInt("mixed_multiple_ethnic_groups"));
			socioData.setAsian_asian_british(rs.getInt("asian_asian_british"));
			socioData.setBlack_african_caribbean_black_british(rs.getInt("black_african_caribbean_black_british"));
			socioData.setUnited_kingdom(rs.getInt("united_kingdom"));
			socioData.setNot_united_kingdom(rs.getInt("not_united_kingdom"));
			socioData.setMedian_price(rs.getDouble("median_price"));
			socioData.setSales(rs.getInt("sales"));
			socioData.setEconomically_active_total(rs.getInt("economically_active_total"));
			socioData.setEconomically_inactive_total(rs.getInt("economically_inactive_total"));
			socioData.setEconomically_active_employee(rs.getInt("economically_active_employee"));
			socioData.setEconomically_active_self_employed(rs.getInt("economically_active_self_employed"));
			socioData.setEconomically_active_unemployed(rs.getInt("economically_active_unemployed"));
			socioData.setEconomically_active_full_time_student(rs.getInt("economically_active_full_time_student"));
			socioData.setEmployment_rate(rs.getDouble("employment_rate"));
			socioData.setUnemployment_rate(rs.getDouble("unemployment_rate"));
			socioData.setNo_qualifications(rs.getInt("no_qualifications"));
			socioData.setHighest_level_of_qualification_level_1_qualifications(rs.getInt("highest_level_of_qualification_level_1_qualifications"));
			socioData.setHighest_level_of_qualification_level_2_qualifications(rs.getInt("highest_level_of_qualification_level_2_qualifications"));
			socioData.setHighest_level_of_qualification_apprenticeship(rs.getInt("highest_level_of_qualification_apprenticeship"));
			socioData.setHighest_level_of_qualification_level_3_qualifications(rs.getInt("highest_level_of_qualification_level_3_qualifications"));
			socioData.setHighest_level_of_qualification_level_4_qualifications_and_abov(rs.getInt("highest_level_of_qualification_level_4_qualifications_and_abov"));
			socioData.setDay_to_day_activities_limited_a_lot(rs.getInt("day_to_day_activities_limited_a_lot"));
			socioData.setDay_to_day_activities_limited_a_little(rs.getInt("day_to_day_activities_limited_a_little"));
			socioData.setDay_to_day_activities_not_limited(rs.getInt("day_to_day_activities_not_limited"));
			socioData.setVery_good_or_good_health(rs.getInt("very_good_or_good_health"));
			socioData.setNo_cars_or_vans_in_household(rs.getInt("no_cars_or_vans_in_household"));
			socioData.set_1_car_or_van_in_household(rs.getInt("_1_car_or_van_in_household"));
			socioData.set_2_cars_or_vans_in_household(rs.getInt("_2_cars_or_vans_in_household"));
			socioData.set_3_cars_or_vans_in_household(rs.getInt("_3_cars_or_vans_in_household"));
			socioData.set_4_or_more_cars_or_vans_in_household(rs.getInt("_4_or_more_cars_or_vans_in_household"));
			list.add(socioData);
		}
		return list;
		
	}

	@Override
	public void save(SocioData entidade) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SocioData> findAll() {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM social_data";

        List<SocioData> listData = new ArrayList<SocioData>();

        try {
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            
            listData = setSocioAttributes(rs);
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conexao.close(conn, pstm, rs);
        }
        return listData;
		
	}

}
package be.fsoffe.imaging.pb.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import be.fsoffe.imaging.pb.model.LinkContext;

/**
 * Map a link context object based on a result set.
 * 
 * @author jbourlet
 *
 */
public final class LinkContextMapper {

	/**
	 * Static class recommandation.
	 */
	private LinkContextMapper() {
		//Empty one
	}
	
	/**
	 * Map a link context object based on a result set.
	 * @param rs the resultSet
	 * @return a LinkContext object
	 * @throws SQLException if error occurs
	 */
	public static LinkContext mapLinkContext(ResultSet rs) throws SQLException {
		LinkContext lc = new LinkContext();
		lc.setSessionId(rs.getLong(1));
		lc.setUserName(rs.getString(2));
		lc.setRefScreen(rs.getLong(3));
		if (rs.wasNull()) {
			lc.setRefScreen(null);
		}
		lc.setRefDossier(rs.getLong(4));
		if (rs.wasNull()) {
			lc.setRefDossier(null);
		}
		lc.setMap(rs.getString(5));
		lc.setRefEmployer(rs.getLong(6));
		if (rs.wasNull()) {
			lc.setRefEmployer(null);
		}
		lc.setRefWorker(rs.getLong(7));
		if (rs.wasNull()) {
			lc.setRefWorker(null);
		}
		lc.setRefPerson(rs.getLong(8));
		if (rs.wasNull()) {
			lc.setRefPerson(null);
		}
		lc.setRefKeyword1(rs.getLong(9));
		if (rs.wasNull()) {
			lc.setRefKeyword1(null);
		}
		lc.setRefKeyword2(rs.getLong(10));
		if (rs.wasNull()) {
			lc.setRefKeyword2(null);
		}
		
		lc.setLogicalOp(rs.getLong(12));
		if (rs.wasNull()) {
			lc.setLogicalOp(null);
		}
		
		lc.setRefDocType(rs.getLong(13));
		if (rs.wasNull()) {
			lc.setRefDocType(null);
		}
		
		lc.setSpecialjp(rs.getBoolean(14));
		
		lc.setRefGajur(rs.getLong(15));
		if (rs.wasNull()) {
			lc.setRefGajur(null);
		}
				
		return lc;
	}
}

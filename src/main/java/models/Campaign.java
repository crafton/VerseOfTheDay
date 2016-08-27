package models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


/**
 * Entity implementation class for Entity: CampaignTest
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Campaign.findAll", query = "SELECT x FROM Campaign x"),
	@NamedQuery(name = "Campaign.findActive", query = "SELECT x FROM Campaign x WHERE :now BETWEEN x.startDate AND x.endDate")
})
public class Campaign {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long campaignId;
	private Timestamp startDate;
	private Timestamp endDate;
	private String campaignName;
	private String campaignDescription;
	private int campaignDays;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "CampaignTheme", joinColumns = @JoinColumn(name = "campaignId", referencedColumnName = "campaignId") , inverseJoinColumns = @JoinColumn(name = "themeId", referencedColumnName = "id") )
	private List<Theme> themeList;

	public Campaign() {
		super();
	}

	public Long getCampaignId() {
		return this.campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
//		 SimpleDateFormat htmlFormat = new SimpleDateFormat("dd/MM/yyyy");
//		 SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
//		 Date convertedCurrentDate;
//		try {
//			convertedCurrentDate = dbFormat.parse(dbFormat.format(endDate));
//			String date=htmlFormat.format(convertedCurrentDate );
//			endDate = htmlFormat.parse(date);
//			
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		 
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getCampaignName() {
		return this.campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public int getCampaignDays() {
		return this.campaignDays;
	}

	public void setCampaignDays(int campaignDays) {
		this.campaignDays = campaignDays;
	}

	public List<Theme> getThemeList() {
		return themeList;
	}

	public void setThemeList(List<Theme> themeList) {
		this.themeList = themeList;
	}

	public String getCampaignDescription() {
		return campaignDescription;
	}

	public void setCampaignDescription(String campaignDescription) {
		this.campaignDescription = campaignDescription;
	}
}

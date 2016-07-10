package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.GenericGenerator;

/**
 * Entity implementation class for Entity: CampaignTest
 *
 */
@Entity
public class Campaign implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long campaignId;
	private Date startDate;
	private Date endDate;
	private String campaignName;
	private int campaignDays;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "CampaignTheme", joinColumns = @JoinColumn(name = "campaignId", referencedColumnName = "campaignId") , inverseJoinColumns = @JoinColumn(name = "themeId", referencedColumnName = "themeId") )
	private List<Theme> themeList;

	private static final long serialVersionUID = 1L;

	public Campaign() {
		super();
	}

	public Long getCampaignId() {
		return this.campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
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

	public void setEndDate(Date endDate) {
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

}

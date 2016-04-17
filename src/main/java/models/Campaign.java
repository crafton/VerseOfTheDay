package models;

import java.io.Serializable;
import java.lang.String;
import java.sql.Date;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: CampaignTest
 *
 */
@Entity
public class Campaign implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String campaignId;
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

	public String getCampaignId() {
		return this.campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
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

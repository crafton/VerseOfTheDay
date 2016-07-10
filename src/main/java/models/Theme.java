package models;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Theme
 *
 */
@Entity

public class Theme implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long themeId;
	private String themeName;

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "themeList")
	private List<Campaign> campaignList;
	
	private static final long serialVersionUID = 1L;

	public Theme() {
		super();
	}

	public Long getThemeId() {
		return this.themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getThemeName() {
		return this.themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public List<Campaign> getCampaignList() {
		return campaignList;
	}

	public void setCampaignList(List<Campaign> campaignList) {
		this.campaignList = campaignList;
	}

}

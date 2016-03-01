package tr.org.liderahenk.network.inventory.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AHENK_SETUP_DETAIL_RESULT")
public class AhenkSetupResultDetail {

	@Id
	@GeneratedValue
	@Column(name = "AHENK_SETUP_DETAIL_RESULT_ID", unique = true, nullable = false)
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "AHENK_SETUP_RESULT_ID", nullable = false)
	private AhenkSetupParameters parent;
	
	@Column(name = "IP", nullable = false)
	private String ip;
	
	@Column(name = "SETUP_RESULT", nullable = false)
	private String setupResult;

	public AhenkSetupResultDetail() {
		super();
	}
	
	public AhenkSetupResultDetail(Long id, String ip, String setupResult) {
		super();
		this.id = id;
		this.ip = ip;
		this.setupResult = setupResult;
		// DO NOT forget to set 'parent'!
	}

	public AhenkSetupParameters getParent() {
		return parent;
	}

	public void setParent(AhenkSetupParameters parent) {
		this.parent = parent;
	}
}

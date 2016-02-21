package tr.org.liderahenk.network.inventory.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for file distribution results.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.network.inventory.dto.FileDistResultDto
 *
 */
@Entity
@Table(name = "P_FILE_DIST_RESULT")
public class FileDistResult {
	
	@Id
	@GeneratedValue
	@Column(name = "FILE_DIST_RESULT_ID")
	private Long id;
	
	private ArrayList<String> ipAddresses;

	private String fileName;

	private String username;

	private String password;

	private Integer port;

	private String privateKey;

	private String destDirectory;

	private Date fileDistDate;

	@OneToMany(mappedBy = "fileDistResult", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<FileDistResultHost> hosts;

	public FileDistResult() {
		super();
	}

	public FileDistResult(Long id, ArrayList<String> ipAddresses, String fileName, String username, String password,
			Integer port, String privateKey, String destDirectory, Date fileDistDate, List<FileDistResultHost> hosts) {
		super();
		this.id = id;
		this.ipAddresses = ipAddresses;
		this.fileName = fileName;
		this.username = username;
		this.password = password;
		this.port = port;
		this.privateKey = privateKey;
		this.destDirectory = destDirectory;
		this.fileDistDate = fileDistDate;
		this.hosts = hosts;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ArrayList<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(ArrayList<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getDestDirectory() {
		return destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}

	public Date getFileDistDate() {
		return fileDistDate;
	}

	public void setFileDistDate(Date fileDistDate) {
		this.fileDistDate = fileDistDate;
	}

	public List<FileDistResultHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<FileDistResultHost> hosts) {
		this.hosts = hosts;
	}

}

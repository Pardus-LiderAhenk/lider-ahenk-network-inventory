package tr.org.liderahenk.network.inventory.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class for file distribution results.
 * 
 * @author <a href="mailto:caner.feyzullahoglu@agem.com.tr">Caner Feyzullahoğlu</a>
 *
 */
public class FileDistResult implements Serializable {

	private static final long serialVersionUID = -2306871345547739795L;

	private ArrayList<String> ipAddresses;

	private String fileName;

	private String username;

	private String password;

	private Integer port;

	private String privateKey;

	private String destDirectory;

	private Date fileDistDate;

	private List<FileDistResultHost> hosts;

	public FileDistResult() {
			super();
		}

	public FileDistResult(ArrayList<String> ipAddresses, String fileName, String username, String password,
				Integer port, String privateKey, String destDirectory, Date fileDistDate,
				List<FileDistResultHost> hosts) {
			super();
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

	public static long getSerialversionuid() {
		return serialVersionUID;
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

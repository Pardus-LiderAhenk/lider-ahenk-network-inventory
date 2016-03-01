package tr.org.liderahenk.network.inventory.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.plugin.CommandResultStatus;
import tr.org.liderahenk.lider.core.api.plugin.ICommandContext;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.plugin.IPluginDbService;
import tr.org.liderahenk.network.inventory.contants.Constants;
import tr.org.liderahenk.network.inventory.dto.FileDistResultDto;
import tr.org.liderahenk.network.inventory.dto.FileDistResultHostDto;
import tr.org.liderahenk.network.inventory.entities.FileDistResult;
import tr.org.liderahenk.network.inventory.entities.FileDistResultHost;
import tr.org.liderahenk.network.inventory.runnables.RunnableFileDistributor;

/**
 * This class is responsible for distributing a file to a number of machines in
 * the given IP list. Safe-copy (SCP) utility command is used to copy file to
 * its destination and it can be configured via plugin configuration file.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class FileDistributionCommand extends BaseCommand {

	private Logger logger = LoggerFactory.getLogger(FileDistributionCommand.class);

	private ICommandResultFactory resultFactory;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ICommandResult execute(ICommandContext context) {

		logger.debug("Executing command.");

		FileDistResultDto fileDistResultDto = null;

		// Read command parameters.
		Map<String, Object> parameterMap = context.getRequest().getParameterMap();
		ArrayList<String> ipAddresses = (ArrayList<String>) parameterMap.get("ipAddresses");
		File fileToTransfer = getFileInstance((String) parameterMap.get("file"), (String) parameterMap.get("filename"));
		String username = (String) parameterMap.get("username");
		String password = (String) parameterMap.get("password");
		Integer port = (Integer) (parameterMap.get("port") == null ? 22 : parameterMap.get("port"));
		byte[] privateKey = (byte[]) parameterMap.get("privateKey");
		String destDirectory = (String) parameterMap.get("destDirectory");

		logger.debug("Parameter map: {}", parameterMap);

		// Create new instance to send back to Lider Console
		fileDistResultDto = new FileDistResultDto(ipAddresses, fileToTransfer.getName(), username, password, port,
				privateKey, destDirectory, new Date(),
				Collections.synchronizedList(new ArrayList<FileDistResultHostDto>()));

		// Distribute the provided file via threads.
		// Each thread is responsible for a limited number of hosts!
		if (ipAddresses != null && !ipAddresses.isEmpty() && fileToTransfer != null) {

			// Create thread pool executor!
			LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
			final List<Runnable> running = Collections.synchronizedList(new ArrayList());
			ThreadPoolExecutor executor = new ThreadPoolExecutor(Constants.SSH_CONFIG.NUM_THREADS,
					Constants.SSH_CONFIG.NUM_THREADS, 0L, TimeUnit.MILLISECONDS, taskQueue,
					Executors.defaultThreadFactory()) {

				@Override
				protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, T value) {
					return new FutureTask<T>(runnable, value) {
						@Override
						public String toString() {
							return runnable.toString();
						}
					};
				}

				@Override
				protected void beforeExecute(Thread t, Runnable r) {
					super.beforeExecute(t, r);
					running.add(r);
				}

				@Override
				protected void afterExecute(Runnable r, Throwable t) {
					super.afterExecute(r, t);
					running.remove(r);
					logger.debug("Running threads: {}", running);
				}
			};

			logger.debug("Created thread pool executor for file distribution.");

			// Calculate number of the hosts a thread can process
			int numberOfHosts = ipAddresses.size();
			int hostsPerThread = numberOfHosts / Constants.SSH_CONFIG.NUM_THREADS;

			logger.debug("Hosts: {}, Threads:{}, Host per Thread: {}",
					new Object[] { numberOfHosts, Constants.SSH_CONFIG.NUM_THREADS, hostsPerThread });

			// Create & execute threads
			for (int i = 0; i < numberOfHosts; i += hostsPerThread) {
				int toIndex = i + hostsPerThread;
				List<String> ipSubList = ipAddresses.subList(i,
						toIndex < ipAddresses.size() ? toIndex : ipAddresses.size() - 1);

				RunnableFileDistributor distributor = new RunnableFileDistributor(fileDistResultDto, ipSubList,
						username, password, port, privateKey, fileToTransfer, destDirectory);
				executor.execute(distributor);
			}

			executor.shutdown();

			// Insert new distribution result record
			pluginDbService.save(getEntityObject(fileDistResultDto));
		}

		logger.info("Command executed successfully.");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap.put("result", mapper.writeValueAsString(fileDistResultDto));
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this, resultMap);
	}

	/**
	 * Convert data transfer object to entity object.
	 * 
	 * @param dto
	 * @return
	 */
	private FileDistResult getEntityObject(FileDistResultDto dto) {
		FileDistResult entity = new FileDistResult(null, joinIpAddresses(dto.getIpAddresses()), dto.getFileName(),
				dto.getUsername(), dto.getPassword(), dto.getPort(), dto.getPrivateKey(), dto.getDestDirectory(),
				dto.getFileDistDate(), null);
		entity.setHosts(getEntityList(dto.getHosts(), entity));
		return entity;
	}

	/**
	 * Convert list of data transfer objects to list of entity objects.
	 * 
	 * @param dtoList
	 * @param parentEntity
	 * @return
	 */
	private List<FileDistResultHost> getEntityList(List<FileDistResultHostDto> dtoList, FileDistResult parentEntity) {
		List<FileDistResultHost> entityList = new ArrayList<FileDistResultHost>();
		if (dtoList != null) {
			for (FileDistResultHostDto dto : dtoList) {
				FileDistResultHost entity = new FileDistResultHost(null, parentEntity, dto.getIp(), dto.isSuccess(),
						dto.getErrorMessage());
				entityList.add(entity);
			}
		}
		return entityList;
	}

	/**
	 * Join given IP addresses by comma.
	 * 
	 * @param ipAddresses
	 * @return
	 */
	private String joinIpAddresses(ArrayList<String> ipAddresses) {
		if (ipAddresses != null) {
			StringBuilder ipAddressStr = new StringBuilder("");
			for (String ipAddress : ipAddresses) {
				if (ipAddressStr.length() > 0) {
					ipAddressStr.append(",");
				}
				ipAddressStr.append(ipAddress);
			}
		}
		return null;
	}

	/**
	 * Create a tempopary file which can be used for SCP.
	 * 
	 * @param contents
	 * @param filename
	 * @return
	 */
	private File getFileInstance(String contents, String filename) {
		File temp = null;
		try {
			temp = File.createTempFile(filename, "");
			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(contents);
			out.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return temp;
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		return "DISTRIBUTEFILE";
	}

	@Override
	public Boolean needsTask() {
		return false;
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	public void setLogService(IOperationLogService logService) {
		this.logService = logService;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}

}

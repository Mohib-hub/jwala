package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * The CommandFactory class.<br/>
 */
@Component
public final class CommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandFactory.class);

    private HashMap<String, Command> commands;

    @Autowired
    protected SshConfiguration sshConfig;

    @Value("${remote.paths.instances}")
    protected String remoteJvmInstanceDir;

    @Value("${remote.jwala.java.home}")
    protected String remoteJdkHome;
    @Value("${jmap.dump.live.enabled}")
    protected Boolean dumpLive;

    @Value("${remote.jwala.data.dir}")
    protected String remoteDataDir;

    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;

    /**
     *
     * @param jvm
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(Jvm jvm, JvmControlOperation operation) throws ApplicationServiceException{
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).apply(jvm);
        }
        throw new ApplicationServiceException("Command not found");
    }

    public void listCommands() {
        LOGGER.debug("Available jvm commands");
        for (String command:commands.keySet()) {
            LOGGER.debug(command);
        }
    }

    /* Factory pattern */
    @PostConstruct
    public void initJvmCommands() {
        commands = new HashMap<>();
        // commands are added here using lambdas. It is also possible to dynamically add commands without editing the code.
        commands.put(JvmControlOperation.START.getExternalValue(), (Jvm jvm) -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommand("start-service.sh", jvm))));
        commands.put(JvmControlOperation.STOP.getExternalValue(), (Jvm jvm) -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommand("stop-service.sh", jvm))));
        commands.put(JvmControlOperation.THREAD_DUMP.getExternalValue(), (Jvm jvm) -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForThreadDump("thread-dump.sh", jvm))));
        commands.put(JvmControlOperation.HEAP_DUMP.getExternalValue(), (Jvm jvm) -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForHeapDump("heap-dump.sh", jvm))));
    }

    private RemoteSystemConnection getConnection(Jvm jvm) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort());
    }

    /**
     * Get
     * @param jvm
     * @param scriptName
     * @return
     */
    private String getFullPathScript(Jvm jvm, String scriptName){
        return remoteJvmInstanceDir + "/"+jvm.getJvmName()+"/bin/"+scriptName;
    }

    /**
     * Generate parameters for JVM Heap dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForHeapDump(String scriptName, Jvm jvm) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
        String dumpFile = "heapDump." + StringUtils.replace(jvm.getJvmName(), " ", "") + "." +fmt.print(DateTime.now());
        String dumpLiveStr = dumpLive ? "live," : "";
        String jvmInstanceDir = remoteJvmInstanceDir + "/" +StringUtils.replace(jvm.getJvmName(), " ", "");
        return new ExecCommand(getFullPathScript(jvm, scriptName),remoteJdkHome, remoteDataDir, dumpFile, dumpLiveStr, jvmInstanceDir);
    }

    /**
     * Generate parameters for Thread dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForThreadDump(String scriptName, Jvm jvm) {
        String jvmInstanceDir = remoteJvmInstanceDir + "/" +StringUtils.replace(jvm.getJvmName(), " ", "");
        return new ExecCommand(getFullPathScript(jvm, scriptName),remoteJdkHome, jvmInstanceDir);
    }
    private ExecCommand getExecCommand(String scriptName, Jvm jvm){
        return new ExecCommand(getFullPathScript(jvm, scriptName), jvm.getJvmName());
    }
}

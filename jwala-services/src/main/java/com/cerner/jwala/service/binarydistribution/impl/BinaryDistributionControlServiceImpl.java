package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.control.binarydistribution.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;

/**
 * Created by SP043299 on 9/7/2016.
 */
public class BinaryDistributionControlServiceImpl implements BinaryDistributionControlService {
    private final RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;

    public BinaryDistributionControlServiceImpl(RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor) {
        this.remoteCommandExecutor = remoteCommandExecutor;
    }

    @Override
    public CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.SECURE_COPY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                source,
                destination);
    }

    @Override
    public CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.CREATE_DIRECTORY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }

    @Override
    public CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.CHECK_FILE_EXISTS,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }

    @Override
    public CommandOutput unzipBinary(final String hostname, final String binaryLocation) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.UNZIP_BINARY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                binaryLocation);
    }

    @Override
    public CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.DELETE_BINARY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }
}
